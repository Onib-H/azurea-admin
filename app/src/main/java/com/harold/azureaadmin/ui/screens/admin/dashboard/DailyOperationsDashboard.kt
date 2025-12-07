package com.harold.azureaadmin.ui.screens.admin.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harold.azureaadmin.data.models.DailyBookingsResponse
import com.harold.azureaadmin.data.models.DailyCancellationsResponse
import com.harold.azureaadmin.data.models.DailyCheckInCheckoutResponse
import com.harold.azureaadmin.data.models.DailyNoShowRejectedResponse
import com.harold.azureaadmin.data.models.DailyRevenueResponse
import com.harold.azureaadmin.ui.theme.Gray500
import com.harold.azureaadmin.ui.theme.Green600
import com.harold.azureaadmin.ui.theme.Purple100
import com.harold.azureaadmin.ui.theme.Purple300
import com.harold.azureaadmin.ui.theme.Purple500
import com.harold.azureaadmin.ui.theme.Red500
import com.harold.azureaadmin.ui.theme.Red600
import com.harold.azureaadmin.utils.FormatPrice
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue


private object DashboardConstants {
    const val MILLIS_PER_DAY = 86400000L
    const val DEFAULT_DAYS_IN_MONTH = 30
    const val HEADER_ICON_SIZE = 40
    const val HEADER_ICON_INNER_SIZE = 20
    const val TREND_ICON_SIZE = 14
    const val METRIC_ICON_SIZE = 22
    const val METRIC_ICON_CONTAINER_SIZE = 38
}


data class MetricData(
    val label: String,
    val value: String,
    val trend: Pair<TrendType, String>? = null
)

enum class DailyMetricType {
    BOOKINGS,
    CANCELLATIONS,
    CHECKINS,
    CHECKOUTS,
    NOSHOWS,
    REJECTED
}

data class DailyMetric(
    val type: DailyMetricType,
    val label: String,
    val value: Int
)

private data class MonthTotals(
    val revenue: Double,
    val bookings: Int,
    val cancellations: Int,
    val checkIns: Int,
    val checkOuts: Int,
    val noShows: Int,
    val rejected: Int
) {
    fun isAllZero(): Boolean =
        revenue == 0.0 && bookings == 0 && cancellations == 0 &&
                checkIns == 0 && checkOuts == 0 && noShows == 0 && rejected == 0
}

private fun shouldShowTrend(trend: Pair<TrendType, String>?): Boolean {
    if (trend == null) return false
    // Don't show if neutral or if value is empty/zero
    if (trend.first == TrendType.NEUTRAL) return false
    val cleanValue = trend.second.replace("+", "").replace("-", "").replace("%", "").trim()
    val numValue = cleanValue.toDoubleOrNull() ?: 0.0
    return numValue != 0.0
}

private fun allTrendsEmpty(trends: DailyAnalyticsTrends?): Boolean {
    if (trends == null) return true

    val list = listOf(
        trends.revenueTrend,
        trends.bookingsTrend,
        trends.cancellationsTrend,
        trends.checkInsTrend,
        trends.checkOutsTrend,
        trends.noShowsTrend,
        trends.rejectedTrend
    )

    return list.all { trend ->
        trend == null || !shouldShowTrend(trend)
    }
}


