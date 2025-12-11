package com.harold.azureaadmin.ui.screens.admin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harold.azureaadmin.data.local.DataStoreManager
import com.harold.azureaadmin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repo: AdminRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    val checking = mutableStateOf(true)

    fun verifySession(onInvalid: (() -> Unit)? = null) {
        viewModelScope.launch {
            checking.value = true

            try {
                val resp = repo.checkSession()
                val ok = resp.isSuccessful &&
                        resp.body()?.isAuthenticated == true &&
                        resp.body()?.role == "admin"

                if (!ok) {
                    dataStore.clearAll()
                    onInvalid?.invoke()
                }

            } catch (_: Exception) {
                dataStore.clearAll()
                onInvalid?.invoke()
            }

            checking.value = false
        }
    }



    fun logout(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try { repo.logout() } catch (_: Exception) {}
            try { dataStore.clearAll() } catch (_: Exception) {}
            onComplete?.invoke()
        }
    }
}

