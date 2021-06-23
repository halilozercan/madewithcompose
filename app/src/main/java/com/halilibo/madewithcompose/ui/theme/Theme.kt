package com.halilibo.madewithcompose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.halilibo.colors.*

private val DarkColorPalette = darkColors(
    primary = B400,
    primaryVariant = B400,
    secondary = N800,
    background = DN20,
    surface = DN40,
    onPrimary = N20,
    onSecondary = N0,
    onBackground = DN800,
)

private val LightColorPalette = lightColors(
    primary = B400,
    primaryVariant = B400,
    secondary = N800,
    background = N30,
    surface = N0,
    onPrimary = N20,
    onSecondary = N0,
    onBackground = N800,
)


@Composable
fun MadeWithComposeTheme(
    content: @Composable () -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()
    val nightMode = remember { NightMode(isDarkMode) }

    val colors = if (nightMode.isNight) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    CompositionLocalProvider(LocalNightMode provides nightMode) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}