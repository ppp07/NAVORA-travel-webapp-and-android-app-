package com.navora.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = NavoraNavy,
    onPrimary = NavoraWhite,
    primaryContainer = NavoraBlue,
    onPrimaryContainer = NavoraInk,
    secondary = NavoraOrange,
    onSecondary = NavoraNavy,
    secondaryContainer = NavoraPeach,
    onSecondaryContainer = NavoraInk,
    tertiary = NavoraCoralAccent,
    onTertiary = NavoraWhite,
    tertiaryContainer = NavoraCoral,
    onTertiaryContainer = NavoraInk,
    background = NavoraCream,
    onBackground = NavoraInk,
    surface = NavoraCard,
    onSurface = NavoraInk,
    surfaceVariant = NavoraCreamDeep,
    onSurfaceVariant = NavoraMuted,
    outline = NavoraLine,
)

@Composable
fun NavoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
