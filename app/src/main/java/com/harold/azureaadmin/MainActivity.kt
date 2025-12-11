package com.harold.azureaadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.harold.azureaadmin.ui.navigation.AzureaNavHost
import com.harold.azureaadmin.ui.theme.AzureaAdminTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // install splash BEFORE super
        val splash = installSplashScreen()

        super.onCreate(savedInstanceState)

        // keep splash while viewmodel is loading
        splash.setKeepOnScreenCondition { mainViewModel.isLoading.value }

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.show(WindowInsetsCompat.Type.statusBars())
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            AzureaAdminTheme {
                AzureaNavHost(startDestination = mainViewModel.startDestination.value)
            }
        }
    }
}
