package com.batb4016.tinynext.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    secondary = Color(0xFF546E7A),
    tertiary = Color(0xFF7B5E2A),
    background = Color(0xFFFAFDF7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7EFE2)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CD67D),
    onPrimary = Color(0xFF08380E),
    secondary = Color(0xFFB7C8D1),
    tertiary = Color(0xFFE2C47B),
    background = Color(0xFF111510),
    surface = Color(0xFF1A1F18),
    surfaceVariant = Color(0xFF354034)
)

@Composable
fun TinyNextTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme = if (darkTheme) DarkColors else LightColors,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

