package com.example.azureaadmin.ui.screens.admin.archived_users

import ArchivedUsersViewModel
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.azureaadmin.data.repository.AdminRepository
import com.example.azureaadmin.ui.components.filters.SearchFilterHeader
import com.example.azureaadmin.utils.BaseViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    // Pull-to-refresh states
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var isRefreshInProgress by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    // Blackout overlay
    var showBlackout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchArchivedUsers()
    }

    // Handle restore messages
    LaunchedEffect(restoreSuccess) {
        if (restoreSuccess != null) {
            showSuccessSnackbar = true
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

    val filteredUsers = if (searchQuery.isBlank()) {
        users
    } else {
        users.filter {
            it.first_name.contains(searchQuery, ignoreCase = true) ||
                    it.last_name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    val onRefresh = {
        if (!isRefreshInProgress) {
            scope.launch {
                isRefreshInProgress = true
                isRefreshing = true

                viewModel.fetchArchivedUsers()
                delay(300)

                showBlackout = true
                delay(150)
                showBlackout = false

                isRefreshing = false
                isRefreshInProgress = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchFilterHeader(
                title = "Archived Users",
                searchPlaceholder = "Search archived users…",
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onFilterClick = { },
                showFilter = false
            )

            Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !showBlackout,
                    enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(200)),
                    exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
                ) {
                    PullToRefreshBox(
                        state = pullState,
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                loading && users.isEmpty() -> {
                                    ArchivedUserSkeleton()
                                }

                                error != null && users.isEmpty() -> {
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
                }

                if (showBlackout) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
                            .zIndex(10f)
                    )
                }
            }
        }

        // Snackbars
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