@Composable
private fun getMetricStyle(type: DailyMetricType): Pair<ImageVector, Color> {
    return when (type) {
        DailyMetricType.BOOKINGS -> Icons.Filled.Event to Purple500
        DailyMetricType.CANCELLATIONS -> Icons.Filled.Close to Red500
        DailyMetricType.CHECKINS -> Icons.Filled.Login to Green600
        DailyMetricType.CHECKOUTS -> Icons.Filled.Logout to Green600
        DailyMetricType.NOSHOWS -> Icons.Filled.VisibilityOff to Gray500
        DailyMetricType.REJECTED -> Icons.Filled.Block to Red600
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyOperationsDashboard(
    selectedDate: LocalDate,
    viewMode: DailyViewMode,
    dailyRevenue: DailyRevenueResponse?,
    dailyBookings: DailyBookingsResponse?,
    dailyCancellations: DailyCancellationsResponse?,
    dailyCheckIns: DailyCheckInCheckoutResponse?,
    dailyNoShow: DailyNoShowRejectedResponse?,
    trends: DailyAnalyticsTrends?,
    onViewModeChange: (DailyViewMode) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showMonthYearPicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDay() * DashboardConstants.MILLIS_PER_DAY
    )

    // Check if data is actually loading (null means not fetched yet)
    val isLoading = dailyRevenue == null && dailyBookings == null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        DashboardHeader(
            selectedDate = selectedDate,
            viewMode = viewMode,
            onPreviousClick = onPreviousClick,
            onNextClick = onNextClick,
            onDateClick = {
                if (viewMode == DailyViewMode.MONTH) {
                    showMonthYearPicker = true
                } else {
                    showDatePicker = true
                }
            },
            onViewModeChange = onViewModeChange
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Show skeleton while data hasn't been fetched yet (null)
        if (isLoading) {
            DailyOperationsSkeleton(viewMode = viewMode)
        } else {
            AnimatedContent(
                targetState = viewMode,
                label = "view_mode_transition"
            ) { mode ->
                when (mode) {
                    DailyViewMode.DAY -> DayContent(
                        selectedDate = selectedDate,
                        revenue = dailyRevenue,
                        bookings = dailyBookings,
                        cancellations = dailyCancellations,
                        checkIns = dailyCheckIns,
                        noShow = dailyNoShow
                    )
                    DailyViewMode.MONTH -> MonthContent(
                        revenue = dailyRevenue,
                        bookings = dailyBookings,
                        cancellations = dailyCancellations,
                        checkIns = dailyCheckIns,
                        noShow = dailyNoShow,
                        trends = trends
                    )
                    else -> Unit
                }
            }
        }
    }

    // Date pickers remain the same...
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onDateSelected(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = Purple500, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White
            )
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true,
                title = {
                    Text(
                        text = "Select Date",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                headline = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Purple500,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    selectedDayContainerColor = Purple500,
                    selectedDayContentColor = Color.White,
                    todayContentColor = Purple500,
                    todayDateBorderColor = Purple500,
                    dayContentColor = Color.Black,
                    selectedYearContainerColor = Purple500,
                    selectedYearContentColor = Color.White,
                    currentYearContentColor = Purple500,
                    navigationContentColor = Purple500,
                    weekdayContentColor = Color.Gray,
                    subheadContentColor = Color.Black,
                    yearContentColor = Color.Black,
                    dividerColor = Color.LightGray
                )
            )
        }
    }

    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            initial = YearMonth.from(selectedDate),
            onDismiss = { showMonthYearPicker = false },
            onConfirm = { yearMonth ->
                onDateSelected(yearMonth.atDay(1))
                showMonthYearPicker = false
            }
        )
    }
}


@Composable
private fun DashboardHeader(
    selectedDate: LocalDate,
    viewMode: DailyViewMode,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onDateClick: () -> Unit,
    onViewModeChange: (DailyViewMode) -> Unit
) {
    val isFuture = when (viewMode) {
        DailyViewMode.DAY -> selectedDate >= LocalDate.now()
        DailyViewMode.MONTH -> YearMonth.from(selectedDate) >= YearMonth.now()
        else -> false
    }
    val datePattern = if (viewMode == DailyViewMode.DAY) "MMM d, yyyy" else "MMMM yyyy"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderTitle(
            viewMode = viewMode,
            onViewModeChange = onViewModeChange
        )

        DateNavigator(
            selectedDate = selectedDate,
            datePattern = datePattern,
            isFuture = isFuture,
            onPreviousClick = onPreviousClick,
            onNextClick = onNextClick,
            onDateClick = onDateClick
        )
    }
}

