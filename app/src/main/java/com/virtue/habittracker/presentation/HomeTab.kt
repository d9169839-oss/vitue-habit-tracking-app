package com.virtue.habittracker.presentation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.R
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.model.HabitDayStatus
import com.virtue.habittracker.domain.model.summarizeHabitDay
import com.virtue.habittracker.presentation.auth.AuthViewModel
import com.virtue.habittracker.presentation.history.HistoryViewModel
import com.virtue.habittracker.presentation.home.HomeViewModel
import com.virtue.habittracker.presentation.programs.ProgramsTab
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


fun HomeTab(
    modifier: Modifier,
    visible: Boolean,
    vm: HomeViewModel,
    authViewModel: AuthViewModel,
    historyViewModel: HistoryViewModel,
    onOpenHistory: () -> Unit,
    onSignOut: () -> Unit
) {
    val date by vm.selectedDate.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var showCalendar by remember { mutableStateOf(false) }
    val summary = summarizeHabitDay(date.toEpochDay(), habits)
    val recordedCount = summary.completedCount + summary.notCompletedCount
    val recordedCompletionRate = if (recordedCount == 0) 0 else summary.completedCount * 100 / recordedCount

    Column(
        modifier.offset(x = if (visible) 0.dp else 10_000.dp).background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("VITUE", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Become who you choose.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { authViewModel.signOut(); onSignOut() }) { Text("Sign out") }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("YOUR DAILY PRACTICE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = vm::previousDay) { Text("← Previous") }
                    OutlinedButton(onClick = vm::nextDay, enabled = date.isBefore(LocalDate.now())) { Text("Next →") }
                    TextButton(onClick = { showCalendar = true }) { Text("Calendar") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusSummaryCard("Completed", summary.completedCount, Color(0xFF34D399), Modifier.weight(1f), "✓")
                    StatusSummaryCard("Not done", summary.notCompletedCount, Color(0xFFFB7185), Modifier.weight(1f), "×")
                    StatusSummaryCard("Unrecorded", summary.unrecordedCount, Color(0xFF9891A8), Modifier.weight(1f), "○")
                }
                Text("$recordedCompletionRate% of recorded habits completed", style = MaterialTheme.typography.labelMedium, color = Color(0xFFC4B5FD))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = recordedCompletionRate / 100f,
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = Color(0xFFA78BFA),
                    trackColor = Color(0xFF393047)
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Your habits", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(onClick = { title = ""; description = ""; formError = null; showAddDialog = true }, enabled = date == LocalDate.now()) { Text("+ Add habit") }
        }
        if (habits.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text(if (date.isAfter(LocalDate.now())) "This day is in the future." else "A fresh start.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(if (date.isAfter(LocalDate.now())) "Choose today or an earlier date to review your habits." else "Add a habit you want to practice consistently. You can record completions for each date.")
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                habits.forEach { entry ->
                    HabitCard(
                        entry = entry,
                        isFuture = date.isAfter(LocalDate.now()),
                        canArchive = date == LocalDate.now(),
                        onToggle = { vm.toggleCompletion(entry) },
                        onMarkNotCompleted = { vm.markNotCompleted(entry) },
                        onArchive = { vm.archiveHabit(entry) },
                        onClear = { vm.clearCompletion(entry) }
                    )
                }
            }
        }
    }

    if (showCalendar) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { showCalendar = false }, confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { millis ->
                    val selected = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    vm.selectDate(selected)
                    historyViewModel.selectDate(selected)
                    onOpenHistory()
                }
                showCalendar = false
            }) { Text("View date") }
        }, dismissButton = { TextButton(onClick = { showCalendar = false }) { Text("Cancel") } }) {
            DatePicker(state = pickerState)
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create a habit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it; formError = null }, label = { Text("Habit name") }, singleLine = true)
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Why it matters (optional)") })
                    formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = { Button(onClick = {
                val cleanTitle = title.trim()
                when {
                    cleanTitle.isBlank() -> formError = "Give your habit a name."
                    cleanTitle.length > 60 -> formError = "Habit names must be 60 characters or fewer."
                    description.length > 240 -> formError = "Descriptions must be 240 characters or fewer."
                    else -> { vm.addHabit(cleanTitle, description) { formError = it }; showAddDialog = false }
                }
            }) { Text("Create") } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun HabitCard(
    entry: HabitDayEntry,
    isFuture: Boolean,
    canArchive: Boolean,
    onToggle: () -> Unit,
    onMarkNotCompleted: () -> Unit,
    onArchive: () -> Unit,
    onClear: () -> Unit
) {
    val statusText = when (entry.status) {
        HabitDayStatus.UNRECORDED -> "Not recorded"
        HabitDayStatus.COMPLETED -> "Completed"
        HabitDayStatus.NOT_COMPLETED -> "Not completed"
    }
    val statusColor = when (entry.status) {
        HabitDayStatus.COMPLETED -> Color(0xFF34D399)
        HabitDayStatus.NOT_COMPLETED -> Color(0xFFFB7185)
        HabitDayStatus.UNRECORDED -> Color(0xFF9891A8)
    }
    val statusSymbol = when (entry.status) {
        HabitDayStatus.COMPLETED -> "✓"
        HabitDayStatus.NOT_COMPLETED -> "×"
        HabitDayStatus.UNRECORDED -> "○"
    }
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF211B30)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF393047))
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.background(Color(0xFF302643), RoundedCornerShape(14.dp)).padding(13.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    when {
                        entry.habit.title.contains("read", true) -> "Aa"
                        entry.habit.title.contains("workout", true) || entry.habit.title.contains("exercise", true) -> "↗"
                        entry.habit.title.contains("water", true) -> "◒"
                        entry.habit.title.contains("meditat", true) -> "✦"
                        else -> "◆"
                    },
                    color = Color(0xFFC4B5FD),
                    fontWeight = FontWeight.Bold
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(entry.habit.title, style = MaterialTheme.typography.titleMedium, color = Color(0xFFF8F5FF), fontWeight = FontWeight.SemiBold)
                if (entry.habit.description.isNotBlank()) {
                    Text(entry.habit.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAA2BB))
                }
                Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.14f)) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(statusSymbol, color = statusColor, fontWeight = FontWeight.Bold)
                        Text(statusText, color = statusColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (entry.currentStreak > 0) {
                    Text("${entry.currentStreak}-day streak", color = Color(0xFFC4B5FD), style = MaterialTheme.typography.labelSmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onToggle,
                        enabled = !isFuture,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (entry.status == HabitDayStatus.COMPLETED) "Undo" else "Complete") }
                    OutlinedButton(
                        onClick = onMarkNotCompleted,
                        enabled = !isFuture && entry.status != HabitDayStatus.NOT_COMPLETED,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Not done") }
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    if (entry.status != HabitDayStatus.UNRECORDED) {
                        TextButton(onClick = onClear, enabled = !isFuture) { Text("Clear record") }
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onArchive, enabled = canArchive) { Text("Archive") }
                }
            }
        }
    }
}
