package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisDarkColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = JarvisDeepBlue,
    onPrimaryContainer = JarvisCyanLight,
    secondary = JarvisBlue,
    onSecondary = Color.White,
    secondaryContainer = JarvisSurfaceElevated,
    onSecondaryContainer = JarvisCyanLight,
    tertiary = JarvisEmerald,
    onTertiary = Color.Black,
    error = JarvisRedAlert,
    onError = Color.White,
    background = JarvisBackground,
    onBackground = TextPrimary,
    surface = JarvisSurface,
    onSurface = TextPrimary,
    surfaceVariant = JarvisSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = JarvisSurfaceBorder,
    outlineVariant = JarvisCyanGlow
)

private val JarvisLightColorScheme = lightColorScheme(
    primary = Color(0xFF006877),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5EEFC),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF00629E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFE5FF),
    onSecondaryContainer = Color(0xFF001D35),
    tertiary = Color(0xFF006C3B),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to futuristic dark HUD aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) JarvisDarkColorScheme else JarvisLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
