package com.harold.azureaadmin


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.harold.azureaadmin.data.local.DataStoreManager
import com.harold.azureaadmin.data.remote.ServerWarmupService
import com.harold.azureaadmin.data.repository.AdminRepository
import com.harold.azureaadmin.ui.navigation.AzureaNavHost
import com.harold.azureaadmin.ui.theme.AzureaAdminTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStore: DataStoreManager

    @Inject
    lateinit var repo: AdminRepository

    @Inject
    lateinit var serverWarmup: ServerWarmupService

    private val isLoadingForSplash = AtomicBoolean(true)
    private var startDestinationState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        splash.setKeepOnScreenCondition { isLoadingForSplash.get() }

        serverWarmup.startWarmup()

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.show(WindowInsetsCompat.Type.statusBars())
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        lifecycleScope.launchWhenCreated {
            val dest = determineStartDestination()
            startDestinationState.value = dest
            isLoadingForSplash.set(false)
        }

        setContent {
            AzureaAdminTheme {
                val startDest by startDestinationState

                if (startDest != null) {
                    AzureaNavHost(startDestination = startDest!!)
                } else {
                    SimpleComposeSplash()
                }
            }
        }
    }

    private suspend fun determineStartDestination(): String = withContext(Dispatchers.IO) {
        try {
            // Quick check without network call
            dataStore.initializeCache()
            val cookieJson = dataStore.getCachedCookieJson()

            // If no cookies or expired, go straight to login - NO network call
            if (cookieJson.isNullOrEmpty() || dataStore.isSessionExpired()) {
                dataStore.clearAll()
                return@withContext "login"
            }

            // Only validate session if we have valid cookies
            // Add timeout to prevent hanging
            val validationResult = withTimeoutOrNull(5000) {
                try {
                    val resp = repo.checkSession()
                    resp.isSuccessful &&
                            resp.body()?.isAuthenticated == true &&
                            resp.body()?.role == "admin"
                } catch (e: Exception) {
                    Log.e("MainActivity", "Session check failed: ${e.message}")
                    false
                }
            }

            if (validationResult == true) {
                "admin"
            } else {
                dataStore.clearAll()
                "login"
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Startup error: ${e.message}")
            dataStore.clearAll()
            "login"
        }
    }
}

@Composable
fun SimpleComposeSplash() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(36.dp))
    }
}