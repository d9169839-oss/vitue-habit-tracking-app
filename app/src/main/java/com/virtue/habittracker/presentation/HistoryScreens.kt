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


private enum class HistoryFilter { ALL, ACTIVE, INACTIVE }

fun HistoryTab(modifier: Modifier, visible: Boolean, vm: HistoryViewModel) {
    val date by vm.selectedDate.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    var showCalendar by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    val active = entries.filter { it.isActiveOnDate }
    val completed = active.count { it.status == HabitDayStatus.COMPLETED }
    val notDone = active.count { it.status == HabitDayStatus.NOT_COMPLETED }
    val unrecorded = active.count { it.status == HabitDayStatus.UNRECORDED }
    val inactiveCount = entries.count { !it.isActiveOnDate }
    val recorded = completed + notDone
    val rate = if (recorded == 0) 0 else (completed * 100f / recorded).toInt()
    val visibleEntries = when (filter) {
        HistoryFilter.ALL -> entries
        HistoryFilter.ACTIVE -> entries.filter { it.isActiveOnDate }
        HistoryFilter.INACTIVE -> entries.filter { !it.isActiveOnDate }
    }

    Column(modifier.offset(x = if (visible) 0.dp else 10_000.dp).background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("YOUR JOURNEY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Habit history", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Look back at the person you were becoming, one day at a time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("DAY SNAPSHOT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy")), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Habits created by this date · status recorded on this day", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = vm::previousDay, modifier = Modifier.weight(1f)) { Text("← Previous") }
                    OutlinedButton(onClick = { showCalendar = true }, modifier = Modifier.weight(1f)) { Text("Choose date") }
                    OutlinedButton(onClick = vm::nextDay, enabled = date.isBefore(LocalDate.now()), modifier = Modifier.weight(1f)) { Text("Next →") }
                }
                if (date != LocalDate.now()) TextButton(onClick = vm::goToToday) { Text("Back to today") }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryTile("Completed", completed.toString(), Color(0xFF34D399), Modifier.weight(1f), "✓")
            SummaryTile("Not done", notDone.toString(), Color(0xFFFB7185), Modifier.weight(1f), "×")
            SummaryTile("Unrecorded", unrecorded.toString(), Color(0xFF9891A8), Modifier.weight(1f), "○")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("$rate% completion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("${active.size} active · ${inactiveCount} inactive", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${entries.size} total habits", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter == HistoryFilter.ALL, onClick = { filter = HistoryFilter.ALL }, label = { Text("All") })
            FilterChip(selected = filter == HistoryFilter.ACTIVE, onClick = { filter = HistoryFilter.ACTIVE }, label = { Text("Active") })
            FilterChip(selected = filter == HistoryFilter.INACTIVE, onClick = { filter = HistoryFilter.INACTIVE }, label = { Text("Inactive") })
        }
        if (visibleEntries.isEmpty()) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (entries.isEmpty()) "No history yet" else "Nothing in this filter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(if (entries.isEmpty()) "Habits created on or before this date will appear here." else "Try another filter to see more of your habit journey.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                visibleEntries.forEach { entry -> HistoryHabitCard(entry) }
            }
        }
    }
    if (showCalendar) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { showCalendar = false }, confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { vm.selectDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                showCalendar = false
            }) { Text("View history") }
        }, dismissButton = { TextButton(onClick = { showCalendar = false }) { Text("Cancel") } }) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun SummaryTile(label: String, count: String, accent: Color, modifier: Modifier = Modifier, symbol: String) {
    Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(symbol, color = accent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(count, color = accent, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HistoryHabitCard(entry: HabitDayEntry) {
    val active = entry.isActiveOnDate
    val label = when (entry.status) {
        HabitDayStatus.COMPLETED -> "Completed"
        HabitDayStatus.NOT_COMPLETED -> "Not completed"
        HabitDayStatus.UNRECORDED -> "Not recorded"
    }
    val color = when (entry.status) {
        HabitDayStatus.COMPLETED -> Color(0xFF34D399)
        HabitDayStatus.NOT_COMPLETED -> Color(0xFFFB7185)
        HabitDayStatus.UNRECORDED -> Color(0xFF9891A8)
    }
    val symbol = when (entry.status) {
        HabitDayStatus.COMPLETED -> "✓"
        HabitDayStatus.NOT_COMPLETED -> "×"
        HabitDayStatus.UNRECORDED -> "○"
    }
    Card(Modifier.fillMaxWidth().alpha(if (active) 1f else 0.62f), shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(entry.habit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (entry.habit.description.isNotBlank()) Text(entry.habit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (active) "Active on this date" else "Inactive on this date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.currentStreak > 0) Text("${entry.currentStreak}-day streak at this date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.14f)) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(symbol, color = color, fontWeight = FontWeight.Bold)
                    Text(label, style = MaterialTheme.typography.labelMedium, color = color)
                }
            }
        }
    }
}

/**
 * Progress uses persisted records for the selected day. Long-range trend charts should only be
 * added once the repository exposes a complete historical aggregation; this screen never fakes data.
 */
