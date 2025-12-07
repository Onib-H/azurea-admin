package com.harold.azureaadmin.di

import com.harold.azureaadmin.data.local.DataStoreManager
import com.harold.azureaadmin.data.remote.AdminApiService
import com.harold.azureaadmin.utils.BASE_URL
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

        var memoryCookieCache: MutableMap<String, List<Cookie>> = mutableMapOf()
        var isLoadedFromDisk = false

        // Preload cookies once in background
        CoroutineScope(Dispatchers.IO).launch {
            val savedJson = dataStore.getCookie.firstOrNull()
            if (!savedJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<String>>() {}.type
                val cookieStrings: List<String> = gson.fromJson(savedJson, type)

                val cookies = cookieStrings.mapNotNull { raw ->
                    // use dummy url host because the actual host will load later
                    Cookie.parse(HttpUrl.Builder().scheme("http").host("dummy").build(), raw)
                }

                memoryCookieCache["preload"] = cookies
            }
            isLoadedFromDisk = true
        }

        return object : CookieJar {

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                if (cookies.isEmpty()) return

                val valid = cookies.filter { it.expiresAt > System.currentTimeMillis() }

                memoryCookieCache[url.host] = valid

                val json = gson.toJson(valid.map { it.toString() })

                CoroutineScope(Dispatchers.IO).launch {
                    dataStore.saveCookie(json)
                }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {

                // If Memory already has cookies for this host
                memoryCookieCache[url.host]?.let { list ->
                    return list.filter { it.expiresAt > System.currentTimeMillis() }
                }

                // If not loaded from disk yet, return empty and soon the preload fills memory
                if (!isLoadedFromDisk) return emptyList()

                // If we preloaded cookies, rebind them to this host
                memoryCookieCache["preload"]?.let { preloadCookies ->
                    val updated = preloadCookies.mapNotNull { stored ->
                        Cookie.parse(url, stored.toString())
                    }
                    memoryCookieCache[url.host] = updated
                    return updated
                }

                return emptyList()
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
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
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