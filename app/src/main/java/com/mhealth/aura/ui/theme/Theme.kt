package com.mhealth.aura.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AuraColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueLight,
    onPrimaryContainer = BlueDark,
    secondary = TealPrimary,
    onSecondary = Color.White,
    secondaryContainer = TealLight,
    background = BackgroundApp,
    surface = CardWhite,
    onBackground = TextDark,
    onSurface = TextDark,
    outline = BorderColor,
    error = RedDanger
)

@Composable
fun AuraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AuraColorScheme,
        typography = AuraTypography,
        content = content
    )
}
