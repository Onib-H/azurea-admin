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

object AzureaColors {
    val Purple = Color(0xFF7B1FA2)
    val PurpleDark = Color(0xFF5A1878)
    val PurpleLight = Color(0xFFEDE1F6)
    val PurpleLighter = Color(0xFFF6EFFB)

    val NeutralDark = Color(0xFF1A1A1A)
    val NeutralMedium = Color(0xFF666666)
    val NeutralLight = Color(0xFFF5F5F5)
    val NeutralStroke = Color(0xFFEAEAEA)

    val Success = Color(0xFF2E7D32)
    val SuccessLight = Color(0xFFE8F5E9)

    val Warning = Color(0xFFE67E22)
    val WarningLight = Color(0xFFFFF4E6)

    val Error = Color(0xFFD32F2F)
    val ErrorLight = Color(0xFFFFE6E6)
}



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

