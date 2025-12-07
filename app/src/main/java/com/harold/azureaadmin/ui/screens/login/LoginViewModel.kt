package com.harold.azureaadmin.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harold.azureaadmin.data.local.DataStoreManager
import com.harold.azureaadmin.data.models.AdminLoginResponse
import com.harold.azureaadmin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val response: AdminLoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AdminRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> get() = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                val response = repo.login(email, password)

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

                        body.access_token?.let { token ->
                            dataStore.saveToken(token)
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
}
