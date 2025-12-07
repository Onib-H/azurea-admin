package com.harold.azureaadmin.ui.screens.admin.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harold.azureaadmin.ui.components.common.ListScreenContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BookingsScreen(
    viewModel: BookingViewModel = hiltViewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBookingId by remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()
    var refreshLock by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showBlackout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchBookings() }

    val filtered = if (searchQuery.isBlank()) {
        bookings
    } else {
        val q = searchQuery.lowercase()

        bookings.filter { booking ->
            val user = booking.user
            val roomName = booking.room_details?.room_name
            val areaName = booking.area_details?.area_name

            listOf(
                user.username,
                user.email,
                user.first_name,
                user.last_name,
                "$${user.first_name} ${user.last_name}",
                roomName,
                areaName
            ).any { field ->
                field?.lowercase()?.contains(q) == true
            }
        }
    }


    val onRefresh = {
        if (!refreshLock) {
            scope.launch {
                refreshLock = true
                isRefreshing = true

                viewModel.fetchBookings()
                delay(300)

                showBlackout = true
                delay(150)
                showBlackout = false

                isRefreshing = false
                refreshLock = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        ListScreenContainer(
            title = "Manage Bookings",
            searchPlaceholder = "Search bookings…",
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            showFilter = false,
            onFilterClick = {  },

            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            showBlackout = showBlackout,

            loading = loading,
            error = error,
            items = filtered,

            emptyIcon = Icons.Outlined.CalendarMonth,
            emptyTitle = if (searchQuery.isEmpty()) "No bookings at the moment" else "No results match",

            skeleton = { BookingSkeleton() }
        ) { list ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(list, key = { it.id }) { booking ->
                    BookingCard(
                        booking = booking,
                        onViewClick = { selectedBookingId = booking.id }
                    )
                }
            }
        }
    }

    selectedBookingId?.let { id ->
        BookingDetailsDialog(
            bookingId = id,
            viewModel = viewModel,
            onDismiss = { selectedBookingId = null }
        )
    }
}
