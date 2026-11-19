package com.virtue.habittracker.presentation.programs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.virtue.habittracker.domain.model.program.ProgramActivityStatus
import com.virtue.habittracker.domain.model.program.ProgramProgress
import com.virtue.habittracker.domain.model.program.ProgramStatus
import java.time.LocalDate

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
    Scaffold(
        containerColor = Color(0xFF12111A),
        topBar = {
            TopAppBar(
                title = { Text("Your program", fontWeight = FontWeight.Bold, color = Color.White) },
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
            Text(
                progress.enrollment.titleSnapshot,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                progress.enrollment.status.name.lowercase().replaceFirstChar { it.uppercase() },
                color = Color(0xFF00DC82),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "PROGRAM PROGRESS",
                color = ProgramLavender,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { progress.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF00DC82),
                trackColor = Color(0xFF1E1C2A)
            )
            Text(
                "${progress.progressPercent}% complete · ${progress.completedCount}/${progress.totalCount} activities",
                style = MaterialTheme.typography.bodySmall,
                color = ProgramMuted
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ProgramCardGradient, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "TODAY",
                        color = ProgramLavender,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val today = progress.todayActivities
                    if (today.isEmpty()) {
                        Text(
                            "There are no scheduled activities today. Check the timeline below for your next day.",
                            color = ProgramMuted
                        )
                    } else today.forEach { activity ->
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(activity.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text(activity.instructions, color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    if (activity.estimatedMinutes == 0) "Rest" else "${activity.estimatedMinutes} min",
                                    color = ProgramLavender,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (activity.estimatedMinutes == 0) {
                                Text("Recovery day · no workout required", color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = activity.status == ProgramActivityStatus.COMPLETED,
                                        onClick = { onActivityStatus(activity.id, ProgramActivityStatus.COMPLETED) },
                                        label = { Text("Done", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF0D3325),
                                            selectedLabelColor = Color(0xFF00DC82)
                                        )
                                    )
                                    FilterChip(
                                        selected = activity.status == ProgramActivityStatus.SKIPPED,
                                        onClick = { onActivityStatus(activity.id, ProgramActivityStatus.SKIPPED) },
                                        label = { Text("Skip", fontWeight = FontWeight.Bold) },
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
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            val todayEpochDay = LocalDate.now().toEpochDay()
            progress.activities.filter { it.epochDay >= todayEpochDay - 2L }.take(21).forEach { activity ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        1.dp,
                        if (activity.status == ProgramActivityStatus.COMPLETED) {
                            Color(0xFF00DC82).copy(alpha = 0.4f)
                        } else {
                            Color(0xFF383245)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ProgramCardGradient, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Day ${activity.dayIndex}", color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(activity.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text(activity.phaseTitle, color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = activity.status.name.lowercase(),
                            color = when (activity.status) {
                                ProgramActivityStatus.COMPLETED -> Color(0xFF00DC82)
                                ProgramActivityStatus.SKIPPED -> Color(0xFFFF5252)
                                ProgramActivityStatus.PENDING -> ProgramMuted
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Button(
                onClick = onAdjust,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA78BFA),
                    contentColor = Color(0xFF140E21)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Adjust remaining schedule", fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onProgramStatus(if (progress.enrollment.status == ProgramStatus.PAUSED) ProgramStatus.ACTIVE else ProgramStatus.PAUSED) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (progress.enrollment.status == ProgramStatus.PAUSED) "Resume" else "Pause")
                }
                OutlinedButton(
                    onClick = { onProgramStatus(ProgramStatus.COMPLETED) },
                    modifier = Modifier.weight(1f),
                    enabled = progress.enrollment.status != ProgramStatus.COMPLETED,
                    shape = RoundedCornerShape(14.dp)
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
