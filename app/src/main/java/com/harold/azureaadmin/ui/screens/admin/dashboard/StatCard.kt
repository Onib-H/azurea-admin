package com.harold.azureaadmin.ui.screens.admin.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class TrendType {
    UP, DOWN, NEUTRAL
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trend: TrendType? = null,
    trendValue: String = "0%",
    icon: ImageVector? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            hoveredElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp)
        ) {
            // Top Row: Icon and Trend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = Color(0xFFF3E5F5), // Light purple background
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon ?: getDefaultIcon(label),
                        contentDescription = null,
                        tint = Color(0xFF7B2CBF), // Purple icon
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Trend Indicator
                TrendBadge(trend = trend, value = trendValue)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Value
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Label
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun TrendBadge(trend: TrendType?, value: String) {
    // Don't show badge if value is empty or "0%"
    if (value.isEmpty() || value == "0%" || value == "+0%" || value == "-0%" || trend == null) return

    val (backgroundColor, textColor, trendIcon) = when (trend) {
        TrendType.UP -> Triple(
            Color(0xFFDCFCE7),
            Color(0xFF16A34A),
            Icons.AutoMirrored.Filled.TrendingUp
        )
        TrendType.DOWN -> Triple(
            Color(0xFFFEE2E2),
            Color(0xFFDC2626),
            Icons.AutoMirrored.Filled.TrendingDown
        )
        TrendType.NEUTRAL -> return // Don't show neutral trends
    }

    Row(
        modifier = Modifier
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            trendIcon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            value,
            color = textColor,
            fontSize = 12.sp
        )
    }
}


// Helper function to get default icon based on label
private fun getDefaultIcon(label: String): ImageVector {
    return when {
        label.contains("Active", ignoreCase = true) -> Icons.Default.CheckCircle
        label.contains("Pending", ignoreCase = true) -> Icons.Default.PendingActions
        label.contains("Total", ignoreCase = true) -> Icons.Default.CalendarMonth
        label.contains("Revenue", ignoreCase = true) -> Icons.Outlined.AttachMoney
        else -> Icons.Default.CalendarMonth
    }
}

// Extension function for easy usage with trend
@Composable
fun StatCardWithTrend(
    label: String,
    value: String,
    trendType: TrendType?,
    trendValue: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    StatCard(
        label = label,
        value = value,
        trend = trendType,
        trendValue = trendValue,
        icon = icon,
        modifier = modifier
    )
}

// Simple version without trend (backward compatible)
@Composable
fun SimpleStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    StatCard(
        label = label,
        value = value,
        trend = TrendType.NEUTRAL,
        trendValue = "0%",
        modifier = modifier
    )
}