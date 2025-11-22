package com.example.azureaadmin.ui.screens.admin

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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.azureaadmin.R
import com.example.azureaadmin.data.local.DataStoreManager
import com.example.azureaadmin.data.repository.AdminRepository
import com.example.azureaadmin.ui.components.sidebar.DrawerContent
import com.example.azureaadmin.ui.screens.admin.amenities.AmenitiesScreen
import com.example.azureaadmin.ui.screens.admin.archived_users.ArchivedUsersScreen
import com.example.azureaadmin.ui.screens.admin.areas.AreasScreen
import com.example.azureaadmin.ui.screens.admin.bookings.BookingsScreen
import com.example.azureaadmin.ui.screens.admin.dashboard.DashboardScreen
import com.example.azureaadmin.ui.screens.admin.rooms.RoomsScreen
import com.example.azureaadmin.ui.screens.admin.users.UsersScreen
import com.example.azureaadmin.ui.theme.Playfair
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = remember { AdminRepository(context) }
    val dataStoreManager = remember { DataStoreManager(context) }

    var loggedIn by remember { mutableStateOf<Boolean?>(null) }

    // 🔑 Check token OR cookie
    LaunchedEffect(Unit) {
        val ds = DataStoreManager(context)
        val cookie = ds.getCookie.firstOrNull()
        val token = ds.getToken.firstOrNull()
        loggedIn = !token.isNullOrEmpty() || !cookie.isNullOrEmpty()
        if (loggedIn == false) {
            navController.navigate("splash") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    var selectedItem by remember { mutableStateOf("Dashboard") }

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
                navController = navController,
                dataStoreManager = dataStoreManager
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
                        IconButton(onClick = { /* more actions */ }) {
                            Icon(Icons.Default.Refresh, contentDescription = "More", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6A1B9A)
                    )
                )
            }
        ) { padding ->

            // ✅ Switch content here
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            ) {
                when (selectedItem) {
                    "Dashboard" -> DashboardScreen(repository)
                    "Manage Bookings" -> BookingsScreen(repository)
                    "Manage Areas" -> AreasScreen(repository)
                    "Manage Rooms" -> RoomsScreen(repository)
                    "Manage Amenities" -> AmenitiesScreen(repository)
                    "Manage Users" -> UsersScreen(repository)
                    "Archived Users" -> ArchivedUsersScreen(repository)
                    else -> Text("Unknown")
                }
            }

        }
    }
}
