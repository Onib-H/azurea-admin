package com.harold.azureaadmin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.harold.azureaadmin.ui.screens.admin.AdminScreen
import com.harold.azureaadmin.ui.screens.login.LoginScreen
import com.harold.azureaadmin.ui.screens.login.LoginViewModel
import com.harold.azureaadmin.ui.screens.admin.AdminViewModel

@Composable
fun AzureaNavHost(startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val vm: LoginViewModel = hiltViewModel()
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Admin.route) {
            val vm: AdminViewModel = hiltViewModel()

            AdminScreen(
                navController = navController,
                viewModel = vm
            )
        }

    }
}
