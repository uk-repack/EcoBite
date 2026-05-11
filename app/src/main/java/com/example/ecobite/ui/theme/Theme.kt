package com.example.ecobite.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light colour scheme ───────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = Color.Black,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFEDEDED),
    onPrimaryContainer   = Color.Black,

    secondary            = Color.Black,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFEDEDED),
    onSecondaryContainer = Color.Black,

    tertiary             = Color.Black,
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFEDEDED),
    onTertiaryContainer  = Color.Black,

    background           = Color.White,
    onBackground         = Color.Black,

    surface              = Color.White,
    onSurface            = Color.Black,
    surfaceVariant       = Color(0xFFF5F5F5),
    onSurfaceVariant     = Color.DarkGray,

    outline              = Color(0xFFDDDDDD),
    outlineVariant       = Color(0xFFE0E0E0),

    error                = Error40,
    onError              = Color.White,
    errorContainer       = Error90,
    onErrorContainer     = Error40,

    inverseSurface       = Color.Black,
    inverseOnSurface     = Color.White,
    inversePrimary       = Color.Black,
)

// ── Dark colour scheme (AMOLED + off-white) ───────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFFEAEAEA), // 👈 off-white
    onPrimary            = Color.Black,
    primaryContainer     = Color(0xFF1A1A1A),
    onPrimaryContainer   = Color(0xFFEAEAEA),

    secondary            = Color(0xFFE0E0E0),
    onSecondary          = Color.Black,
    secondaryContainer   = Color(0xFF1A1A1A),
    onSecondaryContainer = Color(0xFFE0E0E0),

    tertiary             = Color(0xFFDADADA),
    onTertiary           = Color.Black,
    tertiaryContainer    = Color(0xFF1A1A1A),
    onTertiaryContainer  = Color(0xFFDADADA),

    background           = Color.Black,
    onBackground         = Color(0xFFEAEAEA), // 👈 key fix

    surface              = Color.Black,
    onSurface            = Color(0xFFEAEAEA),

    surfaceVariant       = Color(0xFF121212),
    onSurfaceVariant     = Color(0xFFB0B0B0), // softer gray

    outline              = Color(0xFF2A2A2A),
    outlineVariant       = Color(0xFF1F1F1F),

    error                = Color(0xFFFF6B6B), // softer red
    onError              = Color.Black,
    errorContainer       = Color(0xFF2A1212),
    onErrorContainer     = Color(0xFFFFB3B3),

    inverseSurface       = Color(0xFFEAEAEA),
    inverseOnSurface     = Color.Black,
    inversePrimary       = Color(0xFFEAEAEA),
)

// ── Theme ─────────────────────────────────────────────────────────────────────
@Composable
fun EcoBiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}