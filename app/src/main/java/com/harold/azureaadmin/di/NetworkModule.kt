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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
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
        val cookieStore = mutableMapOf<String, List<Cookie>>()

        return object : CookieJar {

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                if (cookies.isEmpty()) return

                val valid = cookies.filter { it.expiresAt > System.currentTimeMillis() }
                cookieStore[url.host] = valid

                val json = gson.toJson(valid.map { it.toString() })

                runBlocking {
                    withContext(Dispatchers.IO) {
                        dataStore.saveCookie(json)
                    }
                }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                cookieStore[url.host]?.let { stored ->
                    return stored.filter { it.expiresAt > System.currentTimeMillis() }
                }

                val saved = runBlocking { dataStore.getCookie.firstOrNull() }
                if (saved.isNullOrEmpty()) return emptyList()

                val type = object : TypeToken<List<String>>() {}.type
                val cookieStrings: List<String> = gson.fromJson(saved, type)

                val parsed = cookieStrings.mapNotNull { Cookie.parse(url, it) }
                    .filter { it.expiresAt > System.currentTimeMillis() }

                if (parsed.isNotEmpty()) cookieStore[url.host] = parsed

                return parsed
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
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
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