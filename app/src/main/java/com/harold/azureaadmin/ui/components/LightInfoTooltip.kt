package com.harold.azureaadmin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

@Composable
fun LightInfoTooltip(
    message: String,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.TopCenter,
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 40.dp) // ✔ no touching edges
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message,
                fontSize = 13.sp,
                color = accentColor, // ✔ text uses accent
                lineHeight = 17.sp,
                maxLines = 2
            )
        }
    }
}
