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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.presentation.history.HistoryRange
import com.virtue.habittracker.presentation.history.HistoryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private enum class HabitFilter { ACTIVE, ARCHIVED }

private data class AggregateInsightStyle(
    val badgeText: String,
    val badgeColor: Color,
    val headlineText: String
)

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
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf(false) }
    var habitFilter by remember { mutableStateOf(HabitFilter.ACTIVE) }

    val allItems = rangeSummary.habitPerformanceList
    val activeCount = allItems.count { it.entry.habit.inactiveFromEpochDay == null }
    val archivedCount = allItems.count { it.entry.habit.inactiveFromEpochDay != null }

    val activePerformance = allItems.filter {
        val isArchived = it.entry.habit.inactiveFromEpochDay != null
        if (habitFilter == HabitFilter.ACTIVE) !isArchived else isArchived
    }

    val animatedOverallProgress by animateFloatAsState(
        targetValue = rangeSummary.overallPercentage / 100f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "historyOverallProgress"
    )

    val insightStyle = remember(rangeSummary.totalRecordedDays, rangeSummary.overallPercentage) {
        when {
            rangeSummary.totalRecordedDays == 0 -> AggregateInsightStyle(
                badgeText = "No Data",
                badgeColor = Color(0xFF8E8AA7),
                headlineText = "Complete your first check-in to unlock insights"
            )
            rangeSummary.overallPercentage >= 80 -> AggregateInsightStyle(
                badgeText = "Optimal",
                badgeColor = Color(0xFF00DC82),
                headlineText = "Elite consistency across focal rituals"
            )
            rangeSummary.overallPercentage >= 50 -> AggregateInsightStyle(
                badgeText = "Building",
                badgeColor = Color(0xFFA78BFA),
                headlineText = "Steady momentum across your routines"
            )
            else -> AggregateInsightStyle(
                badgeText = "Recovering",
                badgeColor = Color(0xFFFBBF24),
                headlineText = "Room to sharpen your daily rhythm"
            )
        }
    }

    val rangeDescription = when (selectedRange) {
        HistoryRange.SEVEN_DAYS -> "Last 7 days"
        HistoryRange.THIRTY_DAYS -> "Last 30 days"
        HistoryRange.NINETY_DAYS -> "Last 90 days"
        HistoryRange.ALL_TIME -> "All recorded history"
        HistoryRange.SPECIFIC_DATE -> customDate?.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")) ?: "Selected date"
    }

    Box(
        modifier = modifier
            .offset(x = if (visible) 0.dp else 10_000.dp)
            .background(Color(0xFF12111A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Editorial Screen Header (matching Home & Profile tabs)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "PERFORMANCE & ANALYTICS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E8AA7),
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = "Your History",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF1E1C2B),
                    border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(insightStyle.badgeColor, CircleShape)
                        )
                        Text(
                            text = rangeDescription,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC4B5FD),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Top Filter Tabs (7D, 30D, 90D, All, Custom Date)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HistoryTabChip(
                    label = "7D",
                    selected = selectedRange == HistoryRange.SEVEN_DAYS,
                    onClick = { vm.selectRange(HistoryRange.SEVEN_DAYS) },
                    modifier = Modifier.weight(1f)
                )
                HistoryTabChip(
                    label = "30D",
                    selected = selectedRange == HistoryRange.THIRTY_DAYS,
                    onClick = { vm.selectRange(HistoryRange.THIRTY_DAYS) },
                    modifier = Modifier.weight(1f)
                )
                HistoryTabChip(
                    label = "90D",
                    selected = selectedRange == HistoryRange.NINETY_DAYS,
                    onClick = { vm.selectRange(HistoryRange.NINETY_DAYS) },
                    modifier = Modifier.weight(1f)
                )
                HistoryTabChip(
                    label = "All",
                    selected = selectedRange == HistoryRange.ALL_TIME,
                    onClick = { vm.selectRange(HistoryRange.ALL_TIME) },
                    modifier = Modifier.weight(1f)
                )
                HistoryTabChip(
                    label = if (selectedRange == HistoryRange.SPECIFIC_DATE && customDate != null) {
                        customDate!!.format(DateTimeFormatter.ofPattern("MMM d"))
                    } else {
                        "Date"
                    },
                    showCalendarIcon = true,
                    selected = selectedRange == HistoryRange.SPECIFIC_DATE,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1.35f)
                )
            }

            // AGGREGATE METRIC: Overall completion Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.30f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF393344),
                                    Color(0xFF2B253A),
                                    Color(0xFF1E1C2A)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(900f, 900f)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
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
                                color = Color(0xFFC4B5FD),
                                letterSpacing = 1.2.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Overall completion",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            insightStyle.badgeColor.copy(alpha = 0.28f),
                                            Color(0xFF252036)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Insights,
                                contentDescription = null,
                                tint = insightStyle.badgeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Gauge + Dynamic Text insight
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            insightStyle.badgeColor.copy(alpha = 0.16f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { animatedOverallProgress },
                                modifier = Modifier.fillMaxSize(),
                                color = insightStyle.badgeColor,
                                trackColor = Color(0xFF14131C),
                                strokeWidth = 9.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${rangeSummary.overallPercentage}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = insightStyle.badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = insightStyle.badgeColor
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = insightStyle.headlineText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Based on ${rangeSummary.totalRecordedDays} recorded habit-days (excluding unrecorded)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB5AFC7)
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
                            icon = "✕",
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
                            accent = Color(0xFFA78BFA)
                        )
                    }
                }
            }

            // Your consistency Bar Chart Card
            val selectedPoint = weeklyProgress.find { it.date == selectedChartDay } ?: weeklyProgress.lastOrNull()
            val weekRangeLabel = if (weeklyProgress.isNotEmpty()) {
                val firstDay = weeklyProgress.first().date.format(DateTimeFormatter.ofPattern("MMM d"))
                val lastDay = weeklyProgress.last().date.format(DateTimeFormatter.ofPattern("MMM d"))
                "$firstDay – $lastDay • Tap a bar to inspect"
            } else {
                "Tap a bar to inspect that day's history"
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF2E2C3D))
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Your consistency",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = weekRangeLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8E8AA7)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (vm.canMoveToNextWeek()) {
                                Surface(
                                    onClick = { vm.goToCurrentWeek() },
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF29233B),
                                    border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = "Today",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC4B5FD),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Surface(
                                onClick = { vm.previousWeek() },
                                shape = CircleShape,
                                color = Color(0xFF282438),
                                border = BorderStroke(1.dp, Color(0xFF3A3550)),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("‹", color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                            Surface(
                                onClick = { vm.nextWeek() },
                                enabled = vm.canMoveToNextWeek(),
                                shape = CircleShape,
                                color = if (vm.canMoveToNextWeek()) Color(0xFF282438) else Color(0xFF1A1826),
                                border = BorderStroke(1.dp, Color(0xFF3A3550)),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "›",
                                        color = if (vm.canMoveToNextWeek()) Color(0xFFC4B5FD) else Color(0xFF5A5570),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Selected Bar Information Banner
                    selectedPoint?.let { point ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF29233B),
                            border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("•", color = Color(0xFF00DC82), fontWeight = FontWeight.ExtraBold)
                                Text(
                                    text = "${point.date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))} • ${point.completionRatePercent}% completion (${point.completedCount} of ${point.completedCount + point.notCompletedCount + point.unrecordedCount} completed, ${point.notCompletedCount} missed)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Bar Chart Rendering with Honest 0% Stub & Two-Line Day/Date Labels
                    if (weeklyProgress.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyProgress.forEach { point ->
                                val isSelected = point.date == selectedChartDay
                                val isToday = point.date == LocalDate.now()
                                val hasCompletions = point.completedCount > 0
                                val targetRatio = if (!hasCompletions) {
                                    0.12f
                                } else {
                                    (point.completionRatePercent / 100f).coerceIn(0.22f, 1f)
                                }
                                val barHeightRatio by animateFloatAsState(
                                    targetValue = targetRatio,
                                    animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                                    label = "barHeight"
                                )
                                val barColor = when {
                                    !hasCompletions && isSelected -> Color(0xFF3E3856)
                                    !hasCompletions -> Color(0xFF282538)
                                    isSelected -> Color(0xFF00DC82)
                                    else -> Color(0xFF10B981).copy(alpha = 0.58f)
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { vm.selectChartDay(point.date) }
                                        .padding(vertical = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.76f)
                                            .height((96 * barHeightRatio).dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                            .background(barColor),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        if (hasCompletions) {
                                            Text(
                                                text = point.completedCount.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFF05291A) else Color.White,
                                                modifier = Modifier.padding(bottom = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = point.date.format(DateTimeFormatter.ofPattern("EEE")),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = when {
                                            isSelected -> Color(0xFF00DC82)
                                            isToday -> Color(0xFFC4B5FD)
                                            else -> Color(0xFF8E8AA7)
                                        }
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = when {
                                            isSelected -> Color(0xFF00DC82).copy(alpha = 0.18f)
                                            isToday -> Color(0xFFA78BFA).copy(alpha = 0.18f)
                                            else -> Color.Transparent
                                        }
                                    ) {
                                        Text(
                                            text = point.date.dayOfMonth.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                            fontSize = 10.sp,
                                            color = when {
                                                isSelected -> Color(0xFF00DC82)
                                                isToday -> Color(0xFFC4B5FD)
                                                else -> Color(0xFF6E6A85)
                                            },
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Habit Performance Section Header + Active / Archived Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Habit performance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (habitFilter == HabitFilter.ACTIVE) Color(0xFF2E2741) else Color(0xFF1E1C2A),
                        border = BorderStroke(
                            1.dp,
                            if (habitFilter == HabitFilter.ACTIVE) Color(0xFFA78BFA).copy(alpha = 0.6f) else Color(0xFF2E2C3D)
                        ),
                        modifier = Modifier.clickable { habitFilter = HabitFilter.ACTIVE }
                    ) {
                        Text(
                            text = "Active ($activeCount)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (habitFilter == HabitFilter.ACTIVE) Color(0xFFEDE5FF) else Color(0xFF8E8AA7),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (habitFilter == HabitFilter.ARCHIVED) Color(0xFF2E2741) else Color(0xFF1E1C2A),
                        border = BorderStroke(
                            1.dp,
                            if (habitFilter == HabitFilter.ARCHIVED) Color(0xFFA78BFA).copy(alpha = 0.6f) else Color(0xFF2E2C3D)
                        ),
                        modifier = Modifier.clickable { habitFilter = HabitFilter.ARCHIVED }
                    ) {
                        Text(
                            text = "Archived ($archivedCount)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (habitFilter == HabitFilter.ARCHIVED) Color(0xFFEDE5FF) else Color(0xFF8E8AA7),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Habit Performance List or Filter-Aware Empty State
            if (isLoading && activePerformance.isEmpty()) {
                SectionLoading(message = "Loading habit history…")
            } else if (activePerformance.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E2C3D))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF282438), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (habitFilter == HabitFilter.ARCHIVED) Icons.Filled.Archive else Icons.Filled.Insights,
                                contentDescription = null,
                                tint = Color(0xFFC4B5FD),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = if (habitFilter == HabitFilter.ARCHIVED) {
                                "No archived habits"
                            } else {
                                "No habit performance recorded yet"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (habitFilter == HabitFilter.ARCHIVED) {
                                "Habits you archive from the Home screen will appear here so you can restore them anytime."
                            } else {
                                "Practice habits on the Home tab to build your performance history."
                            },
                            color = Color(0xFF8E8AA7),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    activePerformance.forEach { item ->
                        key(item.entry.habit.id) {
                            HabitPerformanceCard(
                                item = item,
                                onReactivateResume = {
                                    vm.reactivateHabitResume(item.entry.habit.id)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("\"${item.entry.habit.title}\" restored with previous history")
                                    }
                                },
                                onReactivateFreshStart = {
                                    vm.reactivateHabitFreshStart(item.entry.habit.id)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("\"${item.entry.habit.title}\" restarted from zero")
                                    }
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

    // Date Picker Dialog when tapping "Date"
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
    modifier: Modifier = Modifier,
    showCalendarIcon: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF9E86E7) else Color(0xFF1E1C2A),
        border = if (selected) null else BorderStroke(1.dp, Color(0xFF2E2C3D))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showCalendarIcon) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = if (selected) Color.White else Color(0xFF8E8AA7),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Color(0xFF8E8AA7),
                maxLines = 1
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
            Text(icon, color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9E98B3)
        )
    }
}

@Composable
private fun HabitPerformanceCard(
    item: com.virtue.habittracker.presentation.history.HabitPerformanceItem,
    onReactivateResume: () -> Unit = {},
    onReactivateFreshStart: () -> Unit = {}
) {
    val targetProgress = if (item.totalDays == 0) 0f else item.completedDays.toFloat() / item.totalDays.toFloat()
    val progressFloat by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "habitPerformanceBar"
    )
    val isArchived = item.entry.habit.inactiveFromEpochDay != null
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "chevronRotation"
    )
    val (icon, iconColor) = habitVisualForTitle(item.entry.habit.title)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (isArchived) Color(0xFFA78BFA).copy(alpha = 0.40f) else Color(0xFF2E2C3D)
        )
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
                // Unified Vector Category Icon with Radial Glow
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    iconColor.copy(alpha = 0.24f),
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

                // Title & Subtitle Stats
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.entry.habit.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (item.entry.currentStreak > 0 && !isArchived) {
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
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${item.entry.currentStreak}",
                                        color = Color(0xFFFDE68A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "${item.completedDays}/${item.totalDays} recorded days • " +
                            if (isArchived) "Archived (tap to restore)"
                            else if (item.entry.currentStreak > 0) "${item.entry.currentStreak}-day streak"
                            else "Tap for details",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isArchived) Color(0xFFC4B5FD) else Color(0xFF8E8AA7)
                    )
                }

                // Completion Percentage & Animated Rotating Vector Chevron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${item.completionPercentage}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isArchived) Color(0xFFC4B5FD) else Color(0xFF00DC82)
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = if (isArchived) Color(0xFFC4B5FD) else Color(0xFF8E8AA7),
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation)
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
                color = if (isArchived) Color(0xFFA78BFA) else Color(0xFF00DC82),
                trackColor = Color(0xFF14131C)
            )

            // Expandable Drawer: Reactivation options for Archived OR Detailed breakdown for Active
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = Color(0xFF2E2C3D), thickness = 1.dp)

                    if (isArchived) {
                        Text(
                            text = "Re-activate Habit — Keep old count or start from zero?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC4B5FD)
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
                                    containerColor = Color(0xFF0D3325),
                                    contentColor = Color(0xFF00DC82)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF00DC82).copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Restore,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "Keep old count",
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
                                    containerColor = Color(0xFF28203E),
                                    contentColor = Color(0xFFC4B5FD)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "Start from zero",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        val missedDays = (item.totalDays - item.completedDays).coerceAtLeast(0)
                        val createdDate = LocalDate.ofEpochDay(item.entry.habit.createdEpochDay)
                            .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "✓ ${item.completedDays} completed • ✕ $missedDays missed",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFB5AFC7),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Started $createdDate",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8E8AA7)
                            )
                        }
                        if (item.entry.habit.description.isNotBlank()) {
                            Text(
                                text = item.entry.habit.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9E98B3)
                            )
                        }
                    }
                }
            }
        }
    }
}
