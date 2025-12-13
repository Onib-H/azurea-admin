package com.harold.azureaadmin.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

class DataStoreManager(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore("user_prefs")
        private val COOKIE_JSON_KEY = stringPreferencesKey("cookie_json")
        private val SESSION_EXPIRY_KEY = longPreferencesKey("session_expiry")
    }

    @Volatile
    private var cachedCookieJson: String? = null

    @Volatile
    private var cachedSessionExpiry: Long? = null

    @Volatile
    private var isCacheInitialized = false

    suspend fun initializeCache() {
        if (isCacheInitialized) return

        val prefs = context.dataStore.data.first()
        cachedCookieJson = prefs[COOKIE_JSON_KEY]
        cachedSessionExpiry = prefs[SESSION_EXPIRY_KEY]
        isCacheInitialized = true
    }

    fun getCachedCookieJson(): String? = cachedCookieJson

    fun isSessionExpired(): Boolean {
        val expiry = cachedSessionExpiry ?: return true
        return System.currentTimeMillis() > expiry
    }

    suspend fun saveCookie(cookieJson: String) {
        val expiry30Days =
            System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)

        cachedCookieJson = cookieJson
        cachedSessionExpiry = expiry30Days

        context.dataStore.edit { prefs ->
            prefs[COOKIE_JSON_KEY] = cookieJson
            prefs[SESSION_EXPIRY_KEY] = expiry30Days
        }
    }

    suspend fun clearAll() {
        cachedCookieJson = null
        cachedSessionExpiry = null
        context.dataStore.edit { it.clear() }
    }
}
