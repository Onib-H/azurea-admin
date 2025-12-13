package com.harold.azureaadmin.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore("user_prefs")
        val COOKIE_KEY = stringPreferencesKey("auth_cookie")
    }

    @Volatile
    private var cachedCookie: String? = null

    @Volatile
    private var isCacheInitialized = false

    suspend fun initializeCache() {
        if (isCacheInitialized) return
        try {
            val prefs = context.dataStore.data.first()
            cachedCookie = prefs[COOKIE_KEY]
        } catch (_: Exception) {
            cachedCookie = null
        } finally {
            isCacheInitialized = true
        }
    }

    fun getCachedCookieJson(): String? = cachedCookie

    suspend fun saveCookie(cookieJson: String) {
        cachedCookie = cookieJson
        isCacheInitialized = true
        context.dataStore.edit { prefs -> prefs[COOKIE_KEY] = cookieJson }
    }

    val getCookie: Flow<String?>
        get() = context.dataStore.data.map { prefs ->
            prefs[COOKIE_KEY].also { cachedCookie = it }
        }

    suspend fun clearCookie() {
        cachedCookie = null
        context.dataStore.edit { prefs -> prefs.remove(COOKIE_KEY) }
    }

    suspend fun clearAll() {
        cachedCookie = null
        context.dataStore.edit { prefs -> prefs.clear() }
    }

}
