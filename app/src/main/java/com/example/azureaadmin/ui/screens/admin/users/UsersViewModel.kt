package com.example.azureaadmin.ui.screens.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azureaadmin.data.models.Pagination
import com.example.azureaadmin.data.models.User
import com.example.azureaadmin.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsersViewModel(private val repository: AdminRepository) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _pagination = MutableStateFlow<Pagination?>(null)
    val pagination: StateFlow<Pagination?> = _pagination

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Add these new state flows
    private val _archiveSuccess = MutableStateFlow<String?>(null)
    val archiveSuccess: StateFlow<String?> = _archiveSuccess

    private val _archiveError = MutableStateFlow<String?>(null)
    val archiveError: StateFlow<String?> = _archiveError

    private var currentPage = 1
    private val pageSize = 10

    fun fetchUsers(page: Int = 1) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.fetchAllUsers(page, pageSize)
                if (response.isSuccessful) {
                    response.body()?.let { usersResponse ->
                        _users.value = usersResponse.users
                        _pagination.value = usersResponse.pagination
                        currentPage = page
                    }
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch users: ${response.code()}"
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
                fetchUsers(currentPage + 1)
            }
        }
    }

    fun loadPreviousPage() {
        if (currentPage > 1) {
            fetchUsers(currentPage - 1)
        }
    }

    // Add this new method
    fun archiveUser(userId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.archiveUser(userId)
                if (response.isSuccessful) {
                    _archiveSuccess.value = "User archived successfully"
                    // Remove the user from the current list
                    _users.value = _users.value.filter { it.id != userId }
                    _archiveError.value = null
                } else {
                    _archiveError.value = "Failed to archive user: ${response.code()}"
                }
            } catch (e: Exception) {
                _archiveError.value = e.localizedMessage ?: "Unknown error"
            }
        }
    }

    // Add method to clear messages
    fun clearArchiveMessages() {
        _archiveSuccess.value = null
        _archiveError.value = null
    }
}