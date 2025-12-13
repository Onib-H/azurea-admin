package com.harold.azureaadmin.ui.screens.admin.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harold.azureaadmin.ui.components.common.ListScreenContainer
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
    var snackbarMessage by remember { mutableStateOf("") }
    var showSnackbarSuccess by remember { mutableStateOf(false) }
    var showSnackbarError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var refreshLock by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showBlackout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchUsers() }

    fun triggerSnackbar(msg: String, success: Boolean) {
        snackbarMessage = msg
        if (success) showSnackbarSuccess = true else showSnackbarError = true
        scope.launch {
            delay(3000)
            showSnackbarSuccess = false
            showSnackbarError = false
        }
    }

    LaunchedEffect(archiveSuccess) {
        archiveSuccess?.let {
            triggerSnackbar(it, true)
            viewModel.clearArchiveMessages()
        }
    }
    LaunchedEffect(archiveError) {
        archiveError?.let {
            triggerSnackbar(it, false)
            viewModel.clearArchiveMessages()
        }
    }
    LaunchedEffect(verificationSuccess) {
        verificationSuccess?.let {
            triggerSnackbar(it, true)
            viewModel.clearVerificationMessages()
        }
    }
    LaunchedEffect(verificationError) {
        verificationError?.let {
            triggerSnackbar(it, false)
            viewModel.clearVerificationMessages()
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

                viewModel.fetchUsers()
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
            title = "Manage Users",
            searchPlaceholder = "Search users…",
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

            emptyIcon = Icons.Outlined.Person,
            emptyTitle = if (searchQuery.isEmpty()) "No users yet" else "No users match",

            skeleton = { UserSkeleton() }
        ) { list ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                items(list, key = { it.id }) { user ->
                    UserCard(
                        user = user,
                        onDeleteClick = { viewModel.archiveUser(user.id) },
                        onApproveId = { id, applyDiscount ->
                            viewModel.approveUserId(id, applyDiscount)
                        },
                        onRejectId = { id, reason ->
                            viewModel.rejectUserId(id, reason)
                        }
                    )
                }
            }
        }

        if (showSnackbarSuccess) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = Color(0xFF4CAF50)
            ) { Text(snackbarMessage, color = Color.White) }
        }

        if (showSnackbarError) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) { Text(snackbarMessage, color = Color.White) }
        }
    }
}
