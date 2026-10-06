package com.virtue.habittracker.presentation.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.virtue.habittracker.domain.model.program.ProgramActivityStatus
import com.virtue.habittracker.domain.model.program.ProgramProgress
import com.virtue.habittracker.domain.model.program.ProgramStatus

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
    Scaffold(topBar = {
        TopAppBar(title = { Text("Your program") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 18.dp, vertical = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(progress.enrollment.titleSnapshot, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(progress.enrollment.status.name.lowercase().replaceFirstChar { it.uppercase() }, color = ProgramLavender)
            LinearProgressIndicator(progress = { progress.progressPercent / 100f }, modifier = Modifier.fillMaxWidth(), color = ProgramViolet)
            Text(progress.progressPercent.toString() + "% complete · " + progress.completedCount + "/" + progress.totalCount + " activities", style = MaterialTheme.typography.bodySmall, color = ProgramMuted)
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ProgramCard)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("TODAY", color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val today = progress.todayActivities
                    if (today.isEmpty()) {
                        Text("There are no scheduled activities today. Check the timeline below for your next day.", color = ProgramMuted)
                    } else today.forEach { activity ->
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(activity.title, fontWeight = FontWeight.SemiBold)
                                    Text(activity.instructions, color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(if (activity.estimatedMinutes == 0) "Rest" else activity.estimatedMinutes.toString() + " min", color = ProgramLavender, style = MaterialTheme.typography.labelMedium)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(selected = activity.status == ProgramActivityStatus.COMPLETED, onClick = { onActivityStatus(activity.id, ProgramActivityStatus.COMPLETED) }, label = { Text("Complete") })
                                FilterChip(selected = activity.status == ProgramActivityStatus.SKIPPED, onClick = { onActivityStatus(activity.id, ProgramActivityStatus.SKIPPED) }, label = { Text("Skip") })
                                if (activity.status != ProgramActivityStatus.PENDING) TextButton(onClick = { onActivityStatus(activity.id, ProgramActivityStatus.PENDING) }) { Text("Reset") }
                            }
                        }
                    }
                }
            }
            Text("PROGRAM TIMELINE", color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            progress.activities.take(21).forEach { activity ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Day " + activity.dayIndex, color = ProgramLavender, style = MaterialTheme.typography.labelMedium)
                            Text(activity.title, fontWeight = FontWeight.SemiBold)
                            Text(activity.phaseTitle, color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(activity.status.name.lowercase(), color = ProgramMuted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Button(onClick = onAdjust, Modifier.fillMaxWidth()) { Text("Adjust remaining schedule") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onProgramStatus(if (progress.enrollment.status == ProgramStatus.PAUSED) ProgramStatus.ACTIVE else ProgramStatus.PAUSED) }, Modifier.weight(1f)) {
                    Text(if (progress.enrollment.status == ProgramStatus.PAUSED) "Resume" else "Pause")
                }
                OutlinedButton(onClick = { onProgramStatus(ProgramStatus.COMPLETED) }, Modifier.weight(1f), enabled = progress.enrollment.status != ProgramStatus.COMPLETED) { Text("Finish") }
            }
            TextButton(onClick = { showDelete = true }, Modifier.fillMaxWidth()) { Text("End and remove program") }
            Spacer(Modifier.padding(bottom = 16.dp))
        }
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Remove this program?") },
            text = { Text("Your local program history and pending sync changes for this enrollment will be removed. This cannot be undone from the app.") },
            confirmButton = { TextButton(onClick = { showDelete = false; onDelete() }) { Text("Remove") } },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
        )
    }
}
