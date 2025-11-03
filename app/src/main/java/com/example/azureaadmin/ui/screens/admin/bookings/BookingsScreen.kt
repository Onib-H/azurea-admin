package com.example.azureaadmin.ui.screens.admin.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ErrorOutline
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.azureaadmin.data.repository.AdminRepository
import com.example.azureaadmin.ui.components.SearchFilterHeader
import com.example.azureaadmin.utils.BaseViewModelFactory

@Composable
fun BookingsScreen(
    repository: AdminRepository
) {
    val viewModel: BookingViewModel =
        viewModel(factory = BaseViewModelFactory { BookingViewModel(repository) })
    val bookings by viewModel.bookings.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBookingId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchBookings()
    }

    val filteredBookings = if (searchQuery.isBlank()) {
        bookings
    } else {
        bookings.filter {
            it.user.username.contains(searchQuery, ignoreCase = true) ||
                    it.user.email.contains(searchQuery, ignoreCase = true) ||
                    it.user.first_name.contains(searchQuery, ignoreCase = true) ||
                    it.user.last_name.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchFilterHeader(
            title = "Manage Bookings",
            searchPlaceholder = "Search bookings…",
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            onFilterClick = { /* optional later */ },
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                loading && bookings.isEmpty() -> LoadingState()
                error != null && bookings.isEmpty() -> ErrorState(error, onRetry = { viewModel.fetchBookings() })
                filteredBookings.isEmpty() -> EmptyState(searchQuery)
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                    ) {
                        items(
                            items = filteredBookings,
                            key = { it.id }
                        ) { booking ->
                            BookingCard(
                                booking = booking,
                                onViewClick = { selectedBookingId = booking.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // Show dialog when booking is selected
    selectedBookingId?.let { bookingId ->
        BookingDetailsDialog(
            bookingId = bookingId,
            viewModel = viewModel,
            onDismiss = { selectedBookingId = null }
        )
    }
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp), strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Loading bookings…")
    }
}

@Composable
fun ErrorState(error: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(error ?: "Something went wrong")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Try Again") }
    }
}

@Composable
fun EmptyState(searchQuery: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (searchQuery.isEmpty()) "No bookings found"
            else "No bookings match your search",
            style = MaterialTheme.typography.titleMedium
        )
    }
}