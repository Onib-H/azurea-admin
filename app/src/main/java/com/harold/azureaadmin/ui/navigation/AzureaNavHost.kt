package com.harold.azureaadmin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.harold.azureaadmin.data.local.DataStoreManager
import com.harold.azureaadmin.data.repository.AdminRepository
import com.harold.azureaadmin.ui.screens.admin.AdminScreen
import com.harold.azureaadmin.ui.screens.login.LoginScreen
import com.harold.azureaadmin.ui.screens.splash.SplashScreen

@Composable
fun AzureaNavHost(
    dataStoreManager: DataStoreManager,
    repository: AdminRepository
) {
    val navController = rememberNavController()
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
            SplashScreen(
                navController = navController,
                repository = repository
            )
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
            AdminScreen(
                navController = navController,
                repository = repository,
                dataStoreManager = dataStoreManager
            )
        }
    }
}