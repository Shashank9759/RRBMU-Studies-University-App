package com.studies.rrbmustudies.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.studies.rrbmustudies.domain.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = StitchPrimary,
    onPrimary = StitchOnPrimary,
    primaryContainer = StitchPrimaryContainer,
    onPrimaryContainer = StitchOnPrimaryContainer,
    secondary = StitchSecondary,
    onSecondary = StitchOnPrimary,
    secondaryContainer = StitchSecondaryContainer,
    onSecondaryContainer = StitchOnPrimary,
    tertiary = StitchTertiary,
    onTertiary = StitchOnPrimary,
    tertiaryContainer = StitchTertiaryContainer,
    onTertiaryContainer = StitchPrimaryFixed,
    background = StitchSurface,
    onBackground = StitchOnSurface,
    surface = StitchSurface,
    onSurface = StitchOnSurface,
    surfaceVariant = StitchSurfaceVariant,
    onSurfaceVariant = StitchOnSurfaceVariant,
    surfaceContainerLowest = StitchSurfaceContainerLowest,
    surfaceContainerLow = StitchSurfaceContainerLow,
    surfaceContainer = StitchSurfaceContainer,
    surfaceContainerHigh = StitchSurfaceContainerHigh,
    surfaceContainerHighest = StitchSurfaceContainerHigh,
    outline = StitchOutline,
    outlineVariant = StitchOutlineVariant,
    error = StitchError,
    errorContainer = StitchErrorContainer,
    onError = StitchOnPrimary,
    inversePrimary = StitchPrimaryFixed,
)

private val DarkColorScheme = darkColorScheme(
    primary = StitchDarkPrimary,
    onPrimary = Color(0xFF1A237E),
    primaryContainer = StitchPrimaryContainer,
    onPrimaryContainer = StitchDarkPrimary,
    secondary = StitchSecondaryContainer,
    onSecondary = StitchOnPrimary,
    secondaryContainer = Color(0xFFFF6D00),
    onSecondaryContainer = StitchOnPrimary,
    tertiary = Color(0xFFBAC3FF),
    onTertiary = Color(0xFF00105C),
    tertiaryContainer = StitchTertiaryContainer,
    background = StitchDarkSurface,
    onBackground = StitchDarkOnSurface,
    surface = StitchDarkSurface,
    onSurface = StitchDarkOnSurface,
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = StitchDarkOnSurfaceVariant,
    surfaceContainerLowest = Color(0xFF0A0A0A),
    surfaceContainerLow = StitchDarkSurfaceContainerLow,
    surfaceContainer = StitchDarkSurfaceContainer,
    surfaceContainerHigh = StitchDarkSurfaceContainerHigh,
    surfaceContainerHighest = Color(0xFF33353A),
    outline = Color(0xFF8E9099),
    outlineVariant = StitchDarkOutlineVariant,
    error = StitchError,
    errorContainer = Color(0xFFFFDAD6),
    onError = StitchOnPrimary,
    inversePrimary = Color(0xFF4C56AF),
)

@Composable
fun RrbmuTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = rrbmuTypography(),
        shapes = RrbmuShapes,
        content = content,
    )
}
