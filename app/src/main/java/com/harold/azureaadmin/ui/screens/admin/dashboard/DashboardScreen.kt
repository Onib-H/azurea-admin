package com.harold.azureaadmin.ui.screens.admin.dashboard

import android.content.Context
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.AlertDialog
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.harold.azureaadmin.ui.components.states.ErrorState
import com.harold.azureaadmin.utils.generateReportHTML
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {


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
    var showBlackout by remember { mutableStateOf(false) }
    var showMonthChangeSkeleton by remember { mutableStateOf(false) }
    var previousMonth by remember { mutableStateOf(selectedMonth) }
    var showDatePicker by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    val context = LocalContext.current

    // Initial load
    LaunchedEffect(Unit) {
        viewModel.fetchDataForSelectedMonth()
        viewModel.fetchDailyData()
    }

    // Detect month change and show skeleton
    LaunchedEffect(selectedMonth) {
        if (selectedMonth != previousMonth && previousMonth != null) {
            showMonthChangeSkeleton = true
            previousMonth = selectedMonth
        }
    }

    // Hide skeleton when data is loaded (independent check)
    LaunchedEffect(loading, stats) {
        // Only hide if we're showing skeleton AND not loading AND have stats
        if (showMonthChangeSkeleton && !loading && stats != null) {
            delay(300) // Brief delay to ensure smooth transition
            showMonthChangeSkeleton = false
        }
    }

    // Initial load check - don't show skeleton on first render if data exists
    LaunchedEffect(Unit) {
        if (stats != null) {
            showMonthChangeSkeleton = false
        }
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

                    // Blackout effect
                    showBlackout = true
                    delay(150) // Blackout duration
                    showBlackout = false
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
                DashboardSkeleton()
            }

            error != null && stats == null -> {
                ErrorState(error, { viewModel.fetchDataForSelectedMonth() })
            }

            stats != null -> {
                Box(modifier = Modifier.fillMaxSize()) {

                    // Show skeleton when month is changing
                    if (showMonthChangeSkeleton) {
                        DashboardSkeleton()
                    } else {
                        // Regular content with blackout overlay
                        AnimatedVisibility(
                            visible = !showBlackout,
                            enter = fadeIn(animationSpec = tween(200)),
                            exit = fadeOut(animationSpec = tween(150))
                        ) {
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
                                        bottom = 80.dp
                                    )
                                ) {

                                    item {
                                        MonthNavigationHeader(
                                            selectedMonth = selectedMonth,
                                            onMonthSelected = { viewModel.selectMonth(it) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White)
                                        )
                                    }

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
                                            BookingStatusPieChart(
                                                counts = bookingStatusCounts!!,
                                                selectedMonth = selectedMonth
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Blackout overlay
                        if (showBlackout) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                    }

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
        }

        // Report loading dialog
        if (showReportPreview && reportLoading) {
            Dialog(onDismissRequest = {}) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
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

                        tempWebView.stopLoading()
                    }
                }

                tempWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                showReportPreview = false
            }
        }
    }
}



