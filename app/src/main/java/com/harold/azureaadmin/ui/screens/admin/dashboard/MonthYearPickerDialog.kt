package com.harold.azureaadmin.ui.screens.admin.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@Composable
fun MonthYearPickerDialog(
    initial: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    val currentYearMonth = YearMonth.of(currentYear, currentMonth)

    var displayedYear by remember { mutableStateOf(initial.year) }
    var selectedMonthYear by remember { mutableStateOf(initial) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                // Year Navigation Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { displayedYear-- },
                        enabled = displayedYear > 2000
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Year",
                            tint = if (displayedYear > 2000) {
                                Color.Black
                            } else {
                                Color.Black.copy(alpha = 0.38f)
                            }
                        )
                    }

                    Text(
                        text = displayedYear.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    IconButton(
                        onClick = { displayedYear++ },
                        enabled = displayedYear < currentYear
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Year",
                            tint = if (displayedYear < currentYear) {
                                Color.Black
                            } else {
                                Color.Black.copy(alpha = 0.38f)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Month Calendar Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (row in 0..3) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (col in 0..2) {
                                val month = row * 3 + col + 1
                                val monthYearMonth = YearMonth.of(displayedYear, month)
                                val isFuture = monthYearMonth.isAfter(currentYearMonth)
                                val isCurrentMonth = monthYearMonth == currentYearMonth
                                val isSelectedMonth = monthYearMonth == selectedMonthYear

                                Surface(
                                    onClick = {
                                        if (!isFuture) {
                                            selectedMonthYear = monthYearMonth
                                            onConfirm(monthYearMonth)
                                            onDismiss()
                                        }
                                    },
                                    enabled = !isFuture,
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isCurrentMonth -> MaterialTheme.colorScheme.primary
                                        isSelectedMonth -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        isFuture -> Color(0xFFF5F5F5)
                                        else -> Color.White
                                    },
                                    border = when {
                                        isCurrentMonth -> null
                                        isSelectedMonth -> null
                                        isFuture -> null
                                        else -> BorderStroke(1.dp, Color(0xFFE0E0E0))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp)
                                    ) {
                                        Text(
                                            text = monthYearMonth.month.getDisplayName(
                                                TextStyle.SHORT,
                                                Locale.getDefault()
                                            ),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = when {
                                                isCurrentMonth -> MaterialTheme.colorScheme.onPrimary
                                                isSelectedMonth -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                                isFuture -> Color.Black.copy(alpha = 0.38f)
                                                else -> Color.Black
                                            },
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}