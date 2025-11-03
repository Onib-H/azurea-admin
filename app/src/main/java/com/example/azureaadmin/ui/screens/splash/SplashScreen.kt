package com.example.azureaadmin.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.azureaadmin.R
import com.example.azureaadmin.data.remote.RetrofitInstance
import com.example.azureaadmin.ui.theme.Playfair
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val scale = remember { Animatable(0f) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Animate logo scale
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        // Stay for a bit
        delay(800)

        try {
            val api = RetrofitInstance.getApi(context)
            val response = api.checkSession()
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
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        } finally {
            isFinished = true
        }
    }

    // 🎨 UI
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {

            Image(
                painter = painterResource(id = R.drawable.moon_stars),
                contentDescription = "Logo",
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Azurea",
                fontFamily = Playfair,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                color = Color.White
            )


        }
    }
}
