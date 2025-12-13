package com.harold.azureaadmin


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
import com.harold.azureaadmin.data.repository.AdminRepository
import com.harold.azureaadmin.ui.navigation.AzureaNavHost
import com.harold.azureaadmin.ui.theme.AzureaAdminTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inject dependencies directly into Activity
    @Inject
    lateinit var dataStore: DataStoreManager

    @Inject
    lateinit var repo: AdminRepository

    // The system splash will keep showing while this AtomicBoolean is true.
    private val isLoadingForSplash = AtomicBoolean(true)

    // Compose-observable start destination — null means "not ready yet"
    private var startDestinationState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // install splash BEFORE super
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the system splash while our async check runs
        splash.setKeepOnScreenCondition { isLoadingForSplash.get() }

        // Window configuration (same as yours)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.show(WindowInsetsCompat.Type.statusBars())
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Start the async session check (non-blocking)
        lifecycleScope.launchWhenCreated {
            val dest = try {
                // initialize cache safely (suspending)
                withContext(Dispatchers.IO) {
                    dataStore.initializeCache()
                    dataStore.getCachedCookieJson()
                }.let { cookieJson ->
                    if (cookieJson.isNullOrEmpty()) {
                        // no cookie → not logged in
                        withContext(Dispatchers.IO) { dataStore.clearAll() }
                        "login"
                    } else {
                        // validate with backend — keep on IO dispatcher if needed
                        val resp = withContext(Dispatchers.IO) {
                            repo.checkSession()
                        }
                        val ok = resp.isSuccessful &&
                                resp.body()?.isAuthenticated == true &&
                                resp.body()?.role == "admin"

                        if (ok) "admin" else {
                            withContext(Dispatchers.IO) { dataStore.clearAll() }
                            "login"
                        }
                    }
                }
            } catch (e: Exception) {
                // network or other error — treat as not authenticated
                withContext(Dispatchers.IO) { dataStore.clearAll() }
                "login"
            }

            // Publish result to Compose and allow splash to hide
            startDestinationState.value = dest
            isLoadingForSplash.set(false)
        }

        // Compose UI — shows nothing (or a minimal compose splash) until startDestinationState is set
        setContent {
            AzureaAdminTheme {
                val startDest by startDestinationState

                if (startDest != null) {
                    // Normal app start — NavHost only created when we already know the destination
                    AzureaNavHost(startDestination = startDest!!)
                } else {
                    // Minimal Compose splash placeholder while system splash is up.
                    // Keep it very light so you don't get flicker or heavy work here.
                    SimpleComposeSplash()
                }
            }
        }
    }
}

// Minimal Compose placeholder — simple centered spinner and app name (optional)
@androidx.compose.runtime.Composable
fun SimpleComposeSplash() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Keep this very lightweight. If you prefer an image/logo, use Image with painterResource.
        CircularProgressIndicator(modifier = Modifier.size(36.dp))
    }
}

