package com.harold.azureaadmin.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harold.azureaadmin.data.local.DataStoreManager
import com.harold.azureaadmin.data.models.AdminLoginResponse
import com.harold.azureaadmin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
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
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password are required")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                val response = repo.login(email, password)

                if (!response.isSuccessful) {
                    _loginState.value = when (response.code()) {
                        401 -> LoginState.Error("Invalid email or password")
                        else -> LoginState.Error("Login failed: ${response.code()}")
                    }
                    return@launch
                }

                val body = response.body()
                if (body == null) {
                    _loginState.value = LoginState.Error("Unexpected empty response")
                    return@launch
                }

                val user = body.user
                if (user == null) {
                    _loginState.value = LoginState.Error("Not authenticated")
                    return@launch
                }

                if (user.role != "admin") {
                    _loginState.value = LoginState.Error("Only admin can access this app")
                    return@launch
                }

                // Save token on IO thread
                body.access_token?.let { token ->
                    viewModelScope.launch(Dispatchers.IO) {
                        dataStore.saveToken(token)
                    }
                }

                _loginState.value = LoginState.Success(body)

            } catch (e: Exception) {
                val msg = when (e) {
                    is java.net.SocketTimeoutException -> "Connection timed out. Please try again."
                    is java.net.UnknownHostException -> "No internet connection."
                    is java.net.ConnectException -> "Unable to reach server."
                    else -> "Something went wrong: ${e.message}"
                }

                _loginState.value = LoginState.Error(msg)
            }

        }
    }
}

