package com.harold.azureaadmin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harold.azureaadmin.data.local.DataStoreManager
import com.harold.azureaadmin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStore: DataStoreManager,
    private val repo: AdminRepository
) : ViewModel() {

    val isLoading = mutableStateOf(true)
    val startDestination = mutableStateOf("login")

    init {
        viewModelScope.launch {
            dataStore.initializeCache()

            val cookieJson = dataStore.getCachedCookieJson()

            // No cookies → not logged in
            if (cookieJson.isNullOrEmpty()) {
                dataStore.clearAll()
                startDestination.value = "login"
                isLoading.value = false
                return@launch
            }

            // Cookies exist → ask backend to validate
            try {
                val resp = repo.checkSession()
                val ok = resp.isSuccessful &&
                        resp.body()?.isAuthenticated == true &&
                        resp.body()?.role == "admin"

                if (ok) {
                    startDestination.value = "admin"
                } else {
                    dataStore.clearAll()
                    startDestination.value = "login"
                }

            } catch (_: Exception) {
                // safer: avoid stale expired cookie staying active
                dataStore.clearAll()
                startDestination.value = "login"
            }

            isLoading.value = false
        }
    }

}
