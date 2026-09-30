package com.virtue.habittracker.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Centralized Vitue palette. Screen-specific accents should reuse these semantic colors.
private val VitueColors = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF171020),
    primaryContainer = Color(0xFF302643),
    onPrimaryContainer = Color(0xFFEDE5FF),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF062B20),
    secondaryContainer = Color(0xFF15392F),
    onSecondaryContainer = Color(0xFFB8F7DF),
    background = Color(0xFF100D18),
    surface = Color(0xFF211B30),
    surfaceVariant = Color(0xFF29213A),
    onSurface = Color(0xFFF8F5FF),
    onSurfaceVariant = Color(0xFFAAA2BB),
    outline = Color(0xFF393047),
    error = Color(0xFFFB7185),
    onError = Color(0xFF3B0714)
)

@Composable
fun VitueHabitTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = VitueColors, content = content)
}
