package com.harold.azureaadmin.ui.screens.admin.bookings

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.harold.azureaadmin.ui.components.filters.SearchFilterHeader
import com.harold.azureaadmin.ui.components.states.EmptyState
import com.harold.azureaadmin.ui.components.states.ErrorState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    viewModel: BookingViewModel = hiltViewModel()
) {

    val bookings by viewModel.bookings.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBookingId by remember { mutableStateOf<Int?>(null) }

    // Pull to refresh
    val pullState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var isRefreshInProgress by remember { mutableStateOf(false) }

    // Blackout overlay (same as dashboard)
    var showBlackout by remember { mutableStateOf(false) }

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

    val onRefresh = {
        if (!isRefreshInProgress) {
            scope.launch {
                isRefreshInProgress = true
                isRefreshing = true

                viewModel.fetchBookings()
                delay(300)

                // Same blackout pattern as dashboard
                showBlackout = true
                delay(150)
                showBlackout = false

                isRefreshing = false
                isRefreshInProgress = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        SearchFilterHeader(
            title = "Manage Bookings",
            searchPlaceholder = "Search bookings…",
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            onFilterClick = { },
            showFilter = false
        )

        Box(modifier = Modifier.fillMaxSize()) {

            androidx.compose.animation.AnimatedVisibility(
                visible = !showBlackout,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(150))
            ) {
                PullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    state = pullState,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh
                ) {
                    val showSkeleton = loading && bookings.isEmpty()

                    when {
                        showSkeleton -> BookingSkeleton()

                        error != null && bookings.isEmpty() ->
                            ErrorState(error, onRetry = { viewModel.fetchBookings() })

                        filteredBookings.isEmpty() ->
                            EmptyState(
                                icon = Icons.Outlined.CalendarMonth,
                                title = if (searchQuery.isEmpty()) "There are no bookings at the moment"
                                else "No bookings match your search",
                                subtitle = null,
                                useScroll = true
                            )


                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                            ) {
                                items(filteredBookings, key = { it.id }) { booking ->
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


    selectedBookingId?.let { bookingId ->
        BookingDetailsDialog(
            bookingId = bookingId,
            viewModel = viewModel,
            onDismiss = { selectedBookingId = null }
        )
    }
}



