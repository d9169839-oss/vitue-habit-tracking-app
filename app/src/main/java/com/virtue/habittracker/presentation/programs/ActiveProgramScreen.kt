package com.virtue.habittracker.presentation.programs

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtue.habittracker.domain.model.program.ProgramActivityStatus
import com.virtue.habittracker.domain.model.program.ProgramProgress
import com.virtue.habittracker.domain.model.program.ProgramStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActiveProgramScreen(
    progress: ProgramProgress,
    onBack: () -> Unit,
    onAdjust: () -> Unit,
    onActivityStatus: (String, ProgramActivityStatus) -> Unit,
    onProgramStatus: (ProgramStatus) -> Unit,
    onDelete: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = (progress.progressPercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "activeProgramProgress"
    )

    val statusAccent = when (progress.enrollment.status) {
        ProgramStatus.ACTIVE -> Color(0xFF00DC82)
        ProgramStatus.PAUSED -> Color(0xFFFBBF24)
        ProgramStatus.COMPLETED -> Color(0xFFA78BFA)
        else -> ProgramMuted
    }

    Scaffold(
        containerColor = Color(0xFF12111A),
        topBar = {
            TopAppBar(
                title = { Text("Active Program", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF12111A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        progress.enrollment.titleSnapshot,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusAccent.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, statusAccent.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusAccent, CircleShape)
                            )
                            Text(
                                text = progress.enrollment.status.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = statusAccent,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "${progress.progressPercent}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00DC82)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "PROGRAM PROGRESS",
                    color = ProgramLavender,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Color(0xFF00DC82),
                    trackColor = Color(0xFF1E1C2A)
                )
                Text(
                    "${progress.completedCount} of ${progress.totalCount} scheduled sessions completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = ProgramMuted
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.2.dp, Color(0xFFA78BFA).copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ProgramCardGradient, RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TODAY'S SESSION",
                            color = ProgramLavender,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                            color = Color(0xFFB5AFC7),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    val today = progress.todayActivities
                    if (today.isEmpty()) {
                        Text(
                            "There are no scheduled activities today. Check the timeline below for your next day.",
                            color = ProgramMuted
                        )
                    } else today.forEach { activity ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        activity.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(activity.instructions, color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF28233C),
                                    border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = if (activity.estimatedMinutes == 0) "Rest" else "${activity.estimatedMinutes} min",
                                        color = ProgramLavender,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (activity.estimatedMinutes == 0) {
                                Text("Recovery day · no workout required", color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    FilterChip(
                                        selected = activity.status == ProgramActivityStatus.COMPLETED,
                                        onClick = { onActivityStatus(activity.id, ProgramActivityStatus.COMPLETED) },
                                        label = { Text("✓ Done", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF0D3325),
                                            selectedLabelColor = Color(0xFF00DC82)
                                        )
                                    )
                                    FilterChip(
                                        selected = activity.status == ProgramActivityStatus.SKIPPED,
                                        onClick = { onActivityStatus(activity.id, ProgramActivityStatus.SKIPPED) },
                                        label = { Text("✕ Skip", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF381418),
                                            selectedLabelColor = Color(0xFFFF5252)
                                        )
                                    )
                                    if (activity.status != ProgramActivityStatus.PENDING) {
                                        TextButton(onClick = { onActivityStatus(activity.id, ProgramActivityStatus.PENDING) }) {
                                            Text("Reset", color = ProgramLavender)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Text(
                "PROGRAM TIMELINE",
                color = ProgramLavender,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            val todayEpochDay = LocalDate.now().toEpochDay()
            progress.activities.filter { it.epochDay >= todayEpochDay - 2L }.take(21).forEach { activity ->
                val isToday = activity.epochDay == todayEpochDay
                val formattedDate = LocalDate.ofEpochDay(activity.epochDay).format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                val (badgeLabel, badgeTextColor, badgeBgColor, badgeBorderColor) = when {
                    activity.status == ProgramActivityStatus.COMPLETED -> Quad(
                        "✓ Completed",
                        Color(0xFF00DC82),
                        Color(0xFF0D3325),
                        Color(0xFF00DC82).copy(alpha = 0.45f)
                    )
                    activity.status == ProgramActivityStatus.SKIPPED -> Quad(
                        "✕ Skipped",
                        Color(0xFFFF5252),
                        Color(0xFF381418),
                        Color(0xFFFF5252).copy(alpha = 0.45f)
                    )
                    isToday -> Quad(
                        "• Today",
                        Color(0xFFC4B5FD),
                        Color(0xFF2B2242),
                        Color(0xFFA78BFA).copy(alpha = 0.55f)
                    )
                    else -> Quad(
                        "Upcoming",
                        Color(0xFF9E98B3),
                        Color(0xFF221F30),
                        Color(0xFF36324A)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        width = if (isToday) 1.5.dp else 1.dp,
                        color = when {
                            isToday -> Color(0xFFA78BFA)
                            activity.status == ProgramActivityStatus.COMPLETED -> Color(0xFF00DC82).copy(alpha = 0.4f)
                            else -> Color(0xFF383245)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ProgramCardGradient, RoundedCornerShape(18.dp))
                            .padding(15.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Day ${activity.dayIndex} · $formattedDate",
                                color = if (isToday) Color(0xFFC4B5FD) else ProgramLavender,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(activity.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text(activity.phaseTitle, color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = badgeBgColor,
                            border = BorderStroke(1.dp, badgeBorderColor)
                        ) {
                            Text(
                                text = badgeLabel,
                                color = badgeTextColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onAdjust,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA78BFA),
                    contentColor = Color(0xFF140E21)
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text("Adjust remaining schedule", fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onProgramStatus(if (progress.enrollment.status == ProgramStatus.PAUSED) ProgramStatus.ACTIVE else ProgramStatus.PAUSED) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(if (progress.enrollment.status == ProgramStatus.PAUSED) "Resume" else "Pause")
                }
                OutlinedButton(
                    onClick = { onProgramStatus(ProgramStatus.COMPLETED) },
                    modifier = Modifier.weight(1f),
                    enabled = progress.enrollment.status != ProgramStatus.COMPLETED,
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Finish")
                }
            }
            TextButton(onClick = { showDelete = true }, Modifier.fillMaxWidth()) {
                Text("End and remove program", color = Color(0xFFFF5252), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.padding(bottom = 16.dp))
        }
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor = Color(0xFF1E1C2A),
            titleContentColor = Color.White,
            textContentColor = ProgramMuted,
            title = { Text("Remove this program?", fontWeight = FontWeight.Bold) },
            text = { Text("Your local program history and pending sync changes for this enrollment will be removed. This cannot be undone from the app.") },
            confirmButton = {
                TextButton(onClick = { showDelete = false; onDelete() }) {
                    Text("Remove", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text("Cancel", color = ProgramLavender)
                }
            }
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
