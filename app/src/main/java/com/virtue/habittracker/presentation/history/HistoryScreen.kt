package com.virtue.habittracker.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.model.HabitDayStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private enum class HistoryFilter { ALL, ACTIVE, INACTIVE }

/** History is a read-only snapshot for the selected date. */
@Composable
fun HistoryScreen(modifier: Modifier = Modifier, vm: HistoryViewModel = hiltViewModel()) {
    val date by vm.selectedDate.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    var showCalendar by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }

    val activeEntries = entries.filter { it.isActiveOnDate }
    val completedCount = activeEntries.count { it.status == HabitDayStatus.COMPLETED }
    val notCompletedCount = activeEntries.count { it.status == HabitDayStatus.NOT_COMPLETED }
    val unrecordedCount = activeEntries.count { it.status == HabitDayStatus.UNRECORDED }
    val inactiveCount = entries.count { !it.isActiveOnDate }
    val completionRate = if (activeEntries.isEmpty()) 0
        else (completedCount * 100f / activeEntries.size).toInt()
    val visibleEntries = when (filter) {
        HistoryFilter.ALL -> entries
        HistoryFilter.ACTIVE -> entries.filter { it.isActiveOnDate }
        HistoryFilter.INACTIVE -> entries.filter { !it.isActiveOnDate }
    }

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("YOUR JOURNEY", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Habit history", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            Text("Look back at the person you were becoming, one day at a time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Card(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("DAY SNAPSHOT", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy")),
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Habits created by this date · status recorded on this day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = vm::previousDay, modifier = Modifier.weight(1f)) {
                        Text("← Previous")
                    }
                    OutlinedButton(onClick = { showCalendar = true }, modifier = Modifier.weight(1f)) {
                        Text("Choose date")
                    }
                    OutlinedButton(onClick = vm::nextDay, enabled = date.isBefore(LocalDate.now()),
                        modifier = Modifier.weight(1f)) {
                        Text("Next →")
                    }
                }
                if (date != LocalDate.now()) TextButton(onClick = vm::goToToday) { Text("Back to today") }
            }
        }

        // Completion rate counts only habits active on the selected date.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryTile("Completed", completedCount.toString(), Modifier.weight(1f))
            SummaryTile("Not done", notCompletedCount.toString(), Modifier.weight(1f))
            SummaryTile("Unrecorded", unrecordedCount.toString(), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("${completionRate}% completion", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("${activeEntries.size} active · ${inactiveCount} inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${entries.size} total habits", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter == HistoryFilter.ALL, onClick = { filter = HistoryFilter.ALL }, label = { Text("All") })
            FilterChip(selected = filter == HistoryFilter.ACTIVE, onClick = { filter = HistoryFilter.ACTIVE }, label = { Text("Active") })
            FilterChip(selected = filter == HistoryFilter.INACTIVE, onClick = { filter = HistoryFilter.INACTIVE }, label = { Text("Inactive") })
        }

        if (visibleEntries.isEmpty()) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (entries.isEmpty()) "No history yet" else "Nothing in this filter",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (entries.isEmpty()) "Habits created on or before this date will appear here."
                        else "Try another filter to see more of your habit journey.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visibleEntries, key = { it.habit.id }) { entry -> HistoryHabitCard(entry) }
            }
        }
    }

    if (showCalendar) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        vm.selectDate(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showCalendar = false
                }) { Text("View history") }
            },
            dismissButton = { TextButton(onClick = { showCalendar = false }) { Text("Cancel") } }
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun SummaryTile(label: String, count: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(count, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HistoryHabitCard(entry: HabitDayEntry) {
    val isActive = entry.isActiveOnDate
    val statusLabel = when (entry.status) {
        HabitDayStatus.COMPLETED -> "Completed"
        HabitDayStatus.NOT_COMPLETED -> "Not completed"
        HabitDayStatus.UNRECORDED -> "Not recorded"
    }
    val statusColor = when (entry.status) {
        HabitDayStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        HabitDayStatus.NOT_COMPLETED -> MaterialTheme.colorScheme.error
        HabitDayStatus.UNRECORDED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (isActive) 1f else 0.48f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(entry.habit.title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                if (entry.habit.description.isNotBlank()) {
                    Text(entry.habit.description, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(if (isActive) "Active on this date" else "Inactive on this date",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.currentStreak > 0) {
                    Text("${entry.currentStreak}-day streak at this date",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(1.dp))
            Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.14f)) {
                Text(statusLabel, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium, color = statusColor)
            }
        }
    }
}
