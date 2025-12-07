package com.harold.azureaadmin.ui.screens.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.harold.azureaadmin.ui.components.filters.SearchFilterHeader
import com.harold.azureaadmin.ui.components.states.EmptyState
import com.harold.azureaadmin.ui.components.states.ErrorState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UsersScreen(
    viewModel: UsersViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val archiveSuccess by viewModel.archiveSuccess.collectAsState()
    val archiveError by viewModel.archiveError.collectAsState()
    val verificationSuccess by viewModel.verificationSuccess.collectAsState()
    val verificationError by viewModel.verificationError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()
    var isRefreshInProgress by remember { mutableStateOf(false) }
    var showBlackout by remember { mutableStateOf(false) }

// Update onRefresh:
    val onRefresh = {
        if (!isRefreshInProgress) {
            scope.launch {
                isRefreshInProgress = true
                isRefreshing = true

                viewModel.fetchUsers()
                delay(300)

                showBlackout = true
                delay(150)
                showBlackout = false

                isRefreshing = false
                isRefreshInProgress = false
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    // Handle archive messages
    LaunchedEffect(archiveSuccess) {
        if (archiveSuccess != null) {
            snackbarMessage = archiveSuccess ?: ""
            showSuccessSnackbar = true
            kotlinx.coroutines.delay(3000)
            viewModel.clearArchiveMessages()
            showSuccessSnackbar = false
        }
    }

    LaunchedEffect(archiveError) {
        if (archiveError != null) {
            snackbarMessage = archiveError ?: ""
            showErrorSnackbar = true
            kotlinx.coroutines.delay(3000)
            viewModel.clearArchiveMessages()
            showErrorSnackbar = false
        }
    }

    // Handle verification messages
    LaunchedEffect(verificationSuccess) {
        if (verificationSuccess != null) {
            snackbarMessage = verificationSuccess ?: ""
            showSuccessSnackbar = true
            kotlinx.coroutines.delay(3000)
            viewModel.clearVerificationMessages()
            showSuccessSnackbar = false
        }
    }

    LaunchedEffect(verificationError) {
        if (verificationError != null) {
            snackbarMessage = verificationError ?: ""
            showErrorSnackbar = true
            kotlinx.coroutines.delay(3000)
            viewModel.clearVerificationMessages()
            showErrorSnackbar = false
        }
    }

    // Filtered users
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
        Column(modifier = Modifier.fillMaxSize()) {
            SearchFilterHeader(
                title = "Manage Users",
                searchPlaceholder = "Search users…",
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onFilterClick = { },
                showFilter = false
            )

            Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !showBlackout,
                    enter = androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(
                            200
                        )
                    ),
                    exit = androidx.compose.animation.fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(
                            150
                        )
                    )
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
                                    UserSkeleton()
                                }

                                error != null -> {
                                    ErrorState(error, { viewModel.fetchUsers() })
                                }

                                filteredUsers.isEmpty() -> {
                                    EmptyState(
                                        icon = Icons.Outlined.Person,
                                        title = if (searchQuery.isEmpty()) "No users yet"
                                        else "No users match your search",
                                        useScroll = true
                                    )

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
                                                onDeleteClick = {
                                                    viewModel.archiveUser(user.id)
                                                },
                                                onApproveId = { userId, applyDiscount ->
                                                    viewModel.approveUserId(userId, applyDiscount)
                                                },
                                                onRejectId = { userId, reason ->
                                                    viewModel.rejectUserId(userId, reason)
                                                }
                                            )
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

                if (showSuccessSnackbar) {
                    androidx.compose.material3.Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Text(snackbarMessage, color = Color.White)
                    }
                }

                if (showErrorSnackbar) {
                    androidx.compose.material3.Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(snackbarMessage, color = Color.White)
                    }
                }
            }
        }
    }
}