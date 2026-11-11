package com.virtue.habittracker.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun StatusSummaryCard(
    label: String,
    count: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    symbol: String
) {
    Card(
        modifier = modifier.height(85.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF211E29)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Circular status icon
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = Color(0xFF2A2834),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }

            // Status count
            Text(
                text = count.toString(),
                fontSize = 25.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )

            // Status label
            Text(
                text = label.uppercase(),
                fontSize = 12.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Bold,
                color = accent.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}