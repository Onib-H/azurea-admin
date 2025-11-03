package com.example.azureaadmin.ui.screens.admin.dashboard

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.azureaadmin.data.models.BookingStatusCounts
import com.example.azureaadmin.data.repository.AdminRepository
import com.example.azureaadmin.utils.BaseViewModelFactory
import com.example.azureaadmin.utils.FormatPrice
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(repository: AdminRepository) {
    val viewModel: DashboardViewModel =
        viewModel(factory = BaseViewModelFactory { DashboardViewModel(repository) })

    val stats by viewModel.stats.collectAsState()
    val bookingStatusCounts by viewModel.bookingStatusCounts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope()

    // local refreshing flag shown in the pull-to-refresh indicator
    var isRefreshing by remember { mutableStateOf(false) }

    // flag to prevent overlapping refresh operations
    var isRefreshInProgress by remember { mutableStateOf(false) }

    // create the PullToRefresh state object (parameterless factory)
    val pullState = rememberPullToRefreshState()

    // initial load only
    LaunchedEffect(Unit) {
        viewModel.fetchStats()
        delay(300)
        viewModel.fetchBookingStatusCounts()
    }

    // handle pull-to-refresh action
    val onRefresh: () -> Unit = {
        if (!isRefreshInProgress) {
            scope.launch {
                isRefreshInProgress = true
                isRefreshing = true
                try {
                    viewModel.fetchStats()
                    delay(300)
                    viewModel.fetchBookingStatusCounts()
                    delay(600) // small delay so users see the indicator
                } finally {
                    isRefreshing = false
                    isRefreshInProgress = false
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading dashboard data…")
                    }
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(error ?: "Unknown error")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.fetchStats()
                            viewModel.fetchBookingStatusCounts()
                        }) {
                            Text("Try Again")
                        }
                    }
                }
            }

            stats != null -> {
                // PullToRefreshBox requires explicit isRefreshing and onRefresh parameters
                PullToRefreshBox(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp)
                    ) {
                        // Header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Admin Dashboard",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        // Stats Cards
                        item {
                            val items = listOf(
                                "Active Bookings" to stats!!.active_bookings.toString(),
                                "Pending Bookings" to stats!!.pending_bookings.toString(),
                                "Total Bookings" to stats!!.total_bookings.toString(),
                                "Monthly Revenue" to FormatPrice.formatRevenue(stats!!.formatted_revenue.toString()),
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                items.chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { (label, value) ->
                                            StatCard(
                                                label = label,
                                                value = value,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        // Booking Pie Chart
                        if (bookingStatusCounts != null && bookingStatusCounts!!.isNotEmpty()) {
                            item {
                                BookingStatusPieChartComposeCharts(counts = bookingStatusCounts!!)
                            }
                        }
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data available")
                }
            }
        }
    }
}

