package com.example.azureaadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import com.example.azureaadmin.ui.navigation.AzureaNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            AzureaApp()
        }
    }
}

@Composable
fun AzureaApp() {
    MaterialTheme {
        AzureaNavHost()

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    AzureaApp()
}
