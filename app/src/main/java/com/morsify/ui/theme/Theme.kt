package com.morsify.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = MFPrimary,
    onPrimary = MFOnPrimary,
    primaryContainer = MFTintLavender,
    onPrimaryContainer = MFPrimary,
    secondary = MFPrimaryPressed,
    onSecondary = MFOnPrimary,
    background = MFCanvas,
    onBackground = MFInk,
    surface = MFCanvas,
    onSurface = MFInk,
    surfaceVariant = MFSurface,
    onSurfaceVariant = MFSlate,
    outline = MFHairlineStrong,
    outlineVariant = MFHairline,
    error = MFError,
    onError = MFOnPrimary
)

private val DarkColors = darkColorScheme(
    primary = MFPrimary,
    onPrimary = MFOnPrimary,
    primaryContainer = MFPrimary,
    onPrimaryContainer = MFTintLavender,
    secondary = MFPrimaryPressed,
    onSecondary = MFOnPrimary,
    background = MFSurfaceDark,
    onBackground = MFCanvas,
    surface = MFSurfaceDarkCard,
    onSurface = MFCanvas,
    surfaceVariant = MFCharcoal,
    onSurfaceVariant = MFMuted,
    outline = MFSteel,
    outlineVariant = MFCharcoal,
    error = MFError,
    onError = MFOnPrimary
)

@Composable
fun MorsifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = MFTypography,
        content = content
    )
}
