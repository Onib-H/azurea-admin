package com.harold.azureaadmin.ui.components.sidebar

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.harold.azureaadmin.R
import com.harold.azureaadmin.data.local.DataStoreManager
import com.harold.azureaadmin.data.repository.AdminRepository
import com.harold.azureaadmin.ui.components.modals.DeleteItemDialog
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    selectedItem: String,
    onItemClick: (String) -> Unit,
    onClose: () -> Unit,
    navController: NavController,
    dataStoreManager: DataStoreManager,
    repository: AdminRepository // Add repository as parameter
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.hotel_logo),
                contentDescription = "Hotel Logo",
                modifier = Modifier.size(100.dp)
            )
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val items = listOf(
            "Dashboard", "Manage Bookings", "Manage Areas",
            "Manage Rooms", "Manage Amenities", "Manage Users", "Archived Users"
        )

        items.forEach { SidebarItem(it, selectedItem, onItemClick) }

        Spacer(modifier = Modifier.weight(1f))

        // Logout item
        SidebarItem(
            label = "Logout",
            selectedItem = selectedItem,
            onClick = {
                showLogoutDialog = true
            },
            isLogout = true
        )
    }

    if (showLogoutDialog) {
        DeleteItemDialog(
            show = showLogoutDialog,
            itemLabel = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
            title = "Logout",
            description = "Are you sure you want to log out?",
            confirmButtonText = "Logout",
            onDismiss = { showLogoutDialog = false },
            onDelete = {
                scope.launch {
                    try {
                        val success = repository.logout().isSuccessful
                        if (success) {
                            // Clear all stored data
                            dataStoreManager.clearAll()

                            // Navigate to login and clear back stack
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to logout. Try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Error during logout: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }
}