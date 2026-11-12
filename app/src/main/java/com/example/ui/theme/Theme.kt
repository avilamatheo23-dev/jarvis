package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF001F29),
    primaryContainer = JarvisDeepBlue,
    onPrimaryContainer = JarvisCyan,
    secondary = JarvisElectricBlue,
    onSecondary = Color(0xFF001F29),
    secondaryContainer = Color(0xFF082645),
    onSecondaryContainer = JarvisTextPrimary,
    tertiary = JarvisNeonGlow,
    onTertiary = Color(0xFF00201A),
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorder,
    outlineVariant = Color(0xFF103456)
)

@Composable
fun JarvisTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = JarvisTypography,
        content = content
    )
}

// Backward-compatible alias
@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    JarvisTheme(content = content)
}
