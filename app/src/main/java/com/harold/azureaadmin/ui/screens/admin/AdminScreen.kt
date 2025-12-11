package com.harold.azureaadmin.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.harold.azureaadmin.R
import com.harold.azureaadmin.data.local.DataStoreManager
import com.harold.azureaadmin.data.repository.AdminRepository
import com.harold.azureaadmin.ui.components.sidebar.DrawerContent
import com.harold.azureaadmin.ui.screens.admin.amenities.AmenitiesScreen
import com.harold.azureaadmin.ui.screens.admin.archived_users.ArchivedUsersScreen
import com.harold.azureaadmin.ui.screens.admin.areas.AreasScreen
import com.harold.azureaadmin.ui.screens.admin.bookings.BookingsScreen
import com.harold.azureaadmin.ui.screens.admin.dashboard.DashboardScreen
import com.harold.azureaadmin.ui.screens.admin.rooms.RoomsScreen
import com.harold.azureaadmin.ui.screens.admin.users.UsersScreen
import com.harold.azureaadmin.ui.theme.Playfair
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("Admin Dashboard") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.4f),
        drawerContent = {
            DrawerContent(
                selectedItem = selectedItem,
                onItemClick = { item ->
                    selectedItem = item
                    scope.launch { drawerState.close() }
                },
                onClose = { scope.launch { drawerState.close() } },
                navController = navController
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.moon_stars),
                                    contentDescription = "Logo",
                                    modifier = Modifier.size(25.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Azurea",
                                    color = Color.White,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Playfair
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* more actions */ }) {}
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6A1B9A)
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            ) {
                when (selectedItem) {
                    "Admin Dashboard" -> DashboardScreen()
                    "Manage Bookings" -> BookingsScreen()
                    "Manage Areas" -> AreasScreen()
                    "Manage Rooms" -> RoomsScreen()
                    "Manage Amenities" -> AmenitiesScreen()
                    "Manage Users" -> UsersScreen()
                    "Archived Users" -> ArchivedUsersScreen()
                    else -> Text("Unknown")
                }
            }
        }
    }
}