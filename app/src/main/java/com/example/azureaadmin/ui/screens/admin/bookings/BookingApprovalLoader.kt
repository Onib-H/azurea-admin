package com.example.azureaadmin.ui.screens.admin.bookings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay

@Composable
fun BookingApprovalLoader(
    isLoading: Boolean,
    onComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var isComplete by remember { mutableStateOf(false) }

    // Auto-progress through steps
    LaunchedEffect(isLoading) {
        if (isLoading) {
            currentStep = 0
            isComplete = false

            // Step 1: Verifying (1 second)
            delay(1000)
            currentStep = 1

            // Step 2: Processing (1 second)
            delay(1000)
            currentStep = 2

            // Step 3: Sending (1 second)
            delay(1000)
            currentStep = 3

            // Show completion
            delay(500)
            isComplete = true

            // Wait 2 seconds then notify completion
            delay(2000)
            onComplete()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isComplete) {
                // Circular Progress Ring
                CircularProgressRing(
                    currentStep = currentStep,
                    totalSteps = 3,
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Status Text
                Text(
                    text = when (currentStep) {
                        0 -> "Verifying Guest"
                        1 -> "Processing Payment"
                        2 -> "Sending Notification"
                        else -> "Finalizing"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Step ${currentStep + 1}/3",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Admin approval in progress",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            } else {
                // Success State
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(80.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF16A34A)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "All Done!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Guest has been notified",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun CircularProgressRing(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val progress = (currentStep + 1).toFloat() / totalSteps
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Background Circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )
        }

        // Progress Circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val sweepAngle = 360f * animatedProgress
            drawArc(
                color = Color(0xFF14B8A6),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )
        }

        // Center Icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (currentStep) {
                    0 -> Icons.Filled.Person
                    1 -> Icons.Filled.CreditCard
                    2 -> Icons.Filled.Notifications
                    else -> Icons.Filled.Check
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF14B8A6)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Step ${currentStep + 1}/$totalSteps",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
        }
    }
}

// Preview with example usage
@Composable
fun BookingApprovalLoaderDemo() {
    var isLoading by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isLoading) {
                Button(
                    onClick = { isLoading = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF14B8A6)
                    )
                ) {
                    Text("Start Booking Approval")
                }
            }

            if (isLoading) {
                BookingApprovalLoader(
                    isLoading = isLoading,
                    onComplete = { isLoading = false }
                )
            }
        }
    }
}

// Alternative: Simplified version without auto-steps (manual control)
@Composable
fun BookingApprovalLoaderManual(
    currentStep: Int, // 0, 1, 2, or 3 (complete)
    totalSteps: Int = 3,
    isComplete: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isComplete) {
                CircularProgressRing(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = when (currentStep) {
                        0 -> "Verifying Guest"
                        1 -> "Processing Payment"
                        2 -> "Sending Notification"
                        else -> "Finalizing"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Step ${currentStep + 1}/$totalSteps",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(80.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF16A34A)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "All Done!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }
        }
    }
}