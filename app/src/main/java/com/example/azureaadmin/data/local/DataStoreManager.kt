package com.example.azureaadmin.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore("user_prefs")

        // Token key (if you also use JWT)
        val TOKEN_KEY = stringPreferencesKey("access_token")

        // Cookie key
        val COOKIE_KEY = stringPreferencesKey("auth_cookie")
    }

    // ---------- TOKEN ----------
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    val getToken: Flow<String?>
        get() = context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    // ---------- COOKIE ----------
    suspend fun saveCookie(cookieJson: String) {
        context.dataStore.edit { prefs ->
            prefs[COOKIE_KEY] = cookieJson
        }
    }

    val getCookie: Flow<String?>
        get() = context.dataStore.data.map { prefs ->
            prefs[COOKIE_KEY]
        }

    suspend fun clearCookie() {
        context.dataStore.edit { prefs ->
            prefs.remove(COOKIE_KEY)
        }
    }

    // ---------- CLEAR ALL ----------
    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
