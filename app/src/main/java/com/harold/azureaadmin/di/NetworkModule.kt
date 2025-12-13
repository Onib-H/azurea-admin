package com.harold.azureaadmin.di

import android.util.Log
import com.harold.azureaadmin.data.local.DataStoreManager
import com.harold.azureaadmin.data.remote.AdminApiService
import com.harold.azureaadmin.utils.BASE_URL
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.harold.azureaadmin.dto.CookieDto
import com.harold.azureaadmin.dto.cookieToDto
import com.harold.azureaadmin.dto.dtoToCookie
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideCookieJar(
        dataStore: DataStoreManager,
        gson: Gson
    ): CookieJar {

        Log.d("COOKIE_JAR", "CookieJar CREATED")

        val cookies = mutableListOf<Cookie>()
        val lock = Any()
        val isInitialized = AtomicBoolean(false)

        // Load cookies lazily on first actual use, not during DI initialization
        suspend fun ensureInitialized() {
            if (isInitialized.getAndSet(true)) return

            try {
                dataStore.initializeCache()
                val json = dataStore.getCachedCookieJson()

                if (!json.isNullOrBlank()) {
                    val type = object : TypeToken<List<CookieDto>>() {}.type
                    val list: List<CookieDto> = gson.fromJson(json, type) ?: emptyList()

                    synchronized(lock) {
                        val converted = list.mapNotNull { dto ->
                            try {
                                dtoToCookie(dto)
                            } catch (ex: Exception) {
                                null
                            }
                        }
                        cookies.addAll(converted)
                        cookies.removeAll { it.expiresAt <= System.currentTimeMillis() }
                        Log.d("COOKIE_JAR", "Loaded ${cookies.size} valid cookies")
                    }
                }
            } catch (ex: Exception) {
                Log.e("COOKIE_JAR", "Initialization error: ${ex.message}")
            }
        }

        suspend fun persist() {
            val dtos = synchronized(lock) {
                cookies.filter { it.expiresAt > System.currentTimeMillis() }
                    .map { cookieToDto(it) }
            }
            try {
                dataStore.saveCookie(gson.toJson(dtos))
            } catch (ex: Exception) {
                Log.e("COOKIE_JAR", "persist failed: ${ex.message}")
            }
        }

        return object : CookieJar {

            override fun saveFromResponse(url: HttpUrl, received: List<Cookie>) {
                try {
                    Log.d("COOKIE_JAR", "Saving ${received.size} cookies from ${url.host}")

                    synchronized(lock) {
                        for (c in received) {
                            val isEmptyValue = c.value.isEmpty()
                            val isExpiredNow = c.expiresAt <= System.currentTimeMillis()

                            if (isEmptyValue || isExpiredNow) {
                                cookies.removeAll {
                                    it.name == c.name &&
                                            it.domain == c.domain &&
                                            it.path == c.path
                                }
                                Log.d("COOKIE_JAR", "Removed cookie ${c.name}")
                            } else {
                                cookies.removeAll {
                                    it.name == c.name &&
                                            it.domain == c.domain &&
                                            it.path == c.path
                                }
                                cookies.add(c)
                                Log.d("COOKIE_JAR", "Added cookie ${c.name}")
                            }
                        }
                        cookies.removeAll { it.expiresAt <= System.currentTimeMillis() }
                    }

                    // Persist asynchronously
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            persist()
                        } catch (ex: Exception) {
                            Log.e("COOKIE_JAR", "persist coroutine failed: ${ex.message}")
                        }
                    }
                } catch (ex: Exception) {
                    Log.e("COOKIE_JAR", "saveFromResponse error: ${ex.message}")
                }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val path = url.encodedPath

                // Skip cookies for login endpoint
                if (path.contains("/api/auth/login")) {
                    Log.d("COOKIE_JAR", "Skipping cookies for login request")
                    return emptyList()
                }

                // Lazy initialization on first non-login request
                if (!isInitialized.get()) {
                    runBlocking {
                        ensureInitialized()
                    }
                }

                val valid = synchronized(lock) {
                    cookies.filter {
                        it.expiresAt > System.currentTimeMillis() &&
                                url.host.endsWith(it.domain.removePrefix(".")) &&
                                url.encodedPath.startsWith(it.path)
                    }
                }

                Log.d("COOKIE_JAR", "Loading ${valid.size} cookies for ${url.host}")
                return valid
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        cookieJar: CookieJar
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)  // Increased from 20
            .readTimeout(30, TimeUnit.SECONDS)      // Increased from 25
            .writeTimeout(30, TimeUnit.SECONDS)     // Increased from 25
            .retryOnConnectionFailure(true)         // Added retry
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAdminApiService(
        retrofit: Retrofit
    ): AdminApiService =
        retrofit.create(AdminApiService::class.java)
}