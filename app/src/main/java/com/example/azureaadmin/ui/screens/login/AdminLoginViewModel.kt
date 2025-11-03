package com.example.azureaadmin.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azureaadmin.data.local.DataStoreManager
import com.example.azureaadmin.data.models.AdminLoginResponse
import com.example.azureaadmin.data.remote.RetrofitInstance
import com.example.azureaadmin.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val response: AdminLoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

class AdminLoginViewModel(
    private val repository: AdminRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> get() = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                val response = repository.login(email, password)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val user = body.user
                        if (user == null) {
                            _loginState.value = LoginState.Error("Not authenticated")
                            return@launch
                        }

                        if (user.role != "admin") {
                            _loginState.value = LoginState.Error("Only admin can access this app")
                            return@launch
                        }

                        // ✅ Save token if provided
                        body.access_token?.let { token ->
                            dataStoreManager.saveToken(token)
                        }

                        _loginState.value = LoginState.Success(body)
                    } else {
                        _loginState.value = LoginState.Error("Login failed: Empty response")
                    }
                } else {
                    if (response.code() == 401) {
                        _loginState.value = LoginState.Error("Invalid email or password")
                    } else {
                        _loginState.value =
                            LoginState.Error("Login failed: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _loginState.value =
                    LoginState.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStoreManager.clearAll()
            RetrofitInstance.clearCookies()
            _loginState.value = LoginState.Idle
        }
    }

    suspend fun isLoggedIn(): Boolean {
        RetrofitInstance.removeExpiredCookies() // ✅ auto-clean expired ones
        val token = dataStoreManager.getToken.firstOrNull()
        val cookie = dataStoreManager.getCookie.firstOrNull()
        return !token.isNullOrEmpty() || !cookie.isNullOrEmpty()
    }

}
