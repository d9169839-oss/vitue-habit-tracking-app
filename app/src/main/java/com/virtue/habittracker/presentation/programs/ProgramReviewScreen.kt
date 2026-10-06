package com.virtue.habittracker.presentation.programs

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.ScheduledProgramActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun ProgramReviewScreen(
    template: ProgramTemplate,
    preferences: ProgramPreferences,
    startEpochDay: Long,
    previewActivities: List<ScheduledProgramActivity>,
    isAdjusting: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val active = previewActivities.count { it.estimatedMinutes > 0 }
    val restDays = previewActivities.size - active
    val preview = previewActivities.take(7)
    Scaffold(topBar = {
        TopAppBar(title = { Text(if (isAdjusting) "Review changes" else "Review your plan") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 18.dp, vertical = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = ProgramCard)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(template.title, color = ColorWhite, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(if (isAdjusting) "Completed history remains unchanged. Only the remaining schedule is revised." else "Your schedule is generated deterministically from the settings below.", color = ProgramMuted)
                    Text(
                        if (isAdjusting) "Changes apply from today. Original start: " + LocalDate.ofEpochDay(startEpochDay).format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                        else "Starts " + LocalDate.ofEpochDay(startEpochDay).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                        color = ProgramLavender
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Summary("Time", preferences.availableMinutesPerDay.toString() + " min", Modifier.weight(1f))
                Summary("Days", preferences.availableDaysPerWeek.toString() + "/wk", Modifier.weight(1f))
                Summary("Level", preferences.experience.displayName(), Modifier.weight(1f))
            }
            Text("NEXT 7 DAYS", color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            preview.forEach { activity ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Day " + activity.dayIndex + " · " + LocalDate.ofEpochDay(activity.epochDay).format(DateTimeFormatter.ofPattern("EEE, d MMM")), style = MaterialTheme.typography.labelMedium, color = ProgramLavender)
                            Text(activity.title, fontWeight = FontWeight.SemiBold)
                            Text(activity.instructions, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(if (activity.estimatedMinutes == 0) "Rest" else activity.estimatedMinutes.toString() + " min", color = ProgramMuted, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Text(active.toString() + " active days · " + restDays + " easier/rest days across the full " + template.durationDays + "-day plan.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Button(onClick = onConfirm, Modifier.fillMaxWidth(), enabled = !isSaving) { Text(if (isSaving) "Saving…" else if (isAdjusting) "Apply changes" else "Start program") }
            TextButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("Go back and edit") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private val ColorWhite = androidx.compose.ui.graphics.Color(0xFFF8F5FF)

@Composable
private fun Summary(label: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
