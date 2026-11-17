package com.virtue.habittracker.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.model.HabitDayStatus
import com.virtue.habittracker.domain.model.summarizeHabitDay
import com.virtue.habittracker.presentation.auth.AuthViewModel
import com.virtue.habittracker.presentation.history.HistoryViewModel
import com.virtue.habittracker.presentation.home.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
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
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.selectDate(LocalDate.now())
    }

    val summary = summarizeHabitDay(date.toEpochDay(), habits)
    val totalHabits = habits.size
    val progressPercentage = if (totalHabits == 0) 0 else (summary.completedCount * 100) / totalHabits
    val progressFloat = if (totalHabits == 0) 0f else summary.completedCount.toFloat() / totalHabits.toFloat()
    val remainingHabits = totalHabits - summary.completedCount

    Column(
        modifier = modifier
            .offset(x = if (visible) 0.dp else 10_000.dp)
            .background(Color(0xFF12111A))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header Row: App Name & Sign out
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Today is yours.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                // Current Date badge display (non-clickable calendar picker)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF1E1C2A)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🗓",
                            fontSize = 14.sp
                        )
                        Text(
                            text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
            TextButton(onClick = { authViewModel.signOut(); onSignOut() }) {
                Text("Sign out", color = Color(0xFF9891A8), fontWeight = FontWeight.SemiBold)
            }
        }

        // Daily Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF393344),
                                Color(0xFF302A3A),
                                Color(0xFF24202D)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(900f, 900f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top line inside card: Title + Active Routine badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR DAILY PROGRESS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8862FC),
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF8862FC)
                    ) {
                        Text(
                            text = "Active Routine",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC4B5FD),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                // Middle section: Circular Progress Indicator & Completion Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Circular Gauge
                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { progressFloat },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF00DC82),
                            trackColor = Color(0xFF14131C),
                            strokeWidth = 8.dp
                        )
                        Text(
                            text = "$progressPercentage%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Progress Text
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${summary.completedCount} of $totalHabits completed",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = when {
                                totalHabits == 0 -> "Add habits to begin tracking."
                                remainingHabits > 0 -> "$remainingHabits more habit${if (remainingHabits > 1) "s" else ""} to complete your day."
                                else -> "All habits completed for today!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9E8AA7),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Horizontal Linear Progress Bar (Green for completed, Black for uncompleted)
                LinearProgressIndicator(
                    progress = { progressFloat },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Color(0xFF00DC82),
                    trackColor = Color(0xFF14131C)
                )
            }
        }

        // Small Status Summary Cards Row: Completed, Missed, Unrecorded
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            StatusSummaryCard(
                label = "Completed",
                count = summary.completedCount,
                accent = Color(0xFF00DC82),
                modifier = Modifier.weight(1f),
                symbol = "✓"
            )

            StatusSummaryCard(
                label = "Missed",
                count = summary.notCompletedCount,
                accent = Color(0xFFFF5252),
                modifier = Modifier.weight(1f),
                symbol = "✕"
            )

            StatusSummaryCard(
                label = "Unrecorded",
                count = summary.unrecordedCount,
                accent = Color(0xFF8F9BB3),
                modifier = Modifier.weight(1f),
                symbol = "⏸"
            )
        }
        // Habits List Header & Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Your habits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF262338)
                ) {
                    Text(
                        text = "$totalHabits habits",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9E8AA7),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Button(
                onClick = { title = ""; description = ""; formError = null; showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC4B5FD),
                    contentColor = Color(0xFF171020)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Add habit", fontWeight = FontWeight.Bold)
            }
        }

        // Habits List or Empty State
        if (isLoading) {
            SectionLoading(message = "Loading this day's habits…")
        } else if (habits.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = "A fresh start.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Add a habit you want to practice consistently.",
                        color = Color(0xFF9E8AA7)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                habits.forEach { entry ->
                    HabitCard(
                        entry = entry,
                        isFuture = false,
                        canArchive = true,
                        onToggle = { vm.toggleCompletion(entry) },
                        onMarkNotCompleted = { vm.markNotCompleted(entry) },
                        onArchive = { vm.archiveHabit(entry) },
                        onClear = { vm.clearCompletion(entry) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create a habit", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; formError = null },
                        label = { Text("Habit name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Why it matters (optional)") }
                    )
                    formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanTitle = title.trim()
                        when {
                            cleanTitle.isBlank() -> formError = "Give your habit a name."
                            cleanTitle.length > 60 -> formError = "Habit names must be 60 characters or fewer."
                            description.length > 240 -> formError = "Descriptions must be 240 characters or fewer."
                            else -> {
                                vm.addHabit(cleanTitle, description) { formError = it }
                                showAddDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00DC82), contentColor = Color(0xFF0B140F))
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
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
    var expanded by remember { mutableStateOf(false) }

    val (icon, iconColor) = when {
        entry.habit.title.contains("workout", true) || entry.habit.title.contains("exercise", true) || entry.habit.title.contains("gym", true) ->
            Icons.Filled.FitnessCenter to Color(0xFFC4B5FD)
        entry.habit.title.contains("read", true) || entry.habit.title.contains("book", true) || entry.habit.title.contains("study", true) ->
            Icons.AutoMirrored.Filled.MenuBook to Color(0xFF9E86E7)
        entry.habit.title.contains("water", true) || entry.habit.title.contains("drink", true) || entry.habit.title.contains("hydrat", true) ->
            Icons.Filled.WaterDrop to Color(0xFF80D8FF)
        entry.habit.title.contains("meditat", true) || entry.habit.title.contains("mind", true) || entry.habit.title.contains("yoga", true) ->
            Icons.Filled.SelfImprovement to Color(0xFF00DC82)
        entry.habit.title.contains("code", true) || entry.habit.title.contains("kotlin", true) || entry.habit.title.contains("dev", true) || entry.habit.title.contains("program", true) ->
            Icons.Filled.Code to Color(0xFFC4B5FD)
        entry.habit.title.contains("walk", true) || entry.habit.title.contains("run", true) || entry.habit.title.contains("step", true) ->
            Icons.AutoMirrored.Filled.DirectionsRun to Color(0xFF34D399)
        else ->
            Icons.Filled.Star to Color(0xFFC4B5FD)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF2E2C3D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular icon container
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF262438), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Middle section: Title + streak badge and subtitle
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = entry.habit.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (entry.currentStreak > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = Color(0xFFC4B5FD),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${entry.currentStreak}",
                                    color = Color(0xFFC4B5FD),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "0d streak",
                                color = Color(0xFF78738F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = if (entry.habit.description.isNotBlank()) entry.habit.description else "Daily practice",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8AA7),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right action pill: Mark complete / Completed / Missed
                when (entry.status) {
                    HabitDayStatus.COMPLETED -> {
                        Surface(
                            onClick = { expanded = !expanded },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0D3325),
                            border = BorderStroke(1.dp, Color(0xFF145C3E))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00DC82),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Completed",
                                    color = Color(0xFF00DC82),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    HabitDayStatus.NOT_COMPLETED -> {
                        Surface(
                            onClick = { expanded = !expanded },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF381418),
                            border = BorderStroke(1.dp, Color(0xFF6B1E26))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Cancel,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Missed",
                                    color = Color(0xFFFF5252),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    HabitDayStatus.UNRECORDED -> {
                        Surface(
                            onClick = { if (!isFuture) onToggle() },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E1C2A),
                            border = BorderStroke(1.dp, Color(0xFF3A364E))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Circle,
                                    contentDescription = null,
                                    tint = Color(0xFF8E8AA7),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Mark complete",
                                    color = Color(0xFFE2E0EC),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Dropdown on card tap: allows undoing or resetting
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(
                        color = Color(0xFF2E2C3D),
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (entry.status == HabitDayStatus.COMPLETED) {
                            Button(
                                onClick = {
                                    onClear()
                                    expanded = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2A273C),
                                    contentColor = Color(0xFFFF758F)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Undo completion", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else if (entry.status == HabitDayStatus.NOT_COMPLETED) {
                            Button(
                                onClick = {
                                    onClear()
                                    expanded = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2A273C),
                                    contentColor = Color(0xFFC4B5FD)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Reset status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    onMarkNotCompleted()
                                    expanded = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2A273C),
                                    contentColor = Color(0xFFFF5252)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Mark as missed", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (entry.status != HabitDayStatus.UNRECORDED) {
                                TextButton(
                                    onClick = {
                                        onClear()
                                        expanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Clear", color = Color(0xFF9E8AA7), fontSize = 12.sp)
                                }
                            }
                            if (canArchive) {
                                TextButton(
                                    onClick = {
                                        onArchive()
                                        expanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Archive", color = Color(0xFF9E8AA7), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
