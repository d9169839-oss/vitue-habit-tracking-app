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
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


private enum class HistoryFilter { ALL, ACTIVE, INACTIVE }

@Composable
fun HistoryTab(modifier: Modifier, visible: Boolean, vm: HistoryViewModel) {
    val date by vm.selectedDate.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val weeklyProgress by vm.weeklyProgress.collectAsStateWithLifecycle()
    val isWeeklyProgressLoading by vm.isWeeklyProgressLoading.collectAsStateWithLifecycle()
    val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    val active = entries.filter { it.isActiveOnDate }
    val completed = active.count { it.status == HabitDayStatus.COMPLETED }
    val notDone = active.count { it.status == HabitDayStatus.NOT_COMPLETED }
    val unrecorded = active.count { it.status == HabitDayStatus.UNRECORDED }
    val inactiveCount = entries.count { !it.isActiveOnDate }
    val rate = if (active.isEmpty()) 0 else (completed * 100f / active.size).toInt()
    val visibleEntries = when (filter) {
        HistoryFilter.ALL -> entries
        HistoryFilter.ACTIVE -> entries.filter { it.isActiveOnDate }
        HistoryFilter.INACTIVE -> entries.filter { !it.isActiveOnDate }
    }

    Column(modifier.offset(x = if (visible) 0.dp else 10_000.dp).background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("History", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Look back at the person you were becoming, one day at a time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("WEEKLY HISTORY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(
                    "${weekStart.format(DateTimeFormatter.ofPattern("d MMM"))} – ${weekEnd.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Browse one complete week at a time. Daily habit records remain separate from program progress.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = vm::previousWeek, modifier = Modifier.weight(1f)) { Text("← Previous week") }
                    OutlinedButton(onClick = vm::goToCurrentWeek, modifier = Modifier.weight(1f)) { Text("This week") }
                    OutlinedButton(onClick = vm::nextWeek, enabled = vm.canMoveToNextWeek(), modifier = Modifier.weight(1f)) { Text("Next →") }
                }
                Text("Daily snapshot: ${date.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy"))}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
        WeeklyHabitHistoryChart(
            points = weeklyProgress,
            isLoading = isWeeklyProgressLoading
        )
        if (isLoading) {
            SectionLoading(message = "Loading your history…")
        } else {
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
    }

}

@Composable
private fun WeeklyHabitHistoryChart(
    points: List<com.virtue.habittracker.presentation.history.DailyProgressPoint>,
    isLoading: Boolean
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Weekly consistency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Each day is shown independently. Days without recorded completions stay at 0%.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            if (isLoading) {
                SectionLoading(message = "Loading this week…")
            } else if (points.isEmpty()) {
                Text("No weekly records yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(
                    Modifier.fillMaxWidth().height(142.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    points.forEach { point ->
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                "${point.completionRatePercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(5.dp))
                            Box(
                                Modifier.fillMaxWidth(0.68f)
                                    .height((point.completionRatePercent.coerceIn(0, 100) * 0.78f + 3f).dp)
                                    .background(
                                        if (point.completionRatePercent == 0) MaterialTheme.colorScheme.surfaceVariant
                                        else MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                    )
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(point.date.format(DateTimeFormatter.ofPattern("EE")), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
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

