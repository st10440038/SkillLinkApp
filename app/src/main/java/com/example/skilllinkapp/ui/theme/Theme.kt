package com.example.skilllinkapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = SkillLinkPurple,
    onPrimary = SkillLinkWhite,

    primaryContainer = SkillLinkLightPurple,
    onPrimaryContainer = SkillLinkNavy,

    secondary = SkillLinkDarkPurple,
    onSecondary = SkillLinkWhite,

    secondaryContainer = SkillLinkLightPurple,
    onSecondaryContainer = SkillLinkNavy,

    background = SkillLinkBackground,
    onBackground = SkillLinkDarkText,

    surface = SkillLinkWhite,
    onSurface = SkillLinkDarkText,

    surfaceVariant = SkillLinkCardBackground,
    onSurfaceVariant = SkillLinkNavy,

    outline = SkillLinkBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = SkillLinkPurple,
    onPrimary = SkillLinkWhite,

    primaryContainer = SkillLinkDarkPurple,
    onPrimaryContainer = SkillLinkWhite,

    secondary = SkillLinkLightPurple,
    onSecondary = SkillLinkNavy,

    background = SkillLinkNavy,
    onBackground = SkillLinkWhite,

    surface = SkillLinkNavy,
    onSurface = SkillLinkWhite,

    surfaceVariant = SkillLinkDarkPurple,
    onSurfaceVariant = SkillLinkWhite,

    outline = SkillLinkBorder
)

@Composable
fun SkillLinkAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
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