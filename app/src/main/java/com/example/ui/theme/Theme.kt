package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WaqtiPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0C243B),
    onPrimaryContainer = WaqtiCyanLight,
    secondary = WaqtiSecondaryBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0E2C44),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = WaqtiSecondaryGreen,
    onTertiary = Color.White,
    background = WaqtiDarkBackground,
    onBackground = WaqtiPrimaryTextDark,
    surface = WaqtiDarkCardBg,
    onSurface = WaqtiPrimaryTextDark,
    surfaceVariant = WaqtiDarkSecondaryCard,
    onSurfaceVariant = WaqtiSecondaryTextDark,
    outline = WaqtiDarkBorder,
    outlineVariant = Color(0xFF334155),
    error = WaqtiDanger,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = WaqtiPrimary,
    onPrimary = Color.White,
    primaryContainer = WaqtiPrimaryContainer,
    onPrimaryContainer = WaqtiOnPrimaryContainer,
    secondary = WaqtiPrimaryVariant,
    onSecondary = Color.White,
    background = WaqtiBackgroundLight,
    onBackground = WaqtiTextPrimaryLight,
    surface = WaqtiSurfaceLight,
    onSurface = WaqtiTextPrimaryLight,
    surfaceVariant = WaqtiSurfaceVariantLight,
    onSurfaceVariant = WaqtiTextSecondaryLight,
    outline = WaqtiBorderLight,
    error = WaqtiDanger,
    onError = Color.White
)

@Composable
fun WaqtiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve brand Deep Indigo identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = WaqtiTheme(darkTheme, dynamicColor, content)
