        package com.example.azureaadmin.ui.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.azureaadmin.data.local.DataStoreManager
import com.example.azureaadmin.ui.screens.admin.AdminScreen
import com.example.azureaadmin.ui.screens.admin.dashboard.DashboardScreen
import com.example.azureaadmin.ui.screens.login.LoginScreen
import com.example.azureaadmin.ui.screens.splash.SplashScreen

@Composable
fun AzureaNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val token by dataStoreManager.getToken.collectAsState(initial = null)

    LaunchedEffect(token) {
        if (token == null) {
            navController.navigate(Screen.Splash.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Admin.route) {
            AdminScreen(navController)
        }
    }
}

