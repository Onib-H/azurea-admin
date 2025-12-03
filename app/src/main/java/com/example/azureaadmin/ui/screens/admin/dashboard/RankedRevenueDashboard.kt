package com.example.azureaadmin.ui.screens.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.azureaadmin.data.models.AreaBookingResponse
import com.example.azureaadmin.data.models.AreaRevenueResponse
import com.example.azureaadmin.data.models.RoomBookingResponse
import com.example.azureaadmin.data.models.RoomRevenueResponse
import com.example.azureaadmin.utils.FormatPrice

enum class RevenueViewType {
    ROOM, AREA
}

data class RevenueItem(
    val name: String,
    val revenue: Double,
    val bookingCount: Int,
    val rank: Int
)

@Composable
fun RankedRevenueDashboard(
    roomRevenue: RoomRevenueResponse?,
    roomBookings: RoomBookingResponse?,
    areaRevenue: AreaRevenueResponse?,
    areaBookings: AreaBookingResponse?,
    modifier: Modifier = Modifier
) {
    var selectedView by remember { mutableStateOf(RevenueViewType.ROOM) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header with Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Revenue Ranking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Segmented button toggle
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Rooms button
                Surface(
                    onClick = { selectedView = RevenueViewType.ROOM },
                    shape = RoundedCornerShape(6.dp),
                    color = if (selectedView == RevenueViewType.ROOM) {
                        Color.White
                    } else {
                        Color.Transparent
                    },
                    shadowElevation = if (selectedView == RevenueViewType.ROOM) 2.dp else 0.dp,
                    modifier = Modifier.height(32.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Rooms",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedView == RevenueViewType.ROOM) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                            color = if (selectedView == RevenueViewType.ROOM) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    }
                }

                // Areas button
                Surface(
                    onClick = { selectedView = RevenueViewType.AREA },
                    shape = RoundedCornerShape(6.dp),
                    color = if (selectedView == RevenueViewType.AREA) {
                        Color.White
                    } else {
                        Color.Transparent
                    },
                    shadowElevation = if (selectedView == RevenueViewType.AREA) 2.dp else 0.dp,
                    modifier = Modifier.height(32.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Areas",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedView == RevenueViewType.AREA) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                            color = if (selectedView == RevenueViewType.AREA) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Revenue List
        when (selectedView) {
            RevenueViewType.ROOM -> {
                if (roomRevenue != null && roomBookings != null) {
                    RevenueRankingList(
                        names = roomRevenue.room_names,
                        revenueData = roomRevenue.revenue_data,
                        bookingCounts = roomBookings.booking_counts,
                        emptyMessage = "No room data"
                    )
                } else {
                    EmptyStateMessage("Loading...")
                }
            }
            RevenueViewType.AREA -> {
                if (areaRevenue != null && areaBookings != null) {
                    RevenueRankingList(
                        names = areaRevenue.area_names,
                        revenueData = areaRevenue.revenue_data,
                        bookingCounts = areaBookings.booking_counts,
                        emptyMessage = "No area data"
                    )
                } else {
                    EmptyStateMessage("Loading...")
                }
            }
        }
    }
}

@Composable
private fun RevenueRankingList(
    names: List<String>,
    revenueData: List<Double>,
    bookingCounts: List<Int>,
    emptyMessage: String,
    modifier: Modifier = Modifier
) {
    // Safely combine revenue and booking data
    val items = names.mapIndexedNotNull { index, name ->
        val revenue = revenueData.getOrNull(index)
        val bookingCount = bookingCounts.getOrNull(index)

        // Only create item if we have valid data
        if (revenue != null && bookingCount != null) {
            RevenueItem(
                name = name,
                revenue = revenue,
                bookingCount = bookingCount,
                rank = 0
            )
        } else {
            null
        }
    }

    // Sort: non-zero revenue items by revenue (descending), then zero-revenue items
    val sortedItems = items.partition { it.revenue > 0 }.let { (nonZero, zero) ->
        nonZero.sortedByDescending { it.revenue } + zero.sortedBy { it.name }
    }.mapIndexed { index, item ->
        item.copy(rank = index + 1)
    }

    // Check if all items have zero revenue
    val allZeroRevenue = sortedItems.isNotEmpty() && sortedItems.all { it.revenue == 0.0 }

    when {
        sortedItems.isEmpty() -> {
            EmptyStateMessage(emptyMessage)
        }
        allZeroRevenue -> {
            NoDataDisplay()
        }
        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(sortedItems) { _, item ->
                    RevenueItemCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun RevenueItemCard(
    item: RevenueItem,
    modifier: Modifier = Modifier
) {
    val isZeroRevenue = item.revenue == 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isZeroRevenue) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank and Name
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Rank Badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = when {
                                isZeroRevenue -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                item.rank == 1 -> Color(0xFFFFD700)
                                item.rank == 2 -> Color(0xFFC0C0C0)
                                item.rank == 3 -> Color(0xFFCD7F32)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            shape = RoundedCornerShape(5.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.rank.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isZeroRevenue -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            item.rank <= 3 -> Color.Black
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }

                // Name - truncate if too long
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isZeroRevenue) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1
                )
            }

            // Revenue and Bookings
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = FormatPrice.formatRevenue(item.revenue.toString()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isZeroRevenue) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )

                Text(
                    text = "${item.bookingCount} ${if (item.bookingCount == 1) "booking" else "bookings"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun NoDataDisplay(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )

            Text(
                text = "No Revenue Available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = "There’s no recorded revenue for the this month.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun EmptyStateMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}