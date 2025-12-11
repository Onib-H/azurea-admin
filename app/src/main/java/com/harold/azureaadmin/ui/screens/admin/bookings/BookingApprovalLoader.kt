package com.harold.azureaadmin.ui.screens.admin.bookings

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/**
 * Simple booking approval loader - just shows/hides based on isLoading
 */
@Composable
fun BookingApprovalLoader(
    isLoading: Boolean,
    actionType: BookingActionType = BookingActionType.RESERVE,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Dialog(
            onDismissRequest = { /* Prevent dismissal during loading */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Rotating loader
                    NaturalCircularLoader(
                        actionType = actionType,
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = getLoadingText(actionType),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Please wait...",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }
    }
}

@Composable
private fun NaturalCircularLoader(
    actionType: BookingActionType,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val diameter = size.minDimension - strokeWidth

            // Background circle
            drawArc(
                color = Color(0xFFE3F2FD),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(diameter, diameter),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )

            // Animated arc
            drawArc(
                color = Color(0xFF7B1FA2),
                startAngle = rotation - 90f,
                sweepAngle = 280f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(diameter, diameter),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )
        }

        // Center icon
        Icon(
            imageVector = getActionIcon(actionType),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = Color(0xFF7B1FA2)
        )
    }
}

private fun getLoadingText(actionType: BookingActionType): String {
    return when (actionType) {
        BookingActionType.RESERVE -> "Reserving Booking"
        BookingActionType.CHECK_IN -> "Checking In Guest"
        BookingActionType.CHECK_OUT -> "Checking Out Guest"
        BookingActionType.REJECT -> "Rejecting Booking"
        BookingActionType.CANCEL -> "Cancelling Booking"
        BookingActionType.MARK_NO_SHOW -> "Marking No Show"
    }
}

private fun getActionIcon(actionType: BookingActionType) = when (actionType) {
    BookingActionType.RESERVE -> Icons.Filled.CheckCircle
    BookingActionType.CHECK_IN -> Icons.Filled.CheckCircle
    BookingActionType.CHECK_OUT -> Icons.Filled.CheckCircle
    BookingActionType.REJECT -> Icons.Filled.CheckCircle
    BookingActionType.CANCEL -> Icons.Filled.CheckCircle
    BookingActionType.MARK_NO_SHOW -> Icons.Filled.CheckCircle
}