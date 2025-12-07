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
class ArchivedUsersViewModel@Inject constructor(
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

    // Add these new state flows
    private val _restoreSuccess = MutableStateFlow<String?>(null)
    val restoreSuccess: StateFlow<String?> = _restoreSuccess

    private val _restoreError = MutableStateFlow<String?>(null)
    val restoreError: StateFlow<String?> = _restoreError

    private var currentPage = 1
    private val pageSize = 10

    fun fetchArchivedUsers(page: Int = 1) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.fetchAllArchivedUsers(page, pageSize)
                if (response.isSuccessful) {
                    response.body()?.let { usersResponse ->
                        _users.value = usersResponse.users
                        _pagination.value = usersResponse.pagination
                        currentPage = page
                    }
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch archived users: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadNextPage() {
        _pagination.value?.let { pagination ->
            if (currentPage < pagination.total_pages) {
                fetchArchivedUsers(currentPage + 1)
            }
        }
    }

    fun loadPreviousPage() {
        if (currentPage > 1) {
            fetchArchivedUsers(currentPage - 1)
        }
    }

    // Add this new method
    fun restoreUser(userId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.restoreUser(userId)
                if (response.isSuccessful) {
                    _restoreSuccess.value = "User restored successfully"
                    // Remove the user from the current list
                    _users.value = _users.value.filter { it.id != userId }
                    _restoreError.value = null
                } else {
                    _restoreError.value = "Failed to restore user: ${response.code()}"
                }
            } catch (e: Exception) {
                _restoreError.value = e.localizedMessage ?: "Unknown error"
            }
        }
    }

    // Add method to clear messages
    fun clearRestoreMessages() {
        _restoreSuccess.value = null
        _restoreError.value = null
    }
}