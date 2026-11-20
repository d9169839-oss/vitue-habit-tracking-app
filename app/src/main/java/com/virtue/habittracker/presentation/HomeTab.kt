package com.virtue.habittracker.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.model.HabitDayStatus
import com.virtue.habittracker.domain.model.program.ProgramActivityStatus
import com.virtue.habittracker.domain.model.program.ProgramStatus
import com.virtue.habittracker.domain.model.summarizeHabitDay
import com.virtue.habittracker.presentation.history.HistoryViewModel
import com.virtue.habittracker.presentation.home.HomeViewModel
import com.virtue.habittracker.presentation.programs.ProgramsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private data class StarterHabitPreset(
    val title: String,
    val description: String
)

private val StarterHabitPresets = listOf(
    StarterHabitPreset("Morning Hydration (500ml)", "Drink a full glass of water within 15 minutes of waking"),
    StarterHabitPreset("10-Min Morning Sunlight", "Step outside for natural daylight to anchor circadian rhythm"),
    StarterHabitPreset("20-Min Deep Reading", "Read 10+ pages of non-fiction or skill-building material"),
    StarterHabitPreset("8,000 Daily Steps Walk", "Complete a brisk outdoor walk for metabolic and mental clarity"),
    StarterHabitPreset("Digital Sunset (No Screens)", "Put phone away 30 minutes before sleep window"),
    StarterHabitPreset("5-Min Evening Reflection", "Write down 3 wins and tomorrow's #1 priority")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeTab(
    modifier: Modifier,
    visible: Boolean,
    vm: HomeViewModel,
    historyViewModel: HistoryViewModel,
    onOpenHistory: () -> Unit,
    onOpenPrograms: () -> Unit = {},
    onOpenActiveProgram: (enrollmentId: String) -> Unit = { onOpenPrograms() },
    programsViewModel: ProgramsViewModel = hiltViewModel()
) {
    val date by vm.selectedDate.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val programsState by programsViewModel.uiState.collectAsStateWithLifecycle()
    val activePrograms = remember(programsState.programs) {
        programsState.programs.filter { it.enrollment.status == ProgramStatus.ACTIVE }
    }
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingHabitId by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.selectDate(LocalDate.now())
    }

    val summary = summarizeHabitDay(date.toEpochDay(), habits)
    val totalHabits = habits.size
    val progressPercentage = if (totalHabits == 0) 0 else (summary.completedCount * 100) / totalHabits
    val targetProgressFloat = if (totalHabits == 0) 0f else summary.completedCount.toFloat() / totalHabits.toFloat()
    val animatedProgressFloat by animateFloatAsState(
        targetValue = targetProgressFloat,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "homeDailyProgress"
    )
    val remainingHabits = totalHabits - summary.completedCount

    Box(
        modifier = modifier
            .offset(x = if (visible) 0.dp else 10_000.dp)
            .background(Color(0xFF12111A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 3.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Row: Greeting + Glowing Date Badge + History Shortcut aligned with title text
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today is yours.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    // Quick History / Insights pill aligned directly with "Today is yours."
                    Surface(
                        onClick = onOpenHistory,
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF241F35),
                        border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "History",
                                tint = Color(0xFFC4B5FD),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "History",
                                color = Color(0xFFC4B5FD),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = { vm.previousDay() },
                        shape = CircleShape,
                        color = Color(0xFF1E1C2A),
                        border = BorderStroke(1.dp, Color(0xFF343048)),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("‹", color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Surface(
                        onClick = { vm.selectDate(LocalDate.now()) },
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF1E1C2A),
                        border = BorderStroke(1.dp, Color(0xFF343048))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🗓",
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (date == LocalDate.now()) {
                                    "Today, " + date.format(DateTimeFormatter.ofPattern("MMM d"))
                                } else {
                                    date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                    if (date.isBefore(LocalDate.now())) {
                        Surface(
                            onClick = { vm.nextDay() },
                            shape = CircleShape,
                            color = Color(0xFF1E1C2A),
                            border = BorderStroke(1.dp, Color(0xFF343048)),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("›", color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Compact Daily Progress Hero Card with Inline Status Badges (Keeps Habits Above the Fold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.2.dp,
                    if (totalHabits > 0 && remainingHabits == 0) {
                        Color(0xFF00DC82).copy(alpha = 0.65f)
                    } else {
                        Color(0xFFA78BFA).copy(alpha = 0.35f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF3B3548),
                                    Color(0xFF2D273B),
                                    Color(0xFF1F1C2C)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(900f, 900f)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top section: Circular Progress Indicator & Completion Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Larger Circular Gauge
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF00DC82).copy(alpha = 0.16f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { animatedProgressFloat },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFF00DC82),
                                trackColor = Color(0xFF14131C),
                                strokeWidth = 7.5.dp
                            )
                            Text(
                                text = "$progressPercentage%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        // Progress Text Details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${summary.completedCount} of $totalHabits completed",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF28203E),
                                    border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color(0xFF00DC82), CircleShape)
                                        )
                                        Text(
                                            text = "Today",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFFEDE5FF)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = when {
                                    totalHabits == 0 -> "Add habits to begin tracking your daily rhythm."
                                    remainingHabits > 0 -> "$remainingHabits more habit${if (remainingHabits > 1) "s" else ""} to complete your day."
                                    else -> "All habits completed for today! Keep glowing."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (totalHabits > 0 && remainingHabits == 0) {
                                    Color(0xFF00DC82)
                                } else {
                                    Color(0xFFC4BED6)
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Prominent Status Badges Row (Done, Missed, Left — bigger & clearly visible)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactStatusPill(
                            count = summary.completedCount,
                            label = "Done",
                            symbol = "✓",
                            accent = Color(0xFF00DC82),
                            modifier = Modifier.weight(1f)
                        )
                        CompactStatusPill(
                            count = summary.notCompletedCount,
                            label = "Missed",
                            symbol = "✕",
                            accent = Color(0xFFFF5252),
                            modifier = Modifier.weight(1f)
                        )
                        CompactStatusPill(
                            count = summary.unrecordedCount,
                            label = "Left",
                            symbol = "⏸",
                            accent = Color(0xFFA78BFA),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Horizontal Linear Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedProgressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(50)),
                        color = Color(0xFF00DC82),
                        trackColor = Color(0xFF14131C)
                    )
                }
            }

            // Sleek Compact Active Program Strip (Horizontal carousel if >1 active program so habits stay visible!)
            if (activePrograms.isNotEmpty()) {
                val isMultiProgram = activePrograms.size > 1
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isMultiProgram) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ONGOING PROGRAMS (${activePrograms.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC4B5FD),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Swipe for more →",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8E8AA7),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isMultiProgram) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        activePrograms.forEach { activeProgram ->
                            val activeDayActivity = activeProgram.activities.firstOrNull { it.epochDay == date.toEpochDay() }
                                ?: activeProgram.todayActivities.firstOrNull()
                            Card(
                                onClick = { onOpenActiveProgram(activeProgram.enrollment.id) },
                                modifier = if (isMultiProgram) Modifier.width(305.dp) else Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF28213D),
                                                    Color(0xFF1C192A)
                                                )
                                            ),
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Compact Percentage Badge
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(Color(0xFF0D3325), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${activeProgram.progressPercent}%",
                                            color = Color(0xFF00DC82),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    // Program Name & Today's Session
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = activeProgram.enrollment.titleSnapshot,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = if (activeDayActivity != null) {
                                                "Day ${activeDayActivity.dayIndex}: ${activeDayActivity.title}"
                                            } else {
                                                "Tap to open active program log"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFC4B5FD),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Always opens ActiveProgramScreen on card tap
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFF2A2340),
                                        border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.45f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Open",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp,
                                                color = Color(0xFFEDE5FF)
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Open program",
                                                tint = Color(0xFFC4B5FD),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                        color = Color(0xFF262338),
                        border = BorderStroke(1.dp, Color(0xFF3A3550))
                    ) {
                        Text(
                            text = "$totalHabits habits",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC4B5FD),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Button(
                    onClick = {
                        editingHabitId = null
                        title = ""
                        description = ""
                        formError = null
                        showAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC4B5FD),
                        contentColor = Color(0xFF171020)
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("+ Add habit", fontWeight = FontWeight.Bold)
                }
            }

            // Habits List or Empty State
            if (isLoading) {
                SectionLoading(message = "Loading today's habits…")
            } else if (habits.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.28f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color(0xFF2A243D), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFC4B5FD),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "A fresh start.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tap a science-backed starter habit below to add it in one tap, or create your own custom routine.",
                            color = Color(0xFF9E98B3),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StarterHabitPresets.take(4).forEach { preset ->
                                Surface(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        vm.addHabit(
                                            title = preset.title,
                                            description = preset.description,
                                            onError = {}
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF262238),
                                    border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "+ ${preset.title}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE0D7FF),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(2.dp))
                        Button(
                            onClick = {
                                editingHabitId = null
                                title = ""
                                description = ""
                                formError = null
                                showAddDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00DC82),
                                contentColor = Color(0xFF06291A)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("+ Create custom habit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    habits.forEach { entry ->
                        key(entry.habit.id) {
                            HabitCard(
                                entry = entry,
                                isFuture = false,
                                canArchive = true,
                                onToggle = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    vm.toggleCompletion(entry)
                                },
                                onMarkNotCompleted = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    vm.markNotCompleted(entry)
                                },
                                onArchive = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val archivedHabitId = entry.habit.id
                                    val archivedHabitTitle = entry.habit.title
                                    vm.archiveHabit(entry)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "\"$archivedHabitTitle\" archived to History",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            historyViewModel.reactivateHabitResume(archivedHabitId)
                                        }
                                    }
                                },
                                onClear = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    vm.clearCompletion(entry)
                                },
                                onEdit = {
                                    editingHabitId = entry.habit.id
                                    title = entry.habit.title
                                    description = entry.habit.description
                                    formError = null
                                    showAddDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }

    if (showAddDialog) {
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFFA78BFA),
            unfocusedBorderColor = Color(0xFF3A364E),
            focusedLabelColor = Color(0xFFC4B5FD),
            unfocusedLabelColor = Color(0xFF9E98B3),
            cursorColor = Color(0xFFA78BFA),
            focusedContainerColor = Color(0xFF161421),
            unfocusedContainerColor = Color(0xFF161421)
        )

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingHabitId = null
            },
            containerColor = Color(0xFF1E1C2A),
            titleContentColor = Color.White,
            textContentColor = Color(0xFF9E98B3),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    if (editingHabitId != null) "Edit habit" else "Create a habit",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (editingHabitId == null) {
                        Text(
                            text = "QUICK-FILL TEMPLATES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC4B5FD),
                            letterSpacing = 0.8.sp
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StarterHabitPresets.forEach { preset ->
                                Surface(
                                    onClick = {
                                        title = preset.title
                                        description = preset.description
                                        formError = null
                                    },
                                    shape = RoundedCornerShape(50),
                                    color = if (title == preset.title) Color(0xFF362B54) else Color(0xFF161421),
                                    border = BorderStroke(
                                        1.dp,
                                        if (title == preset.title) Color(0xFFA78BFA) else Color(0xFF343048)
                                    )
                                ) {
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFD5CFE8),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; formError = null },
                        label = { Text("Habit name (e.g. Morning workout)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it; formError = null },
                        label = { Text("Why it matters / daily target (optional)") },
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(visible = formError != null) {
                        formError?.let { err ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF381418),
                                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = err,
                                        color = Color(0xFFFF8A8A),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
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
                                val targetId = editingHabitId
                                if (targetId != null) {
                                    vm.updateHabit(
                                        habitId = targetId,
                                        title = cleanTitle,
                                        description = description.trim(),
                                        onSuccess = {
                                            showAddDialog = false
                                            editingHabitId = null
                                        }
                                    )
                                } else {
                                    vm.addHabit(
                                        title = cleanTitle,
                                        description = description.trim(),
                                        onError = { formError = it },
                                        onSuccess = { showAddDialog = false }
                                    )
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00DC82),
                        contentColor = Color(0xFF06291A)
                    )
                ) {
                    Text(
                        if (editingHabitId != null) "Save changes" else "Create habit",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        editingHabitId = null
                    }
                ) {
                    Text("Cancel", color = Color(0xFFC4B5FD))
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
    onClear: () -> Unit,
    onEdit: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val (icon, iconColor) = habitVisualForTitle(entry.habit.title)

    val cardBorderColor = when (entry.status) {
        HabitDayStatus.COMPLETED -> Color(0xFF00DC82).copy(alpha = 0.35f)
        HabitDayStatus.NOT_COMPLETED -> Color(0xFFFF5252).copy(alpha = 0.30f)
        HabitDayStatus.UNRECORDED -> Color(0xFF2E2C3D)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardBorderColor)
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
                // Circular glowing icon container
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    iconColor.copy(alpha = 0.22f),
                                    Color(0xFF262438)
                                )
                            ),
                            shape = CircleShape
                        ),
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
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF2D2518),
                                border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${entry.currentStreak}",
                                        color = Color(0xFFFDE68A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
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
                        text = entry.habit.description.ifBlank { "Daily practice" },
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
                            border = BorderStroke(1.dp, Color(0xFF00DC82).copy(alpha = 0.55f))
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
                            border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.55f))
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
                            color = Color(0xFF252236),
                            border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.40f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Circle,
                                    contentDescription = null,
                                    tint = Color(0xFFC4B5FD),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Mark complete",
                                    color = Color(0xFFF3F1FA),
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
                        when (entry.status) {
                            HabitDayStatus.COMPLETED -> {
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
                            }
                            HabitDayStatus.NOT_COMPLETED -> {
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
                            }
                            HabitDayStatus.UNRECORDED -> {
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
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            TextButton(
                                onClick = {
                                    onEdit()
                                    expanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Edit", color = Color(0xFFE2E8F0), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
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
                                    Text("Archive", color = Color(0xFFC4B5FD), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactStatusPill(
    count: Int,
    label: String,
    symbol: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.16f),
        border = BorderStroke(1.2.dp, accent.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$symbol $count",
                color = accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

