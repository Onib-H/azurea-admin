package com.harold.azureaadmin.ui.screens.admin.archived_users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harold.azureaadmin.ui.components.common.ListScreenContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ArchivedUsersScreen(
    viewModel: ArchivedUsersViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val restoreSuccess by viewModel.restoreSuccess.collectAsState()
    val restoreError by viewModel.restoreError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshLock by remember { mutableStateOf(false) }
    var showBlackout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchArchivedUsers() }

    LaunchedEffect(restoreSuccess) {
        if (restoreSuccess != null) {
            showSuccess = true
            delay(3000)
            viewModel.clearRestoreMessages()
            showSuccess = false
        }
    }

    LaunchedEffect(restoreError) {
        if (restoreError != null) {
            showError = true
            delay(3000)
            viewModel.clearRestoreMessages()
            showError = false
        }
    }

    val filtered =
        if (searchQuery.isBlank()) users else users.filter {
            it.first_name.contains(searchQuery, true) ||
                    it.last_name.contains(searchQuery, true) ||
                    it.email.contains(searchQuery, true)
        }

    val onRefresh = {
        if (!refreshLock) {
            scope.launch {
                refreshLock = true
                isRefreshing = true

                viewModel.fetchArchivedUsers()
                delay(300)

                showBlackout = true
                delay(150)
                showBlackout = false

                isRefreshing = false
                refreshLock = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        ListScreenContainer(
            title = "Archived Users",
            searchPlaceholder = "Search archived users…",
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            showFilter = false,
            onFilterClick = {  },
            showNotification = false,

            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            showBlackout = showBlackout,

            loading = loading,
            error = error,
            items = filtered,

            emptyIcon = Icons.Outlined.PersonOff,
            emptyTitle = if (searchQuery.isEmpty()) "No archived users yet" else "No results match",
            emptySubtitle = null,

            skeleton = { ArchivedUserSkeleton() }
        ) { list ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                items(list, key = { it.id }) { user ->
                    ArchivedUserCard(
                        user = user,
                        onRestoreClick = { viewModel.restoreUser(user.id) }
                    )
                }
            }
        }

        if (showSuccess && restoreSuccess != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFF4CAF50)
            ) { Text(restoreSuccess!!, color = Color.White) }
        }

        if (showError && restoreError != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) { Text(restoreError!!, color = Color.White) }
        }
    }
}
