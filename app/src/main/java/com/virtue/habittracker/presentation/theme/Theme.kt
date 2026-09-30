package com.virtue.habittracker.presentation.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val VitueColors = darkColorScheme(
    primary = Color(0xFFB7A0FF), onPrimary = Color(0xFF211047), secondary = Color(0xFF7DE2C2),
    background = Color(0xFF0D0B14), surface = Color(0xFF171421), surfaceVariant = Color(0xFF282336),
    onSurface = Color(0xFFF5F1FF), onSurfaceVariant = Color(0xFFC6BED8), error = Color(0xFFFF6B78)
)
@Composable
fun VitueHabitTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = VitueColors, content = content)
}
