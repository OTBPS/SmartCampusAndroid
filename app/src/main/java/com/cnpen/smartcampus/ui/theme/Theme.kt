package com.cnpen.smartcampus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SmartCampusColorScheme = lightColorScheme(
    primary = CampusBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = CampusBlueContainer,
    secondary = CampusTeal,
    background = CampusSurface,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = CampusOnSurface
)

@Composable
fun SmartCampusTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SmartCampusColorScheme,
        typography = AppTypography,
        content = content
    )
}
