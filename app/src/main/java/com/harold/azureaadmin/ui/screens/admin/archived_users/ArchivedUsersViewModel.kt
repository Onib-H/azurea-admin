package com.harold.azureaadmin.ui.screens.admin.archived_users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harold.azureaadmin.data.models.Pagination
import com.harold.azureaadmin.data.models.User
import com.harold.azureaadmin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedUsersViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _pagination = MutableStateFlow<Pagination?>(null)
    val pagination: StateFlow<Pagination?> = _pagination

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _restoreSuccess = MutableStateFlow<String?>(null)
    val restoreSuccess: StateFlow<String?> = _restoreSuccess

    private val _restoreError = MutableStateFlow<String?>(null)
    val restoreError: StateFlow<String?> = _restoreError

    private var currentPage = 1
    private val pageSize = 10

    fun fetchArchivedUsers(page: Int = 1) {
        viewModelScope.launch {
            if (_loading.value) return@launch

            _loading.value = true
            _error.value = null

            try {
                val res = repository.fetchAllArchivedUsers(page, pageSize)
                val body = res.body() ?: return@launch run {
                    _error.value = "No data"
                }

                _users.value = body.users
                _pagination.value = body.pagination
                currentPage = page

            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }

            _loading.value = false
        }
    }

    fun restoreUser(userId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.restoreUser(userId)
                if (!res.isSuccessful) {
                    _restoreError.value = "Failed: ${res.code()}"
                    return@launch
                }

                _users.value = _users.value.filterNot { it.id == userId }
                _restoreSuccess.value = "User restored"

            } catch (e: Exception) {
                _restoreError.value = e.localizedMessage
            }
        }
    }

    fun clearRestoreMessages() {
        _restoreSuccess.value = null
        _restoreError.value = null
    }
}
