package com.harold.azureaadmin.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.harold.azureaadmin.R
import com.harold.azureaadmin.data.repository.AdminRepository
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    repository: AdminRepository
) {
    val scale = remember { Animatable(0f) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        delay(800)

        try {
            val response = repository.checkSession()
            if (response.isSuccessful && response.body()?.isAuthenticated == true) {
                navController.navigate("admin") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        } catch (e: Exception) {
            // If session check fails, go to login
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        } finally {
            isFinished = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.azurea_admin),
                contentDescription = "Logo",
                modifier = Modifier.size(500.dp)
            )
        }
    }
}