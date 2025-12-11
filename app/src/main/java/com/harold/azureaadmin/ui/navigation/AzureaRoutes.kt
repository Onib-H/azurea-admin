package com.harold.azureaadmin.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Admin : Screen("admin")
}
