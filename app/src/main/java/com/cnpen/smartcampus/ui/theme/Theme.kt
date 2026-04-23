package com.cnpen.smartcampus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import android.os.Build

private val SmartCampusLightColorScheme = lightColorScheme(
    primary = CampusBlue,
    onPrimary = Color.White,
    primaryContainer = CampusBlueContainer,
    secondary = CampusTeal,
    background = CampusSurface,
    surface = Color.White,
    onSurface = CampusOnSurface
)

private val SmartCampusDarkColorScheme = darkColorScheme(
    primary = CampusBlueDark,
    onPrimary = Color(0xFF0B1D3A),
    primaryContainer = CampusBlueContainerDark,
    secondary = CampusTealDark,
    background = CampusSurfaceDark,
    surface = Color(0xFF171B24),
    onSurface = CampusOnSurfaceDark
)

@Composable
fun SmartCampusTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> SmartCampusDarkColorScheme
        else -> SmartCampusLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
