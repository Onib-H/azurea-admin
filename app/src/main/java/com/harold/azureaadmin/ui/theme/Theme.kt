package com.harold.azureaadmin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.harold.azureaadmin.R

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimaryLight,
    onPrimary = Color.Black,
    primaryContainer = PurplePrimaryDark,
    secondary = BlueSecondaryLight,
    onSecondary = Color.Black,
    secondaryContainer = BlueSecondaryDark,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    error = RedAlertLight,
    errorContainer = RedAlertDark
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurplePrimaryLight,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = BlueSecondaryLight,
    background = NeutralBackground,
    surface = NeutralSurface,
    onSurface = NeutralOnSurface,
    error = RedAlert,
    errorContainer = RedAlertLight,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AzureaAdminTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}



val Playfair = FontFamily(
    Font(R.font.playfair_regular, FontWeight.Normal),
    Font(R.font.playfair_medium, FontWeight.Medium),
    Font(R.font.playfair_semibold, FontWeight.SemiBold),
    Font(R.font.playfair_bold, FontWeight.Bold)
)

