package com.virtue.habittracker.presentation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
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
import androidx.compose.ui.offset
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
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private enum class AppTab { HOME, HISTORY, PROGRAM, PROFILE }
private enum class HistoryFilter { ALL, ACTIVE, INACTIVE }

/**
 * The app shell and all four bottom-tab screens intentionally live in this file.
 * ViewModels and domain/data code remain separate to preserve MVVM and Clean Architecture.
 */
@Composable
fun App(
    onSignOut: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    val isOnline = rememberInternetConnectivity()
    var showOnlineBanner by remember { mutableStateOf(false) }
    var hasBeenOffline by remember { mutableStateOf(!isOnline) }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            hasBeenOffline = true
            showOnlineBanner = false
        } else if (hasBeenOffline) {
            showOnlineBanner = true
            delay(2_500)
            showOnlineBanner = false
            hasBeenOffline = false
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Offline state remains visible until connectivity returns.
        if (!isOnline) {
            ConnectivityBanner(
                text = "Device offline",
                background = Color(0xFFC62828)
            )
        } else if (showOnlineBanner) {
            ConnectivityBanner(
                text = "Internet available",
                background = Color(0xFF2E7D32)
            )
        }

        // Keep destinations composed to reduce tab-switch startup work and preserve UI state.
        Box(Modifier.weight(1f).fillMaxWidth()) {
            HomeTab(
                modifier = Modifier.fillMaxSize().alpha(if (selectedTab == AppTab.HOME) 1f else 0f),
                visible = selectedTab == AppTab.HOME,
                vm = homeViewModel,
                authViewModel = authViewModel,
                historyViewModel = historyViewModel,
                onOpenHistory = { selectedTab = AppTab.HISTORY },
                onSignOut = onSignOut
            )
            HistoryTab(
                modifier = Modifier.fillMaxSize().alpha(if (selectedTab == AppTab.HISTORY) 1f else 0f),
                visible = selectedTab == AppTab.HISTORY,
                vm = historyViewModel
            )
            ProgramTab(
                modifier = Modifier.fillMaxSize().alpha(if (selectedTab == AppTab.PROGRAM) 1f else 0f),
                visible = selectedTab == AppTab.PROGRAM
            )
            ProfileTab(
                modifier = Modifier.fillMaxSize().alpha(if (selectedTab == AppTab.PROFILE) 1f else 0f),
                visible = selectedTab == AppTab.PROFILE,
                onSignOut = { authViewModel.signOut(); onSignOut() }
            )
        }

        NavigationBar {
            NavigationBarItem(selected = selectedTab == AppTab.HOME, onClick = { selectedTab = AppTab.HOME },
                icon = { androidx.compose.material3.Icon(painterResource(R.drawable.ic_home), contentDescription = "Home") },
                label = { Text("Home") })
            NavigationBarItem(selected = selectedTab == AppTab.HISTORY, onClick = { selectedTab = AppTab.HISTORY },
                icon = { androidx.compose.material3.Icon(painterResource(R.drawable.ic_history), contentDescription = "History") },
                label = { Text("History") })
            NavigationBarItem(selected = selectedTab == AppTab.PROGRAM, onClick = { selectedTab = AppTab.PROGRAM },
                icon = { androidx.compose.material3.Icon(painterResource(R.drawable.ic_program), contentDescription = "Program") },
                label = { Text("Program") })
            NavigationBarItem(selected = selectedTab == AppTab.PROFILE, onClick = { selectedTab = AppTab.PROFILE },
                icon = { androidx.compose.material3.Icon(painterResource(R.drawable.ic_profile), contentDescription = "Profile") },
                label = { Text("Profile") })
        }
    }
}

@Composable
private fun ConnectivityBanner(text: String, background: Color) {
    Surface(color = background, contentColor = Color.White) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Uses Android's validated internet capability, not merely a connected Wi-Fi/mobile network. */
@Composable
private fun rememberInternetConnectivity(): Boolean {
    val context = LocalContext.current
    var online by remember { mutableStateOf(hasValidatedInternet(context)) }

    DisposableEffect(context) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                online = hasValidatedInternet(context)
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                online = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }

            override fun onLost(network: Network) {
                online = hasValidatedInternet(context)
            }
        }
        try {
            manager.registerDefaultNetworkCallback(callback)
        } catch (_: RuntimeException) {
            // Keep the initial connectivity result if Android cannot register the callback.
        }
        onDispose {
            try { manager.unregisterNetworkCallback(callback) } catch (_: RuntimeException) { }
        }
    }
    return online
}

private fun hasValidatedInternet(context: Context): Boolean {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

@Composable
private fun HomeTab(
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

    Column(
        modifier.offset(x = if (visible) 0.dp else 10_000.dp).background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp, vertical = 24.dp),
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
                items(habits, key = { it.habit.id }) { entry ->
                    HabitCard(entry, date.isAfter(LocalDate.now()), date == LocalDate.now(),
                        onToggle = { vm.toggleCompletion(entry) }, onArchive = { vm.archiveHabit(entry) },
                        onClear = { vm.clearCompletion(entry) })
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

@Composable
private fun HistoryTab(modifier: Modifier, visible: Boolean, vm: HistoryViewModel) {
    val date by vm.selectedDate.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    var showCalendar by remember { mutableStateOf(false) }
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

    Column(modifier.offset(x = if (visible) 0.dp else 10_000.dp).background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp, vertical = 20.dp),
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
            SummaryTile("Completed", completed.toString(), Modifier.weight(1f))
            SummaryTile("Not done", notDone.toString(), Modifier.weight(1f))
            SummaryTile("Unrecorded", unrecorded.toString(), Modifier.weight(1f))
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
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visibleEntries, key = { it.habit.id }) { entry -> HistoryHabitCard(entry) }
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
private fun SummaryTile(label: String, count: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(count, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
        HabitDayStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        HabitDayStatus.NOT_COMPLETED -> MaterialTheme.colorScheme.error
        HabitDayStatus.UNRECORDED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(Modifier.fillMaxWidth().alpha(if (active) 1f else 0.48f), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(entry.habit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (entry.habit.description.isNotBlank()) Text(entry.habit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (active) "Active on this date" else "Inactive on this date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.currentStreak > 0) Text("${entry.currentStreak}-day streak at this date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.14f)) {
                Text(label, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = color)
            }
        }
    }
}

@Composable
private fun ProgramTab(modifier: Modifier, visible: Boolean) {
    Column(modifier.offset(x = if (visible) 0.dp else 10_000.dp).background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("YOUR ROUTINE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Programs", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Turn small daily actions into a plan you can follow.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your programs start here", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Organized habit plans will appear here. You can keep tracking individual habits from Home while the program experience is being built.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfileTab(modifier: Modifier, visible: Boolean, onSignOut: () -> Unit) {
    Column(modifier.offset(x = if (visible) 0.dp else 10_000.dp).background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("ACCOUNT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Your account and app preferences.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Keep showing up for yourself.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Profile details and preferences can be added here.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
    }
}
