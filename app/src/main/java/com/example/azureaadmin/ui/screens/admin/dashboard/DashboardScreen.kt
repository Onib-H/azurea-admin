package com.example.azureaadmin.ui.screens.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.azureaadmin.utils.BaseViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(repository: AdminRepository) {
    val viewModel: DashboardViewModel =
        viewModel(factory = BaseViewModelFactory { DashboardViewModel(repository) })

    val stats by viewModel.stats.collectAsState()
    val statsWithTrends by viewModel.statsWithTrends.collectAsState()
    val bookingStatusCounts by viewModel.bookingStatusCounts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Monthly data
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    // Daily data
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailyViewMode by viewModel.dailyViewMode.collectAsState()
    val dailyRevenue by viewModel.dailyRevenue.collectAsState()
    val dailyBookings by viewModel.dailyBookings.collectAsState()
    val dailyCancellations by viewModel.dailyCancellations.collectAsState()
    val dailyCheckIns by viewModel.dailyCheckInsCheckOuts.collectAsState()
    val dailyNoShow by viewModel.dailyNoShowRejected.collectAsState()

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var isRefreshInProgress by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    // Initial load
    LaunchedEffect(Unit) {
        viewModel.fetchDataForSelectedMonth()
        viewModel.fetchDailyData()
    }

    val onRefresh: () -> Unit = {
        if (!isRefreshInProgress) {
            scope.launch {
                isRefreshInProgress = true
                isRefreshing = true
                try {
                    viewModel.fetchDataForSelectedMonth()
                    delay(300)
                    viewModel.fetchDailyData()
                    delay(600)
                } finally {
                    isRefreshing = false
                    isRefreshInProgress = false
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading && stats == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading dashboard data…")
                    }
                }
            }

            error != null && stats == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(error ?: "Unknown error")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchDataForSelectedMonth() }) {
                            Text("Try Again")
                        }
                    }
                }
            }

            stats != null -> {
                Box(modifier = Modifier.fillMaxSize()) {
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
                            // Add spacer for sticky header height
                            item {
                                Spacer(modifier = Modifier.height(60.dp))
                            }

                            /// Stats Cards with Trends
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    if (statsWithTrends.isNotEmpty()) {
                                        // Use stats with calculated trends
                                        statsWithTrends.chunked(2).forEach { rowItems ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                rowItems.forEach { stat ->
                                                    StatCardWithTrend(
                                                        label = stat.label,
                                                        value = stat.value,
                                                        trendType = stat.trend,
                                                        trendValue = stat.trendValue,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                if (rowItems.size == 1) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    } else {
                                        // Fallback to simple cards while trends are loading
                                        val items = listOf(
                                            "Active Bookings" to stats!!.active_bookings.toString(),
                                            "Pending Bookings" to stats!!.pending_bookings.toString(),
                                            "Total Bookings" to stats!!.total_bookings.toString(),
                                            "Monthly Revenue" to stats!!.formatted_revenue
                                        )

                                        items.chunked(2).forEach { rowItems ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                rowItems.forEach { (label, value) ->
                                                    SimpleStatCard(
                                                        label = label,
                                                        value = value,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                if (rowItems.size == 1) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }



                            // Booking Pie Chart
                            if (bookingStatusCounts != null && bookingStatusCounts!!.isNotEmpty()) {
                                item {
                                    BookingStatusPieChartComposeCharts(
                                        counts = bookingStatusCounts!!,
                                        selectedMonth = selectedMonth
                                    )
                                }
                            }

                            // Daily Analytics Section
                            item {
                                DailyAnalyticsSection(
                                    selectedDate = selectedDate,
                                    viewMode = dailyViewMode,
                                    dailyRevenue = dailyRevenue,
                                    dailyBookings = dailyBookings,
                                    dailyCancellations = dailyCancellations,
                                    dailyCheckIns = dailyCheckIns,
                                    dailyNoShow = dailyNoShow,
                                    onViewModeChange = { viewModel.setDailyViewMode(it) },
                                    onPreviousClick = { viewModel.navigateDailyPrevious() },
                                    onNextClick = { viewModel.navigateDailyNext() },
                                    onDateClick = { showDatePicker = true }
                                )
                            }
                        }
                    }

                    // Sticky Header at the top
                    MonthNavigationHeader(
                        selectedMonth = selectedMonth,
                        onMonthSelected = { viewModel.selectMonth(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .zIndex(10f) // Ensures it stays on top
                            .align(Alignment.TopCenter)
                    )
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