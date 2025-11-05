package com.example.azureaadmin.ui.screens.admin.dashboard

import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azureaadmin.data.models.BookingStatusCounts
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun BookingStatusPieChartComposeCharts(counts: List<BookingStatusCounts>) {
    val statusColorMap = mapOf(
        "Pending" to Color(0xFFFF6B6B),
        "Reserved" to Color(0xFFFFD93D),
        "Checked In" to Color(0xFF45B7D1),
        "Checked Out" to Color(0xFF96CEB4),
        "Cancelled" to Color(0xFF4ECDC4),
        "No Show" to Color(0xFFA8A8A8),
        "Rejected" to Color(0xFFFF8C42)
    )

    val nonZeroCounts = counts.filter { it.count > 0 }
    val total = counts.sumOf { it.count }

    var selectedSlice by remember { mutableStateOf<BookingStatusCounts?>(null) }
    val legendListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Booking Status Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "November 2025",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (nonZeroCounts.isNotEmpty()) {
                val pieData = nonZeroCounts.map { status ->
                    Pie(
                        label = status.label,
                        data = status.count.toDouble(),
                        color = statusColorMap[status.label] ?: Color.Gray
                    )
                }

                PieChart(
                    data = pieData,
                    modifier = Modifier.size(220.dp),
                    spaceDegree = 0f,
                    selectedScale = 1.1f,
                    selectedPaddingDegree = 0f,
                    style = Pie.Style.Stroke(width = 70.dp),
                    scaleAnimEnterSpec = tween(400),
                    scaleAnimExitSpec = tween(400),
                    onPieClick = { pie ->
                        val clickedStatus = nonZeroCounts.firstOrNull { it.label == pie.label }
                        selectedSlice = if (selectedSlice?.label == pie.label) null else clickedStatus

                        clickedStatus?.let { status ->
                            val index = counts.indexOf(status)
                            coroutineScope.launch {
                                legendListState.animateScrollToItem(index)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                selectedSlice?.let { slice ->
                    val percentage = if (total > 0) slice.count.toDouble() / total * 100 else 0.0
                    val roundedPercentage = percentage.roundToInt()
                    Text(
                        text = "${slice.label} - $roundedPercentage% (${slice.count} bookings)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColorMap[slice.label] ?: Color.Black
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No booking data available",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                state = legendListState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(counts) { index, status ->
                    val isSelected = selectedSlice?.label == status.label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected)
                                    statusColorMap[status.label]?.copy(alpha = 0.2f)
                                        ?: Color.Gray.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable {
                                selectedSlice = status
                                coroutineScope.launch {
                                    legendListState.animateScrollToItem(index)
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(
                                    color = statusColorMap[status.label] ?: Color.Gray,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = status.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}