package com.example.azureaadmin.ui.screens.admin.archived_users

import ArchivedUsersViewModel
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
import androidx.compose.material.icons.outlined.PersonOff
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
fun ArchivedUsersScreen(
    repository: AdminRepository
) {
    val viewModel: ArchivedUsersViewModel = viewModel(
        factory = BaseViewModelFactory { ArchivedUsersViewModel(repository) }
    )
    val users by viewModel.users.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val restoreSuccess by viewModel.restoreSuccess.collectAsState()
    val restoreError by viewModel.restoreError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchArchivedUsers()
    }

    // Handle restore success/error messages
    LaunchedEffect(restoreSuccess) {
        if (restoreSuccess != null) {
            showSuccessSnackbar = true
            // Clear message after showing
            kotlinx.coroutines.delay(3000)
            viewModel.clearRestoreMessages()
            showSuccessSnackbar = false
        }
    }

    LaunchedEffect(restoreError) {
        if (restoreError != null) {
            showErrorSnackbar = true
            kotlinx.coroutines.delay(3000)
            viewModel.clearRestoreMessages()
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
                title = "Archived Users",
                searchPlaceholder = "Search archived users…",
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
                            Text("Loading archived users…")
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
                            Button(onClick = { viewModel.fetchArchivedUsers() }) {
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
                                imageVector = Icons.Outlined.PersonOff,
                                contentDescription = "No archived users found",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isEmpty()) "No archived users yet"
                                else "No archived users match your search",
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
                                ArchivedUserCard(
                                    user = user,
                                    onRestoreClick = {
                                        viewModel.restoreUser(user.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Success Snackbar
        if (showSuccessSnackbar && restoreSuccess != null) {
            androidx.compose.material3.Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFF4CAF50)
            ) {
                Text(restoreSuccess ?: "", color = Color.White)
            }
        }

        // Error Snackbar
        if (showErrorSnackbar && restoreError != null) {
            androidx.compose.material3.Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Text(restoreError ?: "", color = Color.White)
            }
        }
    }
}