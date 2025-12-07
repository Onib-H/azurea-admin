package com.harold.azureaadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.harold.azureaadmin.data.local.DataStoreManager
import com.harold.azureaadmin.data.repository.AdminRepository
import com.harold.azureaadmin.ui.navigation.AzureaNavHost
import com.harold.azureaadmin.ui.theme.AzureaAdminTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var repository: AdminRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)

        // Show status bar
        insetsController.show(WindowInsetsCompat.Type.statusBars())

        // Hide navigation bar
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())

        // Allow swipe to show navigation bar
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            AzureaAdminTheme {
                AzureaNavHost(
                    dataStoreManager = dataStoreManager,
                    repository = repository
                )
            }
        }
    }

}
