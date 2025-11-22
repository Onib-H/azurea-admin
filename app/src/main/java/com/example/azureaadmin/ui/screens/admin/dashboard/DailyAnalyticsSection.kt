package com.example.azureaadmin.ui.screens.admin.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.azureaadmin.data.models.DailyBookingsResponse
import com.example.azureaadmin.data.models.DailyCancellationsResponse
import com.example.azureaadmin.data.models.DailyCheckInCheckoutResponse
import com.example.azureaadmin.data.models.DailyNoShowRejectedResponse
import com.example.azureaadmin.data.models.DailyRevenueResponse
import com.example.azureaadmin.utils.FormatPrice
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAnalyticsSection(
    selectedDate: LocalDate,
    viewMode: DailyViewMode,
    dailyRevenue: DailyRevenueResponse?,
    dailyBookings: DailyBookingsResponse?,
    dailyCancellations: DailyCancellationsResponse?,
    dailyCheckIns: DailyCheckInCheckoutResponse?,
    dailyNoShow: DailyNoShowRejectedResponse?,
    onViewModeChange: (DailyViewMode) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateFromPicker by remember { mutableStateOf(selectedDate) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Redesigned Header Section
        ModernAnalyticsHeader(
            selectedDate = selectedDate,
            viewMode = viewMode,
            onPreviousClick = onPreviousClick,
            onNextClick = onNextClick,
            onDateClick = { showDatePicker = true },
            onViewModeChange = onViewModeChange
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Content based on view mode
        AnimatedContent(
            targetState = viewMode,
            label = "daily_view_mode"
        ) { mode ->
            when (mode) {
                DailyViewMode.DAY -> HybridDayViewContent(
                    selectedDate = selectedDate,
                    dailyRevenue = dailyRevenue,
                    dailyBookings = dailyBookings,
                    dailyCancellations = dailyCancellations,
                    dailyCheckIns = dailyCheckIns,
                    dailyNoShow = dailyNoShow
                )
                DailyViewMode.MONTH -> HybridMonthViewContent(
                    selectedDate = selectedDate,
                    dailyRevenue = dailyRevenue,
                    dailyBookings = dailyBookings,
                    dailyCancellations = dailyCancellations,
                    dailyCheckIns = dailyCheckIns,
                    dailyNoShow = dailyNoShow
                )
                else -> {}
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = selectedDate,
            viewMode = viewMode,
            onDateSelected = { date ->
                onDateClick() // This will call selectDate in ViewModel
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun DatePickerDialog(
    selectedDate: LocalDate,
    viewMode: DailyViewMode,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (viewMode == DailyViewMode.DAY) "Select Date" else "Select Month",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                DatePicker(
                    state = rememberDatePickerState(
                        initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
                    ),
                    showModeToggle = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onDateSelected(selectedDate)
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernAnalyticsHeader(
    selectedDate: LocalDate,
    viewMode: DailyViewMode,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onDateClick: () -> Unit,
    onViewModeChange: (DailyViewMode) -> Unit
) {
    val isFutureDate = when (viewMode) {
        DailyViewMode.DAY -> selectedDate >= LocalDate.now()
        DailyViewMode.MONTH -> YearMonth.from(selectedDate) >= YearMonth.now()
        else -> false
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title and Monthly Button
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF7B2CBF),
                                    Color(0xFF9D4EDD)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Toggle between Daily and Monthly
            if (viewMode == DailyViewMode.DAY) {
                OutlinedButton(
                    onClick = { onViewModeChange(DailyViewMode.MONTH) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF7B2CBF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Monthly", fontSize = 14.sp)
                }
            } else {
                OutlinedButton(
                    onClick = { onViewModeChange(DailyViewMode.DAY) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF7B2CBF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Daily", fontSize = 14.sp)
                }
            }
        }

        // Date Display and Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Clickable Date
            val displayText = when (viewMode) {
                DailyViewMode.DAY -> selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                DailyViewMode.MONTH -> selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                else -> ""
            }

            Text(
                text = displayText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .clickable(onClick = onDateClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Next Button - Disabled for future dates
            IconButton(
                onClick = onNextClick,
                enabled = !isFutureDate
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next",
                    tint = if (isFutureDate)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun HybridDayViewContent(
    selectedDate: LocalDate,
    dailyRevenue: DailyRevenueResponse?,
    dailyBookings: DailyBookingsResponse?,
    dailyCancellations: DailyCancellationsResponse?,
    dailyCheckIns: DailyCheckInCheckoutResponse?,
    dailyNoShow: DailyNoShowRejectedResponse?
) {
    val dayIndex = selectedDate.dayOfMonth - 1

    val revenueValue = dailyRevenue?.data?.getOrNull(dayIndex)?.let {
        FormatPrice.formatRevenue(it.toString())
    } ?: "₱0.00"

    val bookingsValue = dailyBookings?.data?.getOrNull(dayIndex)?.toString() ?: "0"
    val cancellationsValue = dailyCancellations?.data?.getOrNull(dayIndex)?.toString() ?: "0"
    val checkInsValue = dailyCheckIns?.checkins?.getOrNull(dayIndex)?.toString() ?: "0"
    val checkOutsValue = dailyCheckIns?.checkouts?.getOrNull(dayIndex)?.toString() ?: "0"
    val noShowsValue = dailyNoShow?.no_shows?.getOrNull(dayIndex)?.toString() ?: "0"
    val rejectedValue = dailyNoShow?.rejected?.getOrNull(dayIndex)?.toString() ?: "0"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FeaturedRevenueCard(value = revenueValue, label = "Today's Revenue")

        CompactMetricsList(
            metrics = listOf(
                MetricItem("Bookings", bookingsValue),
                MetricItem("Cancellations", cancellationsValue),
                MetricItem("Check-ins", checkInsValue),
                MetricItem("Check-outs", checkOutsValue),
                MetricItem("No-shows", noShowsValue),
                MetricItem("Rejected", rejectedValue)
            )
        )
    }
}

@Composable
private fun HybridMonthViewContent(
    selectedDate: LocalDate,
    dailyRevenue: DailyRevenueResponse?,
    dailyBookings: DailyBookingsResponse?,
    dailyCancellations: DailyCancellationsResponse?,
    dailyCheckIns: DailyCheckInCheckoutResponse?,
    dailyNoShow: DailyNoShowRejectedResponse?
) {
    var showMonthlyTotal by remember { mutableStateOf(true) }

    val monthRevenue = dailyRevenue?.data?.take(dailyRevenue.days_in_month)?.sum() ?: 0.0
    val monthBookings = dailyBookings?.data?.take(dailyBookings.days_in_month)?.sum() ?: 0
    val monthCancellations = dailyCancellations?.data?.take(dailyCancellations.days_in_month)?.sum() ?: 0
    val monthCheckIns = dailyCheckIns?.checkins?.take(dailyCheckIns.days_in_month)?.sum() ?: 0
    val monthCheckOuts = dailyCheckIns?.checkouts?.take(dailyCheckIns.days_in_month)?.sum() ?: 0
    val monthNoShows = dailyNoShow?.no_shows?.take(dailyNoShow.days_in_month)?.sum() ?: 0
    val monthRejected = dailyNoShow?.rejected?.take(dailyNoShow.days_in_month)?.sum() ?: 0

    val daysInMonth = dailyRevenue?.days_in_month ?: 30
    val avgDailyRevenue = monthRevenue / daysInMonth
    val avgDailyBookings = monthBookings.toDouble() / daysInMonth
    val avgDailyCancellations = monthCancellations.toDouble() / daysInMonth
    val avgDailyCheckIns = monthCheckIns.toDouble() / daysInMonth
    val avgDailyCheckOuts = monthCheckOuts.toDouble() / daysInMonth
    val avgDailyNoShows = monthNoShows.toDouble() / daysInMonth
    val avgDailyRejected = monthRejected.toDouble() / daysInMonth

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Featured Revenue Card
        FeaturedRevenueCard(
            value = if (showMonthlyTotal)
                FormatPrice.formatRevenue(monthRevenue.toString())
            else
                FormatPrice.formatRevenue(avgDailyRevenue.toString()),
            label = if (showMonthlyTotal) "Monthly Revenue" else "Avg Daily Revenue"
        )

        // Toggle Tab
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF3E5F5),
            onClick = { showMonthlyTotal = !showMonthlyTotal }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showMonthlyTotal) "Monthly Total" else "Daily Average",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B2CBF)
                )

                Icon(
                    imageVector = if (showMonthlyTotal) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = "Toggle",
                    tint = Color(0xFF7B2CBF)
                )
            }
        }

        // Metrics based on toggle
        AnimatedContent(targetState = showMonthlyTotal, label = "metrics_toggle") { isTotal ->
            if (isTotal) {
                CompactMetricsList(
                    metrics = listOf(
                        MetricItem("Bookings", monthBookings.toString()),
                        MetricItem("Cancellations", monthCancellations.toString()),
                        MetricItem("Check-ins", monthCheckIns.toString()),
                        MetricItem("Check-outs", monthCheckOuts.toString()),
                        MetricItem("No-shows", monthNoShows.toString()),
                        MetricItem("Rejected", monthRejected.toString())
                    )
                )
            } else {
                CompactMetricsList(
                    metrics = listOf(
                        MetricItem("Bookings", String.format("%.1f", avgDailyBookings)),
                        MetricItem("Cancellations", String.format("%.1f", avgDailyCancellations)),
                        MetricItem("Check-ins", String.format("%.1f", avgDailyCheckIns)),
                        MetricItem("Check-outs", String.format("%.1f", avgDailyCheckOuts)),
                        MetricItem("No-shows", String.format("%.1f", avgDailyNoShows)),
                        MetricItem("Rejected", String.format("%.1f", avgDailyRejected))
                    )
                )
            }
        }
    }
}

@Composable
private fun FeaturedRevenueCard(value: String, label: String = "Revenue") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF7B2CBF),
                        Color(0xFF9D4EDD)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 36.sp
            )
        }
    }
}

data class MetricItem(val label: String, val value: String)

@Composable
private fun CompactMetricsList(metrics: List<MetricItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column {
            metrics.forEachIndexed { index, metric ->
                CompactMetricRow(label = metric.label, value = metric.value)
                if (index < metrics.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}