@Composable
fun ProgressTab(modifier: Modifier, visible: Boolean, vm: HistoryViewModel, onOpenHistory: () -> Unit) {
    val date by vm.selectedDate.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val weeklyProgress by vm.weeklyProgress.collectAsStateWithLifecycle()
    val activeEntries = entries.filter { it.isActiveOnDate }
    val completed = activeEntries.count { it.status == HabitDayStatus.COMPLETED }
    val notCompleted = activeEntries.count { it.status == HabitDayStatus.NOT_COMPLETED }
    val unrecorded = activeEntries.count { it.status == HabitDayStatus.UNRECORDED }
    val recorded = completed + notCompleted
    val rate = if (recorded == 0) 0 else completed * 100 / recorded

    Column(
        modifier.offset(x = if (visible) 0.dp else 10_000.dp)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("YOUR JOURNEY", style = MaterialTheme.typography.labelMedium, color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold)
        Text("Your progress", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Small steps. Lasting change.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF211B30))) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("DAILY OVERVIEW", color = Color(0xFFC4B5FD), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("$rate%", style = MaterialTheme.typography.displaySmall, color = Color(0xFFF8F5FF), fontWeight = FontWeight.Bold)
                        Text("of recorded habits completed", color = Color(0xFFAAA2BB), style = MaterialTheme.typography.bodySmall)
                    }
                    Box(Modifier.background(Color(0xFF302643), RoundedCornerShape(18.dp)).padding(16.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("$completed/$recorded", color = Color(0xFFC4B5FD), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { rate / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color(0xFFA78BFA),
                    trackColor = Color(0xFF393047)
                )
                Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")), color = Color(0xFFAAA2BB), style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = vm::previousDay, modifier = Modifier.weight(1f)) { Text("Previous") }
                    OutlinedButton(onClick = vm::goToToday, modifier = Modifier.weight(1f)) { Text("Today") }
                    OutlinedButton(onClick = vm::nextDay, enabled = date.isBefore(LocalDate.now()), modifier = Modifier.weight(1f)) { Text("Next") }
                }
            }
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF211B30))) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Consistency · 7 days", color = Color(0xFFF8F5FF), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Tap a day to inspect its saved history.", color = Color(0xFFAAA2BB), style = MaterialTheme.typography.bodySmall)
                if (weeklyProgress.isEmpty()) {
                    Text("Your recent progress will appear here as records are saved.", color = Color(0xFFAAA2BB), style = MaterialTheme.typography.bodySmall)
                } else {
                    Row(Modifier.fillMaxWidth().height(132.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
                        weeklyProgress.forEach { point ->
                            Column(
                                Modifier.weight(1f).fillMaxWidth().clickable {
                                    vm.selectDate(point.date)
                                    onOpenHistory()
                                },
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text("${point.completionRatePercent}%", color = Color(0xFFC4B5FD), style = MaterialTheme.typography.labelSmall)
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    Modifier.fillMaxWidth(0.72f)
                                        .height((point.completionRatePercent.coerceIn(0, 100) * 0.82f + 5f).dp)
                                        .background(
                                            if (point.date == date) Color(0xFFC4B5FD) else Color(0xFFA78BFA),
                                            RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp)
                                        )
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(point.date.format(DateTimeFormatter.ofPattern("EE")), color = Color(0xFFAAA2BB), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusSummaryCard("Completed", completed, Color(0xFF34D399), Modifier.weight(1f), "✓")
            StatusSummaryCard("Not done", notCompleted, Color(0xFFFB7185), Modifier.weight(1f), "×")
            StatusSummaryCard("Unrecorded", unrecorded, Color(0xFF9891A8), Modifier.weight(1f), "○")
        }
        Text("Habit performance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (activeEntries.isEmpty()) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("No habits to summarize", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Create habits on Home and their recorded progress will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else activeEntries.forEach { entry ->
            val accent = when (entry.status) {
                HabitDayStatus.COMPLETED -> Color(0xFF34D399)
                HabitDayStatus.NOT_COMPLETED -> Color(0xFFFB7185)
                HabitDayStatus.UNRECORDED -> Color(0xFF9891A8)
            }
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.background(Color(0xFF302643), RoundedCornerShape(12.dp)).padding(12.dp)) {
                            Text(if (entry.habit.title.contains("read", true)) "Aa" else if (entry.habit.title.contains("workout", true)) "↗" else "✦", color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(entry.habit.title, color = Color(0xFFF8F5FF), fontWeight = FontWeight.SemiBold)
                            Text(when (entry.status) {
                                HabitDayStatus.COMPLETED -> "Completed"
                                HabitDayStatus.NOT_COMPLETED -> "Not completed"
                                HabitDayStatus.UNRECORDED -> "Not recorded"
                            }, color = accent, style = MaterialTheme.typography.bodySmall)
                        }
                        if (entry.currentStreak > 0) Text("${entry.currentStreak}d streak", color = Color(0xFFC4B5FD), style = MaterialTheme.typography.labelSmall)
                    }
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { if (entry.status == HabitDayStatus.COMPLETED) 1f else 0f },
                        modifier = Modifier.fillMaxWidth().height(5.dp),
                        color = accent,
                        trackColor = Color(0xFF393047)
                    )
                }
            }
        }
        Button(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Text("Open daily history for this date")
        }
        Text("Unrecorded habits are not counted as failures. This view summarizes saved records for the selected day.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}
