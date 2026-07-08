package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    val dynamicScheme = darkColorScheme(
        primary = GoldPrimary,
        secondary = GoldAccent,
        tertiary = GoldDark,
        background = DeepBlack,
        surface = SurfaceDark,
        onPrimary = if (isDarkThemeState.value) DeepBlack else PureWhite,
        onSecondary = if (isDarkThemeState.value) DeepBlack else PureWhite,
        onTertiary = PureWhite,
        onBackground = PureWhite,
        onSurface = PureWhite,
        surfaceVariant = SurfaceLight,
        onSurfaceVariant = TextSecondary
    )

    MaterialTheme(
        colorScheme = dynamicScheme,
        typography = Typography,
        content = content
    )
}
