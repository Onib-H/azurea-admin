package com.harold.azureaadmin.ui.screens.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harold.azureaadmin.data.models.Pagination
import com.harold.azureaadmin.data.models.User
import com.harold.azureaadmin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UsersViewModel @Inject constructor(
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

    private val _archiveSuccess = MutableStateFlow<String?>(null)
    val archiveSuccess: StateFlow<String?> = _archiveSuccess

    private val _archiveError = MutableStateFlow<String?>(null)
    val archiveError: StateFlow<String?> = _archiveError

    private val _verificationSuccess = MutableStateFlow<String?>(null)
    val verificationSuccess: StateFlow<String?> = _verificationSuccess

    private val _verificationError = MutableStateFlow<String?>(null)
    val verificationError: StateFlow<String?> = _verificationError

    private var currentPage = 1
    private val pageSize = 10

    private fun handleError(e: Exception) {
        _error.value = e.localizedMessage ?: "Unknown error"
    }

    fun fetchUsers(page: Int = 1) {
        viewModelScope.launch {
            if (_loading.value) return@launch

            _loading.value = true
            _error.value = null

            try {
                val res = repository.fetchAllUsers(page, pageSize)
                val body = res.body() ?: return@launch run {
                    _error.value = "No user data"
                }

                _users.value = body.users
                _pagination.value = body.pagination
                currentPage = page

            } catch (e: Exception) {
                handleError(e)
            }

            _loading.value = false
        }
    }

    fun loadNextPage() {
        val page = _pagination.value?.current_page ?: return
        val total = _pagination.value?.total_pages ?: return
        if (page < total) fetchUsers(page + 1)
    }

    fun loadPreviousPage() {
        val page = _pagination.value?.current_page ?: return
        if (page > 1) fetchUsers(page - 1)
    }

    fun archiveUser(userId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.archiveUser(userId)
                if (!res.isSuccessful) {
                    _archiveError.value = "Failed: ${res.code()}"
                    return@launch
                }

                _users.value = _users.value.filterNot { it.id == userId }
                _archiveSuccess.value = "User archived"

            } catch (e: Exception) {
                _archiveError.value = e.localizedMessage
            }
        }
    }

    fun approveUserId(userId: Int, isSeniorOrPwd: Boolean) {
        viewModelScope.launch {
            try {
                val res = repository.approveValidId(userId, isSeniorOrPwd)
                val body = res.body() ?: return@launch

                _users.value = _users.value.map {
                    if (it.id == userId) body.user else it
                }

                _verificationSuccess.value = body.message
                _verificationError.value = null

            } catch (e: Exception) {
                _verificationError.value = e.localizedMessage
            }
        }
    }

    fun rejectUserId(userId: Int, reason: String) {
        viewModelScope.launch {
            try {
                val res = repository.rejectValidId(userId, reason)
                val body = res.body() ?: return@launch

                _users.value = _users.value.map {
                    if (it.id == userId)
                        it.copy(
                            is_verified = body.is_verified,
                            valid_id_rejection_reason = body.valid_id_rejection_reason
                        )
                    else it
                }

                _verificationSuccess.value = body.message
                _verificationError.value = null

            } catch (e: Exception) {
                _verificationError.value = e.localizedMessage
            }
        }
    }

    fun clearArchiveMessages() {
        _archiveSuccess.value = null
        _archiveError.value = null
    }

    fun clearVerificationMessages() {
        _verificationSuccess.value = null
        _verificationError.value = null
    }
}
