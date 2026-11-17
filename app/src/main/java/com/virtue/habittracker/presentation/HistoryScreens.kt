package com.virtue.habittracker.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.model.HabitDayStatus
import com.virtue.habittracker.presentation.history.DailyProgressPoint
import com.virtue.habittracker.presentation.history.HistoryRange
import com.virtue.habittracker.presentation.history.HistoryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private enum class HabitFilter { ACTIVE, ARCHIVED }

@Composable
fun HistoryTab(
    modifier: Modifier,
    visible: Boolean,
    vm: HistoryViewModel
) {
    val selectedRange by vm.selectedRange.collectAsStateWithLifecycle()
    val customDate by vm.customDate.collectAsStateWithLifecycle()
    val rangeSummary by vm.rangeSummary.collectAsStateWithLifecycle()
    val weeklyProgress by vm.weeklyProgress.collectAsStateWithLifecycle()
    val selectedChartDay by vm.selectedChartDay.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var habitFilter by remember { mutableStateOf(HabitFilter.ACTIVE) }

    val activePerformance = rangeSummary.habitPerformanceList.filter {
        val isArchived = it.entry.habit.inactiveFromEpochDay != null
        if (habitFilter == HabitFilter.ACTIVE) !isArchived else isArchived
    }

    Column(
        modifier = modifier
            .offset(x = if (visible) 0.dp else 10_000.dp)
            .background(Color(0xFF12111A))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Filter Tabs (7 days, 30 days, 90 days, All time, Choose date)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryTabChip(
                label = "7 days",
                selected = selectedRange == HistoryRange.SEVEN_DAYS,
                onClick = { vm.selectRange(HistoryRange.SEVEN_DAYS) },
                modifier = Modifier.weight(1f)
            )
            HistoryTabChip(
                label = "30 days",
                selected = selectedRange == HistoryRange.THIRTY_DAYS,
                onClick = { vm.selectRange(HistoryRange.THIRTY_DAYS) },
                modifier = Modifier.weight(1f)
            )
            HistoryTabChip(
                label = "90 days",
                selected = selectedRange == HistoryRange.NINETY_DAYS,
                onClick = { vm.selectRange(HistoryRange.NINETY_DAYS) },
                modifier = Modifier.weight(1f)
            )
            HistoryTabChip(
                label = "All time",
                selected = selectedRange == HistoryRange.ALL_TIME,
                onClick = { vm.selectRange(HistoryRange.ALL_TIME) },
                modifier = Modifier.weight(1f)
            )
            HistoryTabChip(
                label = if (selectedRange == HistoryRange.SPECIFIC_DATE && customDate != null) {
                    customDate!!.format(DateTimeFormatter.ofPattern("MMM d"))
                } else {
                    "Choose date"
                },
                selected = selectedRange == HistoryRange.SPECIFIC_DATE,
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1.2f)
            )
        }

        // AGGREGATE METRIC: Overall completion Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AGGREGATE METRIC",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E8AA7),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Overall completion",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text("🏁", fontSize = 20.sp)
                }

                // Gauge + Text insight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Box(
                        modifier = Modifier.size(84.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { rangeSummary.overallPercentage / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF00DC82),
                            trackColor = Color(0xFF14131C),
                            strokeWidth = 9.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${rangeSummary.overallPercentage}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (rangeSummary.overallPercentage >= 75) "Optimal" else "Good",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00DC82)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Strong consistency across focal rituals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Based on ${rangeSummary.totalRecordedDays} recorded habit-days (excluding unrecorded)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8AA7)
                        )
                    }
                }

                // 3 Inline Metric Stats: Done, Missed, Idle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricStatItem(
                        icon = "✓",
                        label = "Done",
                        count = rangeSummary.completedCount,
                        sublabel = "Completed",
                        accent = Color(0xFF00DC82)
                    )
                    MetricStatItem(
                        icon = "🎯",
                        label = "Missed",
                        count = rangeSummary.missedCount,
                        sublabel = "Not completed",
                        accent = Color(0xFFFF5252)
                    )
                    MetricStatItem(
                        icon = "○",
                        label = "Idle",
                        count = rangeSummary.unrecordedCount,
                        sublabel = "Unrecorded",
                        accent = Color(0xFF8F9BB3)
                    )
                }
            }
        }

        // Your consistency Bar Chart Card
        val selectedPoint = weeklyProgress.find { it.date == selectedChartDay } ?: weeklyProgress.lastOrNull()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Your consistency",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tap a bar to inspect that day's history",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8AA7)
                        )
                    }
                    Text("📊", fontSize = 20.sp)
                }

                // Selected Bar Information Banner
                selectedPoint?.let { point ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF29233B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("•", color = Color(0xFF9E86E7), fontWeight = FontWeight.Bold)
                            Text(
                                text = "${point.date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))} • ${point.completionRatePercent}% completion (${point.completedCount} of ${point.completedCount + point.notCompletedCount + point.unrecordedCount} completed, ${point.notCompletedCount} missed)",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Bar Chart Rendering
                if (weeklyProgress.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyProgress.forEach { point ->
                            val isSelected = point.date == selectedChartDay
                            val barHeightRatio = (point.completionRatePercent / 100f).coerceIn(0.15f, 1f)
                            val barColor = if (isSelected) Color(0xFF00DC82) else Color(0xFF10B981).copy(alpha = 0.6f)

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { vm.selectChartDay(point.date) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.75f)
                                        .height((100 * barHeightRatio).dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(barColor),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Text(
                                        text = point.completedCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = point.date.format(DateTimeFormatter.ofPattern("EE")).take(1),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF8E8AA7)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Habit Performance Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Habit performance (${activePerformance.size} habits)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (habitFilter == HabitFilter.ACTIVE) Color(0xFF2E2741) else Color(0xFF1E1C2A),
                    modifier = Modifier.clickable { habitFilter = HabitFilter.ACTIVE }
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (habitFilter == HabitFilter.ACTIVE) Color(0xFF9E86E7) else Color(0xFF8E8AA7),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (habitFilter == HabitFilter.ARCHIVED) Color(0xFF2E2741) else Color(0xFF1E1C2A),
                    modifier = Modifier.clickable { habitFilter = HabitFilter.ARCHIVED }
                ) {
                    Text(
                        text = "Archived",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (habitFilter == HabitFilter.ARCHIVED) Color(0xFF9E86E7) else Color(0xFF8E8AA7),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Habit Performance List
        if (activePerformance.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "No habit performance recorded yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Practice habits on the Home tab to build your performance history.",
                        color = Color(0xFF8E8AA7)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                activePerformance.forEach { item ->
                    HabitPerformanceCard(
                        item = item,
                        onReactivateResume = { vm.reactivateHabitResume(item.entry.habit.id) },
                        onReactivateFreshStart = { vm.reactivateHabitFreshStart(item.entry.habit.id) }
                    )
                }
            }
        }
    }

    // Date Picker Dialog when tapping "Choose date"
    if (showDatePicker) {
        val initialMillis = (customDate ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        vm.selectCustomDate(selected)
                    }
                    showDatePicker = false
                }) {
                    Text("Select Date", fontWeight = FontWeight.Bold, color = Color(0xFF00DC82))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color(0xFF8E8AA7))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun HistoryTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF9E86E7) else Color(0xFF1E1C2A),
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2C3D))
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Color(0xFF8E8AA7)
            )
        }
    }
}

