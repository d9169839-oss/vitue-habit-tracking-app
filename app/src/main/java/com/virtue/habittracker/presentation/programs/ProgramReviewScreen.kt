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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.ScheduledProgramActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        containerColor = Color(0xFF12111A),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isAdjusting) "Review changes" else "Review your plan",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ProgramCardGradient, RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(template.title, color = ColorWhite, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        if (isAdjusting) "Completed history remains unchanged. Only the remaining schedule is revised."
                        else "Your schedule is generated deterministically from the settings below.",
                        color = ProgramMuted
                    )
                    Text(
                        if (isAdjusting) "Changes apply from today. Original start: " + LocalDate.ofEpochDay(startEpochDay).format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                        else "Starts " + LocalDate.ofEpochDay(startEpochDay).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                        color = Color(0xFF00DC82),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Summary("Time", "${preferences.availableMinutesPerDay} min", Modifier.weight(1f))
                Summary("Days", "${preferences.availableDaysPerWeek}/wk", Modifier.weight(1f))
                Summary("Level", preferences.experience.displayName(), Modifier.weight(1f))
            }
            Text("NEXT 7 DAYS", color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            preview.forEach { activity ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color(0xFF383245))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ProgramCardGradient, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Day ${activity.dayIndex} · " + LocalDate.ofEpochDay(activity.epochDay).format(DateTimeFormatter.ofPattern("EEE, d MMM")),
                                style = MaterialTheme.typography.labelMedium,
                                color = ProgramLavender,
                                fontWeight = FontWeight.Bold
                            )
                            Text(activity.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text(activity.instructions, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            if (activity.estimatedMinutes == 0) "Rest" else "${activity.estimatedMinutes} min",
                            color = if (activity.estimatedMinutes == 0) ProgramMuted else Color(0xFF00DC82),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                "$active active days · $restDays easier/rest days across the full ${template.durationDays}-day plan.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00DC82),
                    contentColor = Color(0xFF05291A)
                )
            ) {
                if (isSaving) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFF05291A),
                            strokeWidth = 2.dp
                        )
                        Text("Saving…", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(if (isAdjusting) "Apply changes" else "Start program", fontWeight = FontWeight.Bold)
                }
            }
            TextButton(onClick = onBack, Modifier.fillMaxWidth()) {
                Text("Go back and edit", color = ProgramLavender)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private val ColorWhite = Color(0xFFF8F5FF)

@Composable
private fun Summary(label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF383245))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProgramCardGradient, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAA2BB))
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}
