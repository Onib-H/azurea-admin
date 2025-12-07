package com.harold.azureaadmin.ui.components.sidebar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.harold.azureaadmin.R


@Composable
fun Sidebar(
    selectedItem: String,
    onItemClick: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(Color(0xFF6A1B9A))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.hotel_logo),
                contentDescription = "Hotel Logo",
                modifier = Modifier.size(50.dp)
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val items = listOf(
            "Dashboard", "Manage Bookings", "Manage Areas",
            "Manage Rooms", "Manage Amenities", "Manage Users", "Archived Users"
        )

        items.forEach { SidebarItem(it, selectedItem, onClick = onItemClick) }

        Spacer(modifier = Modifier.weight(1f)) // pushes logout to bottom

        SidebarItem("Logout", selectedItem, onClick = onItemClick, isLogout = true)
    }
}


