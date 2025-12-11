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

        // Load stored cookies synchronously
        runBlocking {
            try {
                dataStore.initializeCache()
            } catch (_: Exception) { }

            val json = try {
                dataStore.getCachedCookieJson()
            } catch (ex: Exception) {
                Log.d("COOKIE_JAR", "getCachedCookieJson failed: ${ex.message}")
                null
            }

            if (!json.isNullOrBlank()) {
                try {
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

                        // Add cookies properly BEFORE pruning
                        cookies.addAll(converted)

                        // 🔥 remove ONLY expired ones
                        val before = cookies.size
                        cookies.removeAll { it.expiresAt <= System.currentTimeMillis() }
                        val after = cookies.size
                        Log.d("COOKIE_JAR", "Startup prune: removed ${before - after}")
                    }

                } catch (ex: Exception) {
                    Log.d("COOKIE_JAR", "Cookie parse error: ${ex.message}")
                }
            }
        }


        // Persist helper - synchronous handling inside a try/catch
        suspend fun persist() {
            val dtos = synchronized(lock) {
                cookies.filter { it.expiresAt > System.currentTimeMillis() }
                    .map { cookieToDto(it) }
            }
            try {
                dataStore.saveCookie(gson.toJson(dtos))
            } catch (ex: Exception) {
                Log.d("COOKIE_JAR", "persist failed: ${ex.message}")
            }
        }

        return object : CookieJar {

            override fun saveFromResponse(url: HttpUrl, received: List<Cookie>) {
                try {
                    Log.d("COOKIE_JAR", "Saving cookies from ${url.host}: $received")

                    synchronized(lock) {
                        for (c in received) {
                            try {
                                val isEmptyValue = c.value.isEmpty()
                                val isExpiredNow = c.expiresAt <= System.currentTimeMillis()

                                if (isEmptyValue || isExpiredNow) {
                                    cookies.removeAll { it.name == c.name && it.domain == c.domain && it.path == c.path }
                                    Log.d("COOKIE_JAR", "Removing cookie ${c.name} for domain ${c.domain}")
                                } else {
                                    // replace existing then add
                                    cookies.removeAll { it.name == c.name && it.domain == c.domain && it.path == c.path }
                                    cookies.add(c)
                                }
                            } catch (inner: Exception) {
                                // skip malformed cookie
                                Log.d("COOKIE_JAR", "skipping malformed cookie: ${inner.message}")
                            }
                        }

                        // prune expired
                        cookies.removeAll { it.expiresAt <= System.currentTimeMillis() }
                    }

                    // Persist on IO dispatcher but ensure exceptions are caught
                    kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                        try {
                            Log.d("COOKIE_JAR", "Persisting cookies: $cookies")
                            persist()
                        } catch (ex: Exception) {
                            Log.d("COOKIE_JAR", "persist coroutine failed: ${ex.message}")
                        }
                    }
                } catch (ex: Exception) {
                    Log.d("COOKIE_JAR", "saveFromResponse error: ${ex.message}")
                }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {

                val path = url.encodedPath

                // Log cookies that exist (for debugging visibility)
                val currentCookies = synchronized(lock) { cookies.toList() }


                // 🚫 Still skip sending cookies when calling login API
                if (path.contains("/api/auth/login")) {
                    Log.d("COOKIE_JAR", "Skipping cookies for login request")
                    return emptyList()
                }

                // Normal behavior for all other requests
                val valid = synchronized(lock) {
                    cookies.toList().filter {
                        it.expiresAt > System.currentTimeMillis() &&
                                url.host.endsWith(it.domain.removePrefix(".")) &&
                                url.encodedPath.startsWith(it.path)
                    }
                }

                // Log the cookies being applied to this request
                Log.d("COOKIE_JAR", "Loading cookies for ${url.host}: $valid")

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
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
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