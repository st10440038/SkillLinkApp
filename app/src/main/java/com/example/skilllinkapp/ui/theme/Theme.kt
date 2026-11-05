package com.example.skilllinkapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6F5AE8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAE5FF),
    onPrimaryContainer = Color(0xFF281B68),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9E3F4),
    onSecondaryContainer = Color(0xFF292438),
    background = Color(0xFFF8F7FC),
    onBackground = Color(0xFF242238),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF242238),
    surfaceVariant = Color(0xFFF0EEF7),
    onSurfaceVariant = Color(0xFF625F73),
    outline = Color(0xFFD9D5E5),
    error = Color(0xFFB3263A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB6A9FF),
    onPrimary = Color(0xFF25165F),
    primaryContainer = Color(0xFF403477),
    onPrimaryContainer = Color(0xFFE9E3FF),
    secondary = Color(0xFFCCC3DF),
    onSecondary = Color(0xFF332D43),
    secondaryContainer = Color(0xFF484158),
    onSecondaryContainer = Color(0xFFEAE4F7),
    background = Color(0xFF11121A),
    onBackground = Color(0xFFF1EFF8),
    surface = Color(0xFF1A1C27),
    onSurface = Color(0xFFF1EFF8),
    surfaceVariant = Color(0xFF282A38),
    onSurfaceVariant = Color(0xFFC2BED0),
    outline = Color(0xFF494B5B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun SkillLinkAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
