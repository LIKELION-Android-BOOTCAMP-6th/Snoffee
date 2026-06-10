package com.snoffee.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val SnoffeePrimary = Color(0xFFA0784A)
val SnoffeeBgBase = Color(0xFF000000)
val SnoffeeSurface = Color(0xFF1C1A17)
val SnoffeeTextMain = Color(0xFFF4F3F1)
val SnoffeeTextMuted = Color(0xFFD2D0CB)
val SnoffeeError = Color(0xFFA85454)

val WearColorPalette = Colors(
    primary = SnoffeePrimary,
    background = SnoffeeBgBase,
    surface = SnoffeeSurface,
    onBackground = SnoffeeTextMain,
    onSurface = SnoffeeTextMuted,
    error = SnoffeeError,
    onPrimary = Color.White
)

@Composable
fun WearSnoffeeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = WearColorPalette, content = content)
}