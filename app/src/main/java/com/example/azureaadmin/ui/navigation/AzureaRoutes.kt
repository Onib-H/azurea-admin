package com.example.azureaadmin.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Admin : Screen("admin")
}
