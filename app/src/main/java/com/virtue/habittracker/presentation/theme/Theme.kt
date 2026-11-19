package com.virtue.habittracker.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Centralized Virtue palette with glowing accents and unified obsidian-violet surfaces.
private val VirtueColors = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF140E21),
    primaryContainer = Color(0xFF2E2448),
    onPrimaryContainer = Color(0xFFEDE5FF),
    secondary = Color(0xFF00DC82),
    onSecondary = Color(0xFF05291A),
    secondaryContainer = Color(0xFF0D3325),
    onSecondaryContainer = Color(0xFFB8F7DF),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF2B1D00),
    tertiaryContainer = Color(0xFF362812),
    onTertiaryContainer = Color(0xFFFDE68A),
    background = Color(0xFF12111A),
    surface = Color(0xFF1E1C2A),
    surfaceVariant = Color(0xFF282538),
    onBackground = Color(0xFFF8F5FF),
    onSurface = Color(0xFFF8F5FF),
    onSurfaceVariant = Color(0xFF9E98B3),
    outline = Color(0xFF36324A),
    error = Color(0xFFFF5252),
    onError = Color(0xFF3B0714),
    errorContainer = Color(0xFF381418),
    onErrorContainer = Color(0xFFFFB4AB)
)

@Composable
fun VirtueHabitTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = VirtueColors, content = content)
}

@Composable
fun VitueHabitTheme(content: @Composable () -> Unit) = VirtueHabitTheme(content)
