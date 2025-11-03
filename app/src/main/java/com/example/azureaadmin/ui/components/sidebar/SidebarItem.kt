package com.example.azureaadmin.ui.components.sidebar

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SidebarItem(
    label: String,
    selectedItem: String,
    onClick: (String) -> Unit,
    isLogout: Boolean = false
) {
    val isSelected = selectedItem == label
    val backgroundColor =
        if (isSelected && !isLogout) Color(0xFFF3E5F5) else Color.Transparent
    val contentColor =
        if (isLogout) Color.Red else if (isSelected) Color(0xFF6A1B9A) else Color.Black
    val fontWeight =
        if (isSelected) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .clickable { onClick(label) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = contentColor,
            fontWeight = fontWeight,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
