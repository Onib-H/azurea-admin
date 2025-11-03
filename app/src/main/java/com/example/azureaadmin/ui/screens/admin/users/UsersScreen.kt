package com.example.azureaadmin.ui.screens.admin.users

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.azureaadmin.data.repository.AdminRepository
import com.example.azureaadmin.ui.components.SearchFilterHeader
import com.example.azureaadmin.utils.BaseViewModelFactory

@Composable
fun UsersScreen(
    repository: AdminRepository
) {
    val viewModel: UsersViewModel = viewModel(factory = BaseViewModelFactory { UsersViewModel(repository) })
    val users by viewModel.users.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val archiveSuccess by viewModel.archiveSuccess.collectAsState()
    val archiveError by viewModel.archiveError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    // Handle archive success/error messages
    LaunchedEffect(archiveSuccess) {
        if (archiveSuccess != null) {
            showSuccessSnackbar = true
            // Clear message after showing
            kotlinx.coroutines.delay(3000)
            viewModel.clearArchiveMessages()
            showSuccessSnackbar = false
        }
    }

    LaunchedEffect(archiveError) {
        if (archiveError != null) {
            showErrorSnackbar = true
            kotlinx.coroutines.delay(3000)
            viewModel.clearArchiveMessages()
            showErrorSnackbar = false
        }
    }

    // Filtered users based on search query
    val filteredUsers = if (searchQuery.isBlank()) {
        users
    } else {
        users.filter {
            it.first_name.contains(searchQuery, ignoreCase = true) ||
                    it.last_name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SearchFilterHeader(
                title = "Manage Users",
                searchPlaceholder = "Search users…",
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onFilterClick = { /* TODO: open filter dialog */ },
                showFilter = false
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    loading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading users…")
                        }
                    }

                    error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = "Error",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(error ?: "Unknown error")
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { viewModel.fetchUsers() }) {
                                Text("Try Again")
                            }
                        }
                    }

                    filteredUsers.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "No users found",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isEmpty()) "No users yet"
                                else "No users match your search",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try adjusting your search or filters.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 16.dp
                            )
                        ) {
                            items(
                                items = filteredUsers,
                                key = { it.id }
                            ) { user ->
                                UserCard(
                                    user = user,
                                    onEditClick = {
                                        // TODO: Handle edit
                                    },
                                    onDeleteClick = {
                                        viewModel.archiveUser(user.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Success Snackbar
        if (showSuccessSnackbar && archiveSuccess != null) {
            androidx.compose.material3.Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFF4CAF50)
            ) {
                Text(archiveSuccess ?: "", color = Color.White)
            }
        }

        // Error Snackbar
        if (showErrorSnackbar && archiveError != null) {
            androidx.compose.material3.Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Text(archiveError ?: "", color = Color.White)
            }
        }
    }
}