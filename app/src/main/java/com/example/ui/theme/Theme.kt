package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun DaftarTheme(
    themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    colorTheme: String = "TEAL", // TEAL, NAVY, EMERALD, BRONZE
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val primaryColor = when (colorTheme) {
        "NAVY" -> if (isDark) Color(0xFF93C5FD) else NavyPrimary
        "EMERALD" -> if (isDark) Color(0xFF6EE7B7) else EmeraldPrimary
        "BRONZE" -> if (isDark) Color(0xFFFDE68A) else BronzePrimary
        else -> if (isDark) Color(0xFF67E8F9) else TealPrimary
    }

    val primaryContainerColor = when (colorTheme) {
        "NAVY" -> if (isDark) Color(0xFF1E3A8A) else NavyPrimaryContainer
        "EMERALD" -> if (isDark) Color(0xFF065F46) else EmeraldPrimaryContainer
        "BRONZE" -> if (isDark) Color(0xFF78350F) else BronzePrimaryContainer
        else -> if (isDark) Color(0xFF0E4C5F) else TealPrimaryContainer
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.Black,
            primaryContainer = primaryContainerColor,
            onPrimaryContainer = Color.White,
            secondary = SecondaryAmber,
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF78350F),
            onSecondaryContainer = Color(0xFFFEF3C7),
            tertiary = TertiarySlate,
            background = DarkBackground,
            onBackground = DarkTextPrimary,
            surface = DarkSurface,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkTextSecondary,
            outline = DarkOutline,
            error = StatusDebtRed,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryContainerColor,
            onPrimaryContainer = if (colorTheme == "TEAL") TealOnPrimaryContainer else Color(0xFF0F172A),
            secondary = SecondaryAmber,
            onSecondary = Color.White,
            secondaryContainer = SecondaryAmberContainer,
            onSecondaryContainer = Color(0xFF78350F),
            tertiary = TertiarySlate,
            background = LightBackground,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = LightTextSecondary,
            outline = LightOutline,
            error = StatusDebtRed,
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
