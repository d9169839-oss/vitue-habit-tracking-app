package com.virtue.habittracker.presentation.home
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.virtue.habittracker.R
import com.virtue.habittracker.presentation.program.ProgramScreen
import com.virtue.habittracker.presentation.profile.ProfileScreen
import androidx.compose.ui.offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.model.HabitDayStatus
import com.virtue.habittracker.domain.model.summarizeHabitDay
import com.virtue.habittracker.presentation.auth.AuthViewModel
import com.virtue.habittracker.presentation.history.HistoryScreen
import com.virtue.habittracker.presentation.history.HistoryViewModel
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(onSignOut: () -> Unit, vm: HomeViewModel = hiltViewModel(), authViewModel: AuthViewModel = hiltViewModel(), historyViewModel: HistoryViewModel = hiltViewModel()) {
    val date by vm.selectedDate.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var showCalendar by remember { mutableStateOf(false) }
    // Keep the selected destination local to the shell; each destination has its own UI file.
    var selectedTab by remember { mutableStateOf(0) }
    // Calendar counts are derived from the selected date’s active habits and check-ins.
    val summary = summarizeHabitDay(date.toEpochDay(), habits)
    Column(Modifier.fillMaxSize()) {
        // Keep both tabs composed and collecting their state while hidden. This warms their
        // Room/Flow streams before a tap, so navigation switches visibility instead of creating
        // a screen and starting its data pipeline on the user's tap.
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .offset(x = if (selectedTab == 0) 0.dp else 10_000.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
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
                Text("${summary.completedCount} completed · ${summary.notCompletedCount} not completed · ${summary.unrecordedCount} unrecorded", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${summary.completionRatePercent}% completion rate", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(habits, key = { it.habit.id }) { entry -> HabitCard(entry, isFuture = date.isAfter(LocalDate.now()), canArchive = date == LocalDate.now(), onToggle = { vm.toggleCompletion(entry) }, onArchive = { vm.archiveHabit(entry) }, onClear = { vm.clearCompletion(entry) }) }
            }
        }
            }
            // Keep destinations composed so returning to a tab does not recreate its screen.
            HistoryScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = if (selectedTab == 1) 0.dp else 10_000.dp),
                vm = historyViewModel
            )
            ProgramScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = if (selectedTab == 2) 0.dp else 10_000.dp)
            )
            ProfileScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = if (selectedTab == 3) 0.dp else 10_000.dp),
                onSignOut = { authViewModel.signOut(); onSignOut() }
            )
        }
        NavigationBar {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(painterResource(R.drawable.ic_home), contentDescription = "Home") },
                label = { Text("Home") }
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(painterResource(R.drawable.ic_history), contentDescription = "History") },
                label = { Text("History") }
            )
            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(painterResource(R.drawable.ic_program), contentDescription = "Program") },
                label = { Text("Program") }
            )
            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(painterResource(R.drawable.ic_profile), contentDescription = "Profile") },
                label = { Text("Profile") }
            )
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
                    // Choosing a date is a request to review that date, so open the History tab.
                    selectedTab = 1
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
private fun HabitCard(entry: HabitDayEntry, isFuture: Boolean, canArchive: Boolean, onToggle: () -> Unit, onArchive: () -> Unit, onClear: () -> Unit) {
    val statusText = when (entry.status) {
        HabitDayStatus.UNRECORDED -> "Not recorded"
        HabitDayStatus.COMPLETED -> "Completed"
        HabitDayStatus.NOT_COMPLETED -> "Not completed"
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(entry.habit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (entry.habit.description.isNotBlank()) Text(entry.habit.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(statusText, color = if (entry.status == HabitDayStatus.COMPLETED) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.currentStreak > 0) Text("🔥 ${entry.currentStreak}-day streak", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
            Column {
                FilledTonalButton(onClick = onToggle, enabled = !isFuture) { Text(if (entry.status == HabitDayStatus.COMPLETED) "Undo" else "Done") }
                if (entry.status != HabitDayStatus.UNRECORDED) TextButton(onClick = onClear, enabled = !isFuture) { Text("Clear record") }
                TextButton(onClick = onArchive, enabled = canArchive) { Text("Archive") }
            }
        }
    }
}
