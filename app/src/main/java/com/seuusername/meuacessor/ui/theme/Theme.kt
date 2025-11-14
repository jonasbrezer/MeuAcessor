package com.seuusername.meuacessor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NectarPrimary,
    onPrimary = Color.Black,
    secondary = NectarSecondary,
    background = Color(0xFF111315),
    surface = Color(0xFF1B1E22),
    onSurface = Color(0xFFE8EAED)
)

private val LightColorScheme = lightColorScheme(
    primary = NectarPrimary,
    onPrimary = Color.White,
    primaryContainer = NectarPrimaryContainer,
    secondary = NectarSecondary,
    onSecondary = Color.White,
    background = NectarBackground,
    onBackground = NectarOnSurface,
    surface = NectarSurface,
    onSurface = NectarOnSurface,
    surfaceVariant = Color(0xFFE3F2FD),
    outline = Color(0xFFCFD8DC)
)

@Composable
fun MeuAcessorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
