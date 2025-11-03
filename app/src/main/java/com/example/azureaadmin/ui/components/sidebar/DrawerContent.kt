    package com.example.azureaadmin.ui.components.sidebar
    
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
    import androidx.compose.material.icons.filled.Close
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.rememberCoroutineScope
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.unit.dp
    import androidx.navigation.NavController
    import com.example.azureaadmin.R
    import com.example.azureaadmin.data.local.DataStoreManager
    import com.example.azureaadmin.data.remote.RetrofitInstance
    import com.example.azureaadmin.data.repository.AdminRepository
    import kotlinx.coroutines.flow.first
    import kotlinx.coroutines.flow.firstOrNull
    import kotlinx.coroutines.launch
    
    @Composable
    fun DrawerContent(
        selectedItem: String,
        onItemClick: (String) -> Unit,
        onClose: () -> Unit,
        navController: NavController,
        dataStoreManager: DataStoreManager
    ) {
    
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val repo = AdminRepository(context)
    
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(240.dp)
                .background(Color(0xFFF5F5F5)) // light gray background for sidebar
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
                    modifier = Modifier
                        .size(100.dp) // same size as the Close icon
                )
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black // visible on light background
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
                    scope.launch {
                        val success = repo.logout().isSuccessful
                        if (success) {
                            dataStoreManager.clearAll()
                            RetrofitInstance.clearCookies()
    
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Failed to logout. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                isLogout = true
            )
    
    
    
    
        }
    }
