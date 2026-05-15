package com.snoffee.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Light Color Scheme ─────────────────────────────────────────────────
private val SnoffeeLightColorScheme = lightColorScheme(
    primary = SnoffeePrimary,
    onPrimary = SnoffeeSurface,
    primaryContainer = SnoffeePrimarySubtle,
    onPrimaryContainer = SnoffeePrimaryDark,

    secondary = SnoffeePrimaryLight,
    onSecondary = SnoffeeSurface,
    secondaryContainer = SnoffeePrimaryTint,
    onSecondaryContainer = SnoffeePrimaryDark,

    background = SnoffeeBgBase,
    onBackground = SnoffeeTextMain,

    surface = SnoffeeSurface,
    onSurface = SnoffeeTextMain,
    surfaceVariant = SnoffeeSurfaceOverlay,
    onSurfaceVariant = SnoffeeTextMuted,

    outline = SnoffeeBorder,
    outlineVariant = SnoffeeDivider,

    error = SnoffeeError,
    onError = SnoffeeSurface,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = SnoffeeError,

    inverseSurface = SnoffeeTextMain,
    inverseOnSurface = SnoffeeSurface,
    inversePrimary = SnoffeePrimaryTint,
)

// ── Dark Color Scheme (추후 다크모드 대응용) ────────────────────────────
private val SnoffeeDarkColorScheme = darkColorScheme(
    primary = SnoffeePrimaryLight,
    onPrimary = SnoffeeTextMain,
    primaryContainer = SnoffeePrimaryDark,
    onPrimaryContainer = SnoffeePrimarySubtle,

    secondary = SnoffeePrimaryTint,
    onSecondary = SnoffeeTextMain,
    secondaryContainer = SnoffeePrimaryDark,
    onSecondaryContainer = SnoffeePrimaryTint,

    background = Color(0xFF1C1713),
    onBackground = Color(0xFFEDE0D4),

    surface = Color(0xFF26201A),
    onSurface = Color(0xFFEDE0D4),
    surfaceVariant = Color(0xFF332B24),
    onSurfaceVariant = Color(0xFFCFBFB2),

    outline = Color(0xFF7A6A5E),
    outlineVariant = Color(0xFF4A3C32),

    error = Color(0xFFCF8080),
    onError = Color(0xFF1C0000),
)

// ── Custom Color Extensions ────────────────────────────────────────────
data class SnoffeeExtendedColors(
    val bgWarm: Color,
    val bgMuted: Color,
    val bgDim: Color,
    val surfaceElevated: Color,
    val surfacePressed: Color,
    val shadowLight: Color,
    val shadowDark: Color,
    val textHint: Color,
    val textDisabled: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val scoreCardBg: Color,
    val barChartPrimary: Color,
    val barChartSecondary: Color,
    val sleepStageDeep: Color,
    val sleepStageLight: Color,
    val sleepStageRem: Color,
    val caffeineSafe: Color,
    val caffeineAlert: Color,
    val caffeineOver: Color,
)

val LocalSnoffeeExtendedColors = staticCompositionLocalOf {
    SnoffeeExtendedColors(
        bgWarm = SnoffeeBgWarm,
        bgMuted = SnoffeeBgMuted,
        bgDim = SnoffeeBgDim,
        surfaceElevated = SnoffeeSurfaceElevated,
        surfacePressed = SnoffeeSurfacePressed,
        shadowLight = SnoffeeShadowLight,
        shadowDark = SnoffeeShadowDark,
        textHint = SnoffeeTextHint,
        textDisabled = SnoffeeTextDisabled,
        success = SnoffeeSuccess,
        warning = SnoffeeWarning,
        info = SnoffeeInfo,
        scoreCardBg = ScoreCardBg,
        barChartPrimary = BarChartPrimary,
        barChartSecondary = BarChartSecondary,
        sleepStageDeep = SleepStageDeep,
        sleepStageLight = SleepStageLight,
        sleepStageRem = SleepStageRem,
        caffeineSafe = CaffeineSafe,
        caffeineAlert = CaffeineAlert,
        caffeineOver = CaffeineOver,
    )
}

// ── Theme Entry Point ──────────────────────────────────────────────────
val LocalSnoffeeColorScheme = staticCompositionLocalOf { SnoffeeLightColorScheme }

@Composable
fun SnoffeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SnoffeeDarkColorScheme else SnoffeeLightColorScheme

    CompositionLocalProvider(
        LocalSnoffeeColorScheme provides colorScheme, // 우리 색상 주입
        LocalSnoffeeExtendedColors provides LocalSnoffeeExtendedColors.current
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// ── Theme Accessor ─────────────────────────────────────────────────────
object SnoffeeTheme {
    val colorScheme: ColorScheme
        @Composable get() = LocalSnoffeeColorScheme.current

    val colors: SnoffeeExtendedColors
        @Composable get() = LocalSnoffeeExtendedColors.current
}