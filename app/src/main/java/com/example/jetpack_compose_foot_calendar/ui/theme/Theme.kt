package com.example.jetpack_compose_foot_calendar.ui.theme

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

/** Dark colour scheme using the [Purple80] / [PurpleGrey80] / [Pink80] palette. */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/** Light colour scheme using the [Purple40] / [PurpleGrey40] / [Pink40] palette. */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * Root Material 3 theme for the application.
 *
 * Colour scheme selection priority:
 * 1. **Dynamic colour** (Android 12+ / API 31+): wallpaper-based tones pulled from the system.
 * 2. **Static dark scheme** ([DarkColorScheme]) when [darkTheme] is `true`.
 * 3. **Static light scheme** ([LightColorScheme]) otherwise.
 *
 * @param darkTheme     Whether to use the dark colour scheme. Defaults to the system setting.
 * @param dynamicColor  Whether to enable dynamic colour (Material You). Defaults to `true`.
 *                      Has no effect on devices running below Android 12 (API 31).
 * @param content       The composable content to render inside the theme.
 */
@Composable
fun JetPackComposeFootCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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