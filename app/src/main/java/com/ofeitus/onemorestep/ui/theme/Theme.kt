package com.ofeitus.onemorestep.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightMonochromeScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    background = PureWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,
    secondary = GrayText
)

private val DarkMonochromeScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    background = PureBlack,
    onBackground = PureWhite,
    surface = PureBlack,
    onSurface = PureWhite,
    secondary = GrayText
)

@Composable
fun BWTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkMonochromeScheme else LightMonochromeScheme

    MaterialTheme(
        typography = CustomTypography,
        colorScheme = colorScheme,
        content = content
    )
}