@Composable
private fun HeaderTitle(
    viewMode: DailyViewMode,
    onViewModeChange: (DailyViewMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(DashboardConstants.HEADER_ICON_SIZE.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Purple500, Purple300))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(DashboardConstants.HEADER_ICON_INNER_SIZE.dp)
                )
            }
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        ViewModeToggle(
            viewMode = viewMode,
            onViewModeChange = onViewModeChange
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ViewModeToggle(
    viewMode: DailyViewMode,
    onViewModeChange: (DailyViewMode) -> Unit
) {
    OutlinedButton(
        onClick = {
            onViewModeChange(
                if (viewMode == DailyViewMode.DAY) DailyViewMode.MONTH else DailyViewMode.DAY
            )
        },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple500),
        shape = RoundedCornerShape(8.dp)
    ) {
        AnimatedContent(
            targetState = viewMode,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            label = "viewModeSwap"
        ) { mode ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (mode == DailyViewMode.DAY)
                        Icons.Outlined.CalendarMonth else Icons.Filled.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (mode == DailyViewMode.DAY) "Monthly" else "Daily",
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Composable
private fun DateNavigator(
    selectedDate: LocalDate,
    datePattern: String,
    isFuture: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onDateClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Previous period",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern(datePattern)),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .clickable(onClick = onDateClick)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        IconButton(
            onClick = onNextClick,
            enabled = !isFuture
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Next period",
                tint = if (isFuture)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DayContent(
    selectedDate: LocalDate,
    revenue: DailyRevenueResponse?,
    bookings: DailyBookingsResponse?,
    cancellations: DailyCancellationsResponse?,
    checkIns: DailyCheckInCheckoutResponse?,
    noShow: DailyNoShowRejectedResponse?
) {
    val dayIndex = selectedDate.dayOfMonth - 1

    val dailyData = extractDailyData(
        dayIndex = dayIndex,
        revenue = revenue,
        bookings = bookings,
        cancellations = cancellations,
        checkIns = checkIns,
        noShow = noShow
    )

    if (dailyData.hasNoData()) {
        NoDataDisplay(viewMode = "day")
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (dailyData.revenueVal != 0.0) {
            RevenueCard(
                value = FormatPrice.formatRevenue(dailyData.revenueVal.toString()),
                label = "Today's Revenue",
                trend = null,
                contextText = null,
                showTrendIcon = false
            )
        }

        if (dailyData.metrics.isNotEmpty()) {
            DailyMetricGrid(dailyData.metrics)
        }
    }
}

@Composable
private fun MonthContent(
    revenue: DailyRevenueResponse?,
    bookings: DailyBookingsResponse?,
    cancellations: DailyCancellationsResponse?,
    checkIns: DailyCheckInCheckoutResponse?,
    noShow: DailyNoShowRejectedResponse?,
    trends: DailyAnalyticsTrends?
) {
    var showTotal by remember { mutableStateOf(true) }
    val daysInMonth = revenue?.days_in_month ?: DashboardConstants.DEFAULT_DAYS_IN_MONTH

    val totals = calculateMonthTotals(
        daysInMonth = daysInMonth,
        revenue = revenue,
        bookings = bookings,
        cancellations = cancellations,
        checkIns = checkIns,
        noShow = noShow
    )

    if (totals.isAllZero()) {
        NoDataDisplay(viewMode = "month")
        return
    }

    // 🔍 Determine if ANY trends actually have value
    val hasMeaningfulTrends = !allTrendsEmpty(trends)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RevenueCard(
            value = FormatPrice.formatRevenue(
                if (showTotal) totals.revenue.toString()
                else (totals.revenue / daysInMonth).toString()
            ),
            label = if (showTotal) "Monthly Revenue" else "Daily Average Revenue",
            trend = if (showTotal && hasMeaningfulTrends) trends?.revenueTrend else null,
            contextText = if (showTotal && hasMeaningfulTrends) "vs previous month" else null,
            showTrendIcon = showTotal && hasMeaningfulTrends
        )

        MetricViewToggle(
            showTotal = showTotal,
            onToggle = { showTotal = !showTotal }
        )

        MetricsSection(
            metrics = if (showTotal)
                createTotalMetrics(totals, trends)
            else createAverageMetrics(totals, daysInMonth),
            contextText = if (showTotal && hasMeaningfulTrends) "vs previous month" else null,
            showTrendIcons = showTotal && hasMeaningfulTrends
        )
    }
}


@Composable
private fun NoDataDisplay(viewMode: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(40.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No Data Available",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "No operations recorded for this $viewMode. Try selecting another date.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun RevenueCard(
    value: String,
    label: String,
    trend: Pair<TrendType, String>?,
    contextText: String?,
    showTrendIcon: Boolean
) {
    // Only show trend if it's meaningful (not neutral and not 0%)
    val displayTrend = showTrendIcon && shouldShowTrend(trend)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Purple500, Purple300)))
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Medium
                )

                if (displayTrend && trend != null) {
                    TrendBadge(trend = trend)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 36.sp
            )

            if (contextText != null && displayTrend && trend != null) {
                Text(
                    text = contextText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun TrendBadge(trend: Pair<TrendType, String>) {
    Surface(
        color = Color.White.copy(alpha = 0.25f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (trend.first == TrendType.UP)
                    Icons.AutoMirrored.Filled.TrendingUp
                else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(DashboardConstants.TREND_ICON_SIZE.dp)
            )

            Text(
                text = trend.second,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MetricViewToggle(
    showTotal: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Purple100,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showTotal) "Monthly Total" else "Daily Average",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Purple500
            )
            Icon(
                imageVector = if (showTotal)
                    Icons.Filled.KeyboardArrowDown
                else Icons.Filled.KeyboardArrowUp,
                contentDescription = "Toggle view",
                tint = Purple500
            )
        }
    }
}

@Composable
private fun MetricsSection(
    metrics: List<MetricData>,
    contextText: String?,
    showTrendIcons: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (contextText != null) {
            MetricsSectionHeader(contextText = contextText)
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp
        ) {
            Column {
                metrics.forEachIndexed { index, metric ->
                    MetricRow(
                        label = metric.label,
                        value = metric.value,
                        trend = metric.trend,
                        showTrendIcon = showTrendIcons
                    )
                    if (index < metrics.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricsSectionHeader(contextText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Performance Metrics",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = contextText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    trend: Pair<TrendType, String>?,
    showTrendIcon: Boolean
) {
    // Only show trend if it's meaningful (not neutral and not 0%)
    val displayTrend = showTrendIcon && shouldShowTrend(trend)

    val trendColor = when (trend?.first) {
        TrendType.UP -> Green600
        TrendType.DOWN -> Red600
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            )

            if (displayTrend && trend != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.width(60.dp)
                ) {
                    Icon(
                        imageVector = if (trend.first == TrendType.UP)
                            Icons.AutoMirrored.Filled.TrendingUp
                        else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(12.dp)
                    )

                    Text(
                        text = trend.second,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = trendColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@Composable
private fun DailyMetricGrid(metrics: List<DailyMetric>) {
    val columns = if (metrics.size == 1) 1 else 2

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 600.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(metrics.size) { index ->
            DailyMetricCard(metrics[index])
        }
    }
}

@Composable
private fun DailyMetricCard(metric: DailyMetric) {
    val (icon, color) = getMetricStyle(metric.type)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metric.value.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .size(DashboardConstants.METRIC_ICON_CONTAINER_SIZE.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(DashboardConstants.METRIC_ICON_SIZE.dp)
                    )
                }
            }

            Text(
                text = metric.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private data class DailyData(
    val revenueVal: Double,
    val metrics: List<DailyMetric>
) {
    fun hasNoData(): Boolean = revenueVal == 0.0 && metrics.isEmpty()
}

private fun extractDailyData(
    dayIndex: Int,
    revenue: DailyRevenueResponse?,
    bookings: DailyBookingsResponse?,
    cancellations: DailyCancellationsResponse?,
    checkIns: DailyCheckInCheckoutResponse?,
    noShow: DailyNoShowRejectedResponse?
): DailyData {
    val revenueVal = revenue?.data?.getOrNull(dayIndex) ?: 0.0
    val bookingsVal = bookings?.data?.getOrNull(dayIndex) ?: 0
    val cancellationsVal = cancellations?.data?.getOrNull(dayIndex) ?: 0
    val checkInsVal = checkIns?.checkins?.getOrNull(dayIndex) ?: 0
    val checkOutsVal = checkIns?.checkouts?.getOrNull(dayIndex) ?: 0
    val noShowsVal = noShow?.no_shows?.getOrNull(dayIndex) ?: 0
    val rejectedVal = noShow?.rejected?.getOrNull(dayIndex) ?: 0

    val metrics = listOfNotNull(
        DailyMetric(DailyMetricType.BOOKINGS, "Bookings", bookingsVal)
            .takeIf { bookingsVal != 0 },
        DailyMetric(DailyMetricType.CANCELLATIONS, "Cancellations", cancellationsVal)
            .takeIf { cancellationsVal != 0 },
        DailyMetric(DailyMetricType.CHECKINS, "Check-ins", checkInsVal)
            .takeIf { checkInsVal != 0 },
        DailyMetric(DailyMetricType.CHECKOUTS, "Check-outs", checkOutsVal)
            .takeIf { checkOutsVal != 0 },
        DailyMetric(DailyMetricType.NOSHOWS, "No-shows", noShowsVal)
            .takeIf { noShowsVal != 0 },
        DailyMetric(DailyMetricType.REJECTED, "Rejected", rejectedVal)
            .takeIf { rejectedVal != 0 }
    )

    return DailyData(revenueVal, metrics)
}

private fun calculateMonthTotals(
    daysInMonth: Int,
    revenue: DailyRevenueResponse?,
    bookings: DailyBookingsResponse?,
    cancellations: DailyCancellationsResponse?,
    checkIns: DailyCheckInCheckoutResponse?,
    noShow: DailyNoShowRejectedResponse?
): MonthTotals {
    return MonthTotals(
        revenue = revenue?.data?.take(daysInMonth)?.sum() ?: 0.0,
        bookings = bookings?.data?.take(daysInMonth)?.sum() ?: 0,
        cancellations = cancellations?.data?.take(daysInMonth)?.sum() ?: 0,
        checkIns = checkIns?.checkins?.take(daysInMonth)?.sum() ?: 0,
        checkOuts = checkIns?.checkouts?.take(daysInMonth)?.sum() ?: 0,
        noShows = noShow?.no_shows?.take(daysInMonth)?.sum() ?: 0,
        rejected = noShow?.rejected?.take(daysInMonth)?.sum() ?: 0
    )
}

private fun createTotalMetrics(
    totals: MonthTotals,
    trends: DailyAnalyticsTrends?
): List<MetricData> {
    return listOf(
        MetricData("Bookings", totals.bookings.toString(), trends?.bookingsTrend),
        MetricData("Cancellations", totals.cancellations.toString(), trends?.cancellationsTrend),
        MetricData("Check-ins", totals.checkIns.toString(), trends?.checkInsTrend),
        MetricData("Check-outs", totals.checkOuts.toString(), trends?.checkOutsTrend),
        MetricData("No-shows", totals.noShows.toString(), trends?.noShowsTrend),
        MetricData("Rejected", totals.rejected.toString(), trends?.rejectedTrend)
    )
}

private fun createAverageMetrics(
    totals: MonthTotals,
    daysInMonth: Int
): List<MetricData> {
    return listOf(
        MetricData("Bookings", "%.1f".format(totals.bookings.toDouble() / daysInMonth)),
        MetricData("Cancellations", "%.1f".format(totals.cancellations.toDouble() / daysInMonth)),
        MetricData("Check-ins", "%.1f".format(totals.checkIns.toDouble() / daysInMonth)),
        MetricData("Check-outs", "%.1f".format(totals.checkOuts.toDouble() / daysInMonth)),
        MetricData("No-shows", "%.1f".format(totals.noShows.toDouble() / daysInMonth)),
        MetricData("Rejected", "%.1f".format(totals.rejected.toDouble() / daysInMonth))
    )
}

private fun isZeroPercent(value: String): Boolean {
    val numStr = value.replace("+", "").replace("-", "").replace("%", "").trim()
    val numValue = numStr.toDoubleOrNull() ?: 0.0
    return numValue == 0.0 || numValue.absoluteValue < 0.5 // Consider < 0.5% as zero
}

@Composable
private fun DailyOperationsSkeleton(viewMode: DailyViewMode) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Revenue card skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        )

        if (viewMode == DailyViewMode.MONTH) {
            // Monthly toggle skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            )

            // Metrics section skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            )
        } else {
            // Daily metrics grid skeleton - First row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    )
                }
            }

            // Daily metrics grid skeleton - Second row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}