package com.example.azureaadmin.ui.screens.admin.dashboard

import android.content.Context
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.azureaadmin.data.repository.AdminRepository
import com.example.azureaadmin.utils.BaseViewModelFactory
import com.example.azureaadmin.utils.generateReportHTML
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

    // Room and Area Revenue
    val roomRevenue by viewModel.roomRevenue.collectAsState()
    val roomBookings by viewModel.roomBookings.collectAsState()
    val areaRevenue by viewModel.areaRevenue.collectAsState()
    val areaBookings by viewModel.areaBookings.collectAsState()

    // Daily data
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailyViewMode by viewModel.dailyViewMode.collectAsState()
    val dailyRevenue by viewModel.dailyRevenue.collectAsState()
    val dailyBookings by viewModel.dailyBookings.collectAsState()
    val dailyCancellations by viewModel.dailyCancellations.collectAsState()
    val dailyCheckIns by viewModel.dailyCheckInsCheckOuts.collectAsState()
    val dailyNoShow by viewModel.dailyNoShowRejected.collectAsState()
    val dailyAnalyticsTrends by viewModel.dailyAnalyticsTrends.collectAsState()

    // Report states
    val monthlyReport by viewModel.monthlyReport.collectAsState()
    val reportLoading by viewModel.reportLoading.collectAsState()
    val reportError by viewModel.reportError.collectAsState()
    var showReportPreview by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var isRefreshInProgress by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    val context = LocalContext.current

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
                                .padding(bottom = 16.dp),
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 80.dp
                            )
                        ) {
                            item { Spacer(modifier = Modifier.height(60.dp)) }

                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    if (statsWithTrends.isNotEmpty()) {
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

                            item {
                                DailyOperationsDashboard(
                                    selectedDate = selectedDate,
                                    viewMode = dailyViewMode,
                                    dailyRevenue = dailyRevenue,
                                    dailyBookings = dailyBookings,
                                    dailyCancellations = dailyCancellations,
                                    dailyCheckIns = dailyCheckIns,
                                    dailyNoShow = dailyNoShow,
                                    trends = dailyAnalyticsTrends,
                                    onViewModeChange = { viewModel.setDailyViewMode(it) },
                                    onPreviousClick = { viewModel.navigateDailyPrevious() },
                                    onNextClick = { viewModel.navigateDailyNext() },
                                    onDateSelected = { viewModel.selectDate(it) }
                                )
                            }

                            item {
                                RankedRevenueDashboard(
                                    roomRevenue = roomRevenue,
                                    roomBookings = roomBookings,
                                    areaRevenue = areaRevenue,
                                    areaBookings = areaBookings
                                )
                            }

                            if (bookingStatusCounts != null && bookingStatusCounts!!.isNotEmpty()) {
                                item {
                                    BookingStatusPieChartComposeCharts(
                                        counts = bookingStatusCounts!!,
                                        selectedMonth = selectedMonth
                                    )
                                }
                            }
                        }
                    }

                    // Sticky header
                    MonthNavigationHeader(
                        selectedMonth = selectedMonth,
                        onMonthSelected = { viewModel.selectMonth(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .zIndex(10f)
                            .align(Alignment.TopCenter)
                    )

                    // FAB for report
                    FloatingActionButton(
                        onClick = {
                            viewModel.fetchMonthlyReport()
                            showReportPreview = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 24.dp)
                            .size(60.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "Download Report",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
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

        // Report loading dialog
        if (showReportPreview && reportLoading) {
            Dialog(onDismissRequest = {}) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Generating report...")
                    }
                }
            }
        }

        // Report error dialog
        if (showReportPreview && reportError != null) {
            AlertDialog(
                onDismissRequest = {
                    showReportPreview = false
                    viewModel.clearReportError()
                },
                title = { Text("Error") },
                text = { Text(reportError ?: "Unknown error") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showReportPreview = false
                            viewModel.clearReportError()
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        LaunchedEffect(monthlyReport, reportLoading) {
            if (showReportPreview && monthlyReport != null && !reportLoading && reportError == null) {

                val html = generateReportHTML(monthlyReport!!)

                // Create WebView properly
                val tempWebView = WebView(context).apply {
                    settings.javaScriptEnabled = false
                    settings.domStorageEnabled = true
                }

                tempWebView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                        val printAdapter = tempWebView.createPrintDocumentAdapter("Report_${monthlyReport!!.period}")

                        printManager.print(
                            "Monthly Report - ${monthlyReport!!.period}",
                            printAdapter,
                            null
                        )

                        // Avoid multiple triggers
                        tempWebView.stopLoading()
                    }
                }

                // Load HTML
                tempWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)

                // Hide preview (your design choice)
                showReportPreview = false
            }
        }

    }
}