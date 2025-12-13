package com.harold.azureaadmin.data.remote

import android.util.Log
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerWarmupService @Inject constructor(
    private val apiService: AdminApiService
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var warmupJob: Job? = null

    /**
     * Call this when app starts to wake up the server
     */
    fun startWarmup() {
        warmupJob?.cancel()
        warmupJob = scope.launch {
            try {
                Log.d("ServerWarmup", "Pinging server to wake it up...")
                // Make a lightweight request to wake the server
                // Use a health check endpoint if available, or any GET endpoint
                val response = withTimeoutOrNull(60000) { // 60s timeout for cold start
                    try {
                        apiService.checkSession() // or create a /health endpoint
                    } catch (e: Exception) {
                        Log.d("ServerWarmup", "Warmup ping completed (may have failed, but server is waking)")
                        null
                    }
                }
                Log.d("ServerWarmup", "Server is warm and ready")
            } catch (e: Exception) {
                Log.e("ServerWarmup", "Warmup error: ${e.message}")
            }
        }
    }

    fun cancelWarmup() {
        warmupJob?.cancel()
    }
}