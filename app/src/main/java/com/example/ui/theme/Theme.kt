package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AgroGreenPrimaryDark,
    onPrimary = AgroGreenOnPrimaryDark,
    primaryContainer = AgroGreenContainerDark,
    onPrimaryContainer = AgroGreenOnContainerDark,
    secondary = SpiceOrangeSecondaryDark,
    onSecondary = SpiceOrangeOnSecondaryDark,
    secondaryContainer = SpiceOrangeContainerDark,
    onSecondaryContainer = SpiceOrangeOnContainerDark,
    tertiary = GoldenGrainContainer,
    onTertiary = GoldenGrainOnContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = AgroGreenPrimary,
    onPrimary = AgroGreenOnPrimary,
    primaryContainer = AgroGreenContainer,
    onPrimaryContainer = AgroGreenOnContainer,
    secondary = SpiceOrangeSecondary,
    onSecondary = SpiceOrangeOnSecondary,
    secondaryContainer = SpiceOrangeContainer,
    onSecondaryContainer = SpiceOrangeOnContainer,
    tertiary = GoldenGrainTertiary,
    onTertiary = GoldenGrainOnTertiary,
    background = WarmBackgroundLight,
    surface = WarmSurfaceLight,
    surfaceVariant = WarmSurfaceVariantLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight
)

@Composable
fun GrosGrainsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent agricultural branding by default
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
