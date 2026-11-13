package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Navy900,
    onPrimary = PureWhite,
    primaryContainer = Navy100,
    onPrimaryContainer = Navy900,
    secondary = Gold600,
    onSecondary = PureWhite,
    secondaryContainer = Gold100,
    onSecondaryContainer = Navy900,
    tertiary = Navy700,
    onTertiary = PureWhite,
    background = IvoryBackground,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    error = CrimsonError,
    onError = PureWhite,
    errorContainer = CrimsonSurface,
    onErrorContainer = CrimsonError
)

private val DarkColors = darkColorScheme(
    primary = Gold400,
    onPrimary = Navy950,
    primaryContainer = Navy800,
    onPrimaryContainer = Gold100,
    secondary = Gold500,
    onSecondary = Navy950,
    secondaryContainer = Navy700,
    onSecondaryContainer = Gold100,
    tertiary = Navy100,
    onTertiary = Navy950,
    background = Navy950,
    onBackground = PureWhite,
    surface = Navy900,
    onSurface = PureWhite,
    surfaceVariant = Navy800,
    onSurfaceVariant = Slate300,
    outline = Navy700,
    error = CrimsonSurface,
    onError = CrimsonError,
    errorContainer = CrimsonError,
    onErrorContainer = PureWhite
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AskariTakafulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isUrdu: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val typography = createAppTypography(isUrdu = isUrdu)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = AppShapes,
        content = content
    )
}
