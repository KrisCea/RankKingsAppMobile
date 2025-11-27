package com.example.rankkings.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Color.Black,
    primaryContainer = GoldDark,
    onPrimaryContainer = TextPrimary,

    secondary = GoldLight,
    onSecondary = Color.Black,
    secondaryContainer = GoldDark,
    onSecondaryContainer = TextPrimary,

    tertiary = GoldLight,
    onTertiary = Color.Black,

    background = DarkBackground,
    onBackground = TextPrimary,

    surface = DarkSurface,
    onSurface = TextPrimary,

    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,

    error = ErrorColor,
    onError = TextPrimary,

    outline = Color(0xFF4A4A4A),
    outlineVariant = Color(0xFF2A2A2A)
)

@Composable
fun RankkingsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
