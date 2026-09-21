package com.example.skilllinkapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SkillLinkPurple,
    onPrimary = SkillLinkWhite,

    primaryContainer = SkillLinkLightPurple,
    onPrimaryContainer = SkillLinkNavy,

    secondary = SkillLinkDarkPurple,
    onSecondary = SkillLinkWhite,

    secondaryContainer = SkillLinkSoftPurple,
    onSecondaryContainer = SkillLinkNavy,

    background = SkillLinkBackground,
    onBackground = SkillLinkNavy,

    surface = SkillLinkWhite,
    onSurface = SkillLinkNavy,

    surfaceVariant = SkillLinkCardBackground,
    onSurfaceVariant = SkillLinkGreyText,

    outline = SkillLinkBorder,

    error = SkillLinkError,
    onError = SkillLinkWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = SkillLinkPurple,
    onPrimary = SkillLinkWhite,

    primaryContainer = SkillLinkDarkPurple,
    onPrimaryContainer = SkillLinkWhite,

    secondary = SkillLinkLightPurple,
    onSecondary = SkillLinkNavy,

    secondaryContainer = SkillLinkDarkPurple,
    onSecondaryContainer = SkillLinkWhite,

    background = SkillLinkNavy,
    onBackground = SkillLinkWhite,

    surface = SkillLinkNavy,
    onSurface = SkillLinkWhite,

    surfaceVariant = SkillLinkDarkPurple,
    onSurfaceVariant = SkillLinkWhite,

    outline = SkillLinkBorder,

    error = SkillLinkError,
    onError = SkillLinkWhite
)

@Composable
fun SkillLinkAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}