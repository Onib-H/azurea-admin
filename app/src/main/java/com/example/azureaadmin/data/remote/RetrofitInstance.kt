package com.example.azureaadmin.data.remote

import android.content.Context
import com.example.azureaadmin.data.local.DataStoreManager
import com.example.azureaadmin.utils.BASE_URL
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

object RetrofitInstance {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 🔑 Keep cookies in memory
    private val cookieStore = mutableMapOf<String, List<Cookie>>()
    private var dataStore: DataStoreManager? = null
    private val gson = Gson()

    // 🔑 Clear all cookies + stored session
    fun clearCookies() {
        cookieStore.clear()
        dataStore?.let {
            runBlocking { it.clearAll() }
        }
    }

    fun removeExpiredCookies() {
        val now = System.currentTimeMillis()

        // Filter in-memory cookies
        cookieStore.keys.forEach { host ->
            val valid = cookieStore[host]?.filter { it.expiresAt > now } ?: emptyList()
            cookieStore[host] = valid
        }

        // Save only valid cookies back to DataStore
        val allValidCookies = cookieStore.values.flatten()
        val serialized = gson.toJson(allValidCookies.map { it.toString() })

        runBlocking {
            withContext(Dispatchers.IO) {
                dataStore?.saveCookie(serialized)
            }
        }
    }

    fun getApi(context: Context): AdminApiService {
        val appCtx = context.applicationContext
        if (dataStore == null) {
            dataStore = DataStoreManager(appCtx)
        }

        val cookieJar = object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                if (cookies.isEmpty()) return

                // ✅ Only keep non-expired cookies
                val validCookies = cookies.filter { it.expiresAt > System.currentTimeMillis() }
                cookieStore[url.host] = validCookies

                // Save valid cookies as JSON
                val serialized = gson.toJson(validCookies.map { it.toString() })
                runBlocking {
                    withContext(Dispatchers.IO) {
                        dataStore?.saveCookie(serialized)
                    }
                }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                // ✅ First check in-memory store
                cookieStore[url.host]?.let { stored ->
                    return stored.filter { it.expiresAt > System.currentTimeMillis() }
                }

                // ✅ Else load from DataStore
                val persisted = runBlocking { dataStore?.getCookie?.firstOrNull() }
                if (persisted.isNullOrEmpty()) return emptyList()

                val type = object : TypeToken<List<String>>() {}.type
                val cookieStrings: List<String> = gson.fromJson(persisted, type)

                // Parse and drop expired cookies
                val parsed = cookieStrings.mapNotNull { Cookie.parse(url, it) }
                    .filter { it.expiresAt > System.currentTimeMillis() }

                if (parsed.isNotEmpty()) {
                    cookieStore[url.host] = parsed
                    return parsed
                }
                return emptyList()
            }
        }


        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(AdminApiService::class.java)
    }
}
