package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ScreenshotColorScheme = lightColorScheme(
    primary = DukaanForestGreen,
    onPrimary = Color.White,
    primaryContainer = DukaanForestGreenLight,
    onPrimaryContainer = Color.White,
    secondary = DukaanGreenDark,
    onSecondary = Color.White,
    error = DukaanRedText,
    onError = Color.White,
    background = DukaanPageBg,
    onBackground = DukaanTextDark,
    surface = DukaanCardSurface,
    onSurface = DukaanTextDark,
    surfaceVariant = DukaanNavBg,
    onSurfaceVariant = DukaanTextMuted
)

@Composable
fun DukaanHisabTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ScreenshotColorScheme,
        typography = Typography,
        content = content
    )
}
