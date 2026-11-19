package com.virtue.habittracker.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared semantic icon + glowing accent color mapping for habit titles across Home and History.
 */
fun habitVisualForTitle(title: String): Pair<ImageVector, Color> = when {
    title.contains("workout", true) || title.contains("exercise", true) || title.contains("gym", true) ->
        Icons.Filled.FitnessCenter to Color(0xFFC4B5FD)
    title.contains("read", true) || title.contains("book", true) || title.contains("study", true) ->
        Icons.AutoMirrored.Filled.MenuBook to Color(0xFFA78BFA)
    title.contains("water", true) || title.contains("drink", true) || title.contains("hydrat", true) ->
        Icons.Filled.WaterDrop to Color(0xFF38BDF8)
    title.contains("meditat", true) || title.contains("mind", true) || title.contains("yoga", true) ->
        Icons.Filled.SelfImprovement to Color(0xFF00DC82)
    title.contains("code", true) || title.contains("kotlin", true) || title.contains("dev", true) || title.contains("program", true) ->
        Icons.Filled.Code to Color(0xFFC4B5FD)
    title.contains("walk", true) || title.contains("run", true) || title.contains("step", true) ->
        Icons.AutoMirrored.Filled.DirectionsRun to Color(0xFF34D399)
    else ->
        Icons.Filled.Star to Color(0xFFFBBF24)
}

@Composable
fun StatusSummaryCard(
    label: String,
    count: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    symbol: String
) {
    Card(
        modifier = modifier.height(88.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.10f),
                            Color(0xFF1E1C2A)
                        )
                    )
                )
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Circular glowing status icon
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        color = accent.copy(alpha = 0.16f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }

            // Status count
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            // Status label
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                lineHeight = 10.sp,
                letterSpacing = 0.6.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