@Composable
private fun MetricStatItem(
    icon: String,
    label: String,
    count: Int,
    sublabel: String,
    accent: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, color = accent, fontSize = 14.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8E8AA7)
        )
    }
}

@Composable
private fun HabitPerformanceCard(
    item: com.virtue.habittracker.presentation.history.HabitPerformanceItem,
    onReactivateResume: () -> Unit = {},
    onReactivateFreshStart: () -> Unit = {}
) {
    val progressFloat = if (item.totalDays == 0) 0f else item.completedDays.toFloat() / item.totalDays.toFloat()
    val isArchived = item.entry.habit.inactiveFromEpochDay != null
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isArchived) Color(0xFF4A3B66) else Color(0xFF2E2C3D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF2E2741), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            item.entry.habit.title.contains("workout", true) || item.entry.habit.title.contains("exercise", true) -> "↗"
                            item.entry.habit.title.contains("read", true) -> "📖"
                            item.entry.habit.title.contains("water", true) -> "💧"
                            item.entry.habit.title.contains("meditat", true) -> "🧘"
                            item.entry.habit.title.contains("kotlin", true) || item.entry.habit.title.contains("code", true) -> "💻"
                            else -> "◆"
                        },
                        fontSize = 18.sp
                    )
                }

                // Title & Subtitle Stats
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = item.entry.habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${item.completedDays}/${item.totalDays} recorded days • " +
                            if (isArchived) "Archived (tap for options)"
                            else if (item.entry.currentStreak > 0) "${item.entry.currentStreak}-day streak"
                            else "Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isArchived) Color(0xFFC4B5FD) else Color(0xFF8E8AA7)
                    )
                }

                // Completion Percentage & Dropdown Arrow / Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${item.completionPercentage}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isArchived) Color(0xFF9E86E7) else Color(0xFF00DC82)
                    )
                    Text(
                        text = if (isArchived) (if (expanded) "▲" else "▼") else ">",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isArchived) Color(0xFF9E86E7) else Color(0xFF8E8AA7)
                    )
                }
            }

            // Bottom Progress Bar
            LinearProgressIndicator(
                progress = { progressFloat },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = if (isArchived) Color(0xFF9E86E7) else Color(0xFF00DC82),
                trackColor = Color(0xFF14131C)
            )

            // Reactivation options dropdown for archived habits
            AnimatedVisibility(visible = isArchived && expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = Color(0xFF2E2C3D), thickness = 1.dp)

                    Text(
                        text = "Re-activate Habit Options",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9E86E7)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onReactivateResume()
                                expanded = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E2741),
                                contentColor = Color(0xFF00DC82)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "🔄 Resume active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                onReactivateFreshStart()
                                expanded = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E2741),
                                contentColor = Color(0xFFC4B5FD)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "⚡ Fresh start",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
