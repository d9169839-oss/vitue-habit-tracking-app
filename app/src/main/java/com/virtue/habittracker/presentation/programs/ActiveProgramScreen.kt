package com.virtue.habittracker.presentation.programs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtue.habittracker.domain.model.program.ProgramActivityStatus
import com.virtue.habittracker.domain.model.program.ProgramCatalog
import com.virtue.habittracker.domain.model.program.ProgramProgress
import com.virtue.habittracker.domain.model.program.ProgramStatus
import com.virtue.habittracker.domain.model.program.ScheduledProgramActivity
import com.virtue.habittracker.domain.model.program.catalog.BuildLeanMuscleProgram
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ActiveProgramScreen(
    progress: ProgramProgress,
    onBack: () -> Unit,
    onAdjust: () -> Unit,
    onActivityStatus: (String, ProgramActivityStatus) -> Unit,
    onProgramStatus: (ProgramStatus) -> Unit,
    onDelete: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var showDelete by remember { mutableStateOf(false) }
    var showBlueprintExpanded by remember { mutableStateOf(false) }
    var selectedPhaseFilter by remember { mutableIntStateOf(0) } // 0 = Active Window, 1..3 = Phase 1..3
    var expandedActivityId by remember { mutableStateOf<String?>(null) }
    var activeGuidedActivity by remember { mutableStateOf<ScheduledProgramActivity?>(null) }

    val definition = remember(progress.enrollment.templateId) {
        ProgramCatalog.findDefinition(progress.enrollment.templateId) ?: BuildLeanMuscleProgram
    }
    val template = definition.template
    val accentColor = Color(definition.accentHex)

    val diagnosticSummary = remember(definition, progress.enrollment.preferences) {
        val prefs = progress.enrollment.preferences
        definition.buildDiagnosticSummary(
            preferences = prefs,
            step1PrimaryIndex = prefs.step1PrimaryIndex,
            step1SecondaryIndex = prefs.step1SecondaryIndex,
            step2PrimaryIndex = prefs.step2PrimaryIndex,
            step2SecondaryIndex = prefs.step2SecondaryIndex
        )
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (progress.progressPercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "activeProgramProgress"
    )

    val todayEpochDay = LocalDate.now().toEpochDay()
    val currentDayIndex = ((todayEpochDay - progress.enrollment.startEpochDay).toInt() + 1)
        .coerceIn(1, progress.enrollment.durationDays)
    val currentPhase = template.phases.firstOrNull { currentDayIndex in it.startDay..it.endDay }
        ?: template.phases.first()

    val statusAccent = when (progress.enrollment.status) {
        ProgramStatus.ACTIVE -> Color(0xFF00DC82)
        ProgramStatus.PAUSED -> Color(0xFFFBBF24)
        ProgramStatus.COMPLETED -> Color(0xFFA78BFA)
        else -> ProgramMuted
    }

    Scaffold(
        containerColor = Color(0xFF12111A),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ACTIVE PROTOCOL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = progress.enrollment.titleSnapshot,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = onAdjust,
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF1F1C2E),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "Recalibrate",
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Recalibrate",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF12111A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val isCompactWidth = maxWidth < 360.dp
            val horizontalPad = if (isCompactWidth) 14.dp else 18.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPad, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // =========================================================================
                // 1. HERO MASTERY & PHASE ROADMAP CARD
                // =========================================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.16f),
                                        Color(0xFF191726),
                                        Color(0xFF151320)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = statusAccent.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, statusAccent.copy(alpha = 0.45f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(statusAccent, CircleShape)
                                        )
                                        Text(
                                            text = progress.enrollment.status.name,
                                            color = statusAccent,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF232034),
                                    border = BorderStroke(1.dp, Color(0xFF36314C))
                                ) {
                                    Text(
                                        text = "Day $currentDayIndex of ${progress.enrollment.durationDays}",
                                        color = Color(0xFFD5CFE8),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = "${progress.progressPercent}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00DC82)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = Color(0xFF00DC82),
                            trackColor = Color(0xFF1F1C2E)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${progress.completedCount} of ${progress.totalCount} sessions completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB5AFC7)
                            )
                            Text(
                                text = "${progress.enrollment.preferences.availableMinutesPerDay}m · ${progress.enrollment.preferences.availableDaysPerWeek}d/wk",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }

                        HorizontalDivider(color = Color(0xFF2A263D))

                        // 3-Phase Roadmap Pills
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "CURRENT PHASE: PHASE ${currentPhase.number} — ${currentPhase.title.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                                letterSpacing = 0.8.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                template.phases.forEach { phase ->
                                    val isCurrentPhase = phase.number == currentPhase.number
                                    val isCompletedPhase = currentDayIndex > phase.endDay
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = when {
                                            isCurrentPhase -> accentColor.copy(alpha = 0.22f)
                                            isCompletedPhase -> Color(0xFF0D3325)
                                            else -> Color(0xFF1A1826)
                                        },
                                        border = BorderStroke(
                                            width = if (isCurrentPhase) 1.5.dp else 1.dp,
                                            color = when {
                                                isCurrentPhase -> accentColor
                                                isCompletedPhase -> Color(0xFF00DC82).copy(alpha = 0.5f)
                                                else -> Color(0xFF2B273D)
                                            }
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = if (isCompletedPhase) "✓ PHASE ${phase.number}" else "PHASE ${phase.number}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when {
                                                    isCurrentPhase -> accentColor
                                                    isCompletedPhase -> Color(0xFF00DC82)
                                                    else -> Color(0xFF8E8AA7)
                                                }
                                            )
                                            Text(
                                                text = "Days ${phase.startDay}–${phase.endDay}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // 2. COLLAPSIBLE PERSONALIZED DIAGNOSTIC BLUEPRINT CARD
                // =========================================================================
                Card(
                    onClick = { showBlueprintExpanded = !showBlueprintExpanded },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
                    border = BorderStroke(1.dp, Color(0xFF2B273E))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(accentColor.copy(alpha = 0.16f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "MY PERSONALIZED BLUEPRINT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentColor,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = diagnosticSummary.prescriptionHeadline,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (showBlueprintExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Toggle blueprint",
                                tint = Color(0xFFB5AFC7)
                            )
                        }

                        // Always show 3 key diagnostic metric pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            diagnosticSummary.metrics.forEach { metric ->
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (metric.isHighlighted) Color(0xFF112A22) else Color(0xFF1E1B2C),
                                    border = BorderStroke(
                                        1.dp,
                                        if (metric.isHighlighted) Color(0xFF00DC82).copy(alpha = 0.45f) else Color(0xFF2F2B44)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(1.dp)
                                    ) {
                                        Text(
                                            text = metric.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (metric.isHighlighted) Color(0xFF00DC82) else Color(0xFF9E98B5),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = metric.value,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (metric.isHighlighted) Color(0xFF00DC82) else Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = metric.unit,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Color(0xFF8E8AA7),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = showBlueprintExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                HorizontalDivider(color = Color(0xFF282538))
                                Text(
                                    text = diagnosticSummary.prescriptionDetail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD5CFE8),
                                    lineHeight = 18.sp
                                )
                                if (diagnosticSummary.segments.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFF242132)),
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        diagnosticSummary.segments.forEach { segment ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(segment.weight.coerceAtLeast(1f))
                                                    .fillMaxHeight()
                                                    .background(Color(segment.colorHex))
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        diagnosticSummary.segments.forEach { segment ->
                                            Text(
                                                text = "${segment.label}: ${segment.valueText}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(segment.colorHex)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // 3. TODAY'S GUIDED SESSION HERO CARD (WITH INTERACTIVE TIMER PLAYER)
                // =========================================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.5.dp, accentColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ProgramCardGradient, RoundedCornerShape(24.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF00DC82), CircleShape)
                                )
                                Text(
                                    text = "TODAY'S SESSION",
                                    color = Color(0xFF00DC82),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                                color = Color(0xFFB5AFC7),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        val today = progress.todayActivities
                        if (today.isEmpty()) {
                            Text(
                                "Your program starts on ${LocalDate.ofEpochDay(progress.enrollment.startEpochDay).format(DateTimeFormatter.ofPattern("EEE, MMM d"))}. Tap any session in the timeline below to preview or start early.",
                                color = ProgramMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            today.forEach { activity ->
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(
                                            Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Day ${activity.dayIndex} · ${activity.phaseTitle}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = accentColor
                                            )
                                            Text(
                                                text = activity.title,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFF28233C),
                                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                                        ) {
                                            Text(
                                                text = if (activity.estimatedMinutes == 0) "Rest" else "${activity.estimatedMinutes} min",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = activity.instructions,
                                        color = Color(0xFFD5CFE8),
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 20.sp
                                    )

                                    if (activity.estimatedMinutes == 0) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0xFF1A1828),
                                            border = BorderStroke(1.dp, Color(0xFF2D2940))
                                        ) {
                                            Text(
                                                text = "Active Recovery Day · Neural & physiological consolidation. No formal session required today.",
                                                color = Color(0xFFB5AFC7),
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    } else {
                                        // Primary Interactive Guided Session Launch Button
                                        if (activity.status != ProgramActivityStatus.COMPLETED) {
                                            Button(
                                                onClick = {
                                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    activeGuidedActivity = activity
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = accentColor,
                                                    contentColor = Color(0xFF130E1E)
                                                )
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.PlayArrow,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text(
                                                        text = "Start ${activity.estimatedMinutes}-Min Guided Session",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }

                                        // Quick Status Filter Chips
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            FilterChip(
                                                selected = activity.status == ProgramActivityStatus.COMPLETED,
                                                onClick = {
                                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onActivityStatus(activity.id, ProgramActivityStatus.COMPLETED)
                                                },
                                                label = { Text("✓ Mark Done", fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFF0D3325),
                                                    selectedLabelColor = Color(0xFF00DC82)
                                                )
                                            )
                                            FilterChip(
                                                selected = activity.status == ProgramActivityStatus.SKIPPED,
                                                onClick = {
                                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onActivityStatus(activity.id, ProgramActivityStatus.SKIPPED)
                                                },
                                                label = { Text("✕ Skip Today", fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFF381418),
                                                    selectedLabelColor = Color(0xFFFF5252)
                                                )
                                            )
                                            if (activity.status != ProgramActivityStatus.PENDING) {
                                                TextButton(
                                                    onClick = { onActivityStatus(activity.id, ProgramActivityStatus.PENDING) }
                                                ) {
                                                    Text("Reset", color = ProgramLavender, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // 4. INTERACTIVE PHASE-FILTERED TIMELINE (EXPANDABLE & ACTIONABLE)
                // =========================================================================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRICULUM SCHEDULE",
                            color = accentColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Tap any day to inspect or log",
                            color = Color(0xFF8E8AA7),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Phase Filter Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = buildList {
                            add(0 to "Upcoming Window")
                            template.phases.forEach { phase ->
                                add(phase.number to "Phase ${phase.number} (D${phase.startDay}–${phase.endDay})")
                            }
                        }
                        filters.forEach { (phaseNum, label) ->
                            val isSelected = selectedPhaseFilter == phaseNum
                            Surface(
                                onClick = { selectedPhaseFilter = phaseNum },
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) accentColor else Color(0xFF1D1A2B),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) accentColor else Color(0xFF322D46)
                                )
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF130E1E) else Color(0xFFD5CFE8),
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                val filteredActivities = remember(progress.activities, selectedPhaseFilter, todayEpochDay, template.phases) {
                    if (selectedPhaseFilter == 0) {
                        progress.activities.filter { it.epochDay >= todayEpochDay - 3L }.take(14)
                    } else {
                        val phase = template.phases.firstOrNull { it.number == selectedPhaseFilter }
                        if (phase != null) {
                            progress.activities.filter { it.dayIndex in phase.startDay..phase.endDay }
                        } else {
                            progress.activities.take(14)
                        }
                    }
                }

                filteredActivities.forEach { activity ->
                    val isToday = activity.epochDay == todayEpochDay
                    val isExpanded = expandedActivityId == activity.id
                    val formattedDate = LocalDate.ofEpochDay(activity.epochDay).format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                    val (badgeLabel, badgeTextColor, badgeBgColor, badgeBorderColor) = when {
                        activity.status == ProgramActivityStatus.COMPLETED -> Quad(
                            "✓ Completed",
                            Color(0xFF00DC82),
                            Color(0xFF0D3325),
                            Color(0xFF00DC82).copy(alpha = 0.45f)
                        )
                        activity.status == ProgramActivityStatus.SKIPPED -> Quad(
                            "✕ Skipped",
                            Color(0xFFFF5252),
                            Color(0xFF381418),
                            Color(0xFFFF5252).copy(alpha = 0.45f)
                        )
                        isToday -> Quad(
                            "• Today",
                            Color.White,
                            accentColor.copy(alpha = 0.25f),
                            accentColor
                        )
                        activity.estimatedMinutes == 0 -> Quad(
                            "Rest",
                            Color(0xFF9E98B3),
                            Color(0xFF1B1928),
                            Color(0xFF2D2940)
                        )
                        else -> Quad(
                            "${activity.estimatedMinutes} min",
                            Color(0xFFD5CFE8),
                            Color(0xFF221F30),
                            Color(0xFF36324A)
                        )
                    }

                    Card(
                        onClick = {
                            expandedActivityId = if (isExpanded) null else activity.id
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(
                            width = if (isToday || isExpanded) 1.5.dp else 1.dp,
                            color = when {
                                isToday -> accentColor
                                isExpanded -> accentColor.copy(alpha = 0.7f)
                                activity.status == ProgramActivityStatus.COMPLETED -> Color(0xFF00DC82).copy(alpha = 0.4f)
                                else -> Color(0xFF312C42)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ProgramCardGradient, RoundedCornerShape(18.dp))
                                .padding(15.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "Day ${activity.dayIndex} · $formattedDate · ${activity.phaseTitle}",
                                        color = if (isToday) accentColor else ProgramLavender,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = activity.title,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = badgeBgColor,
                                    border = BorderStroke(1.dp, badgeBorderColor)
                                ) {
                                    Text(
                                        text = badgeLabel,
                                        color = badgeTextColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    HorizontalDivider(color = Color(0xFF2D2940))
                                    Text(
                                        text = activity.instructions,
                                        color = Color(0xFFD5CFE8),
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 18.sp
                                    )
                                    if (activity.estimatedMinutes > 0) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                onClick = { activeGuidedActivity = activity },
                                                shape = RoundedCornerShape(10.dp),
                                                color = accentColor,
                                                border = BorderStroke(1.dp, accentColor)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.PlayArrow,
                                                        contentDescription = null,
                                                        tint = Color(0xFF130E1E),
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Text(
                                                        text = "Guided Timer",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF130E1E)
                                                    )
                                                }
                                            }

                                            Surface(
                                                onClick = { onActivityStatus(activity.id, ProgramActivityStatus.COMPLETED) },
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFF0D3325),
                                                border = BorderStroke(1.dp, Color(0xFF00DC82).copy(alpha = 0.45f))
                                            ) {
                                                Text(
                                                    text = "✓ Complete",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF00DC82),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                                )
                                            }

                                            Surface(
                                                onClick = { onActivityStatus(activity.id, ProgramActivityStatus.SKIPPED) },
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFF381418),
                                                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.45f))
                                            ) {
                                                Text(
                                                    text = "✕ Skip",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFF5252),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // 5. PROGRAM CONTROLS FOOTER
                // =========================================================================
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onAdjust,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color(0xFF140E21)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Recalibrate Schedule & Blueprint", fontWeight = FontWeight.ExtraBold)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onProgramStatus(
                                if (progress.enrollment.status == ProgramStatus.PAUSED) ProgramStatus.ACTIVE
                                else ProgramStatus.PAUSED
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (progress.enrollment.status == ProgramStatus.PAUSED) "Resume Program" else "Pause Program")
                    }
                    OutlinedButton(
                        onClick = { onProgramStatus(ProgramStatus.COMPLETED) },
                        modifier = Modifier.weight(1f),
                        enabled = progress.enrollment.status != ProgramStatus.COMPLETED,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Mark Finished")
                    }
                }
                TextButton(onClick = { showDelete = true }, Modifier.fillMaxWidth()) {
                    Text("End and remove program", color = Color(0xFFFF5252), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.padding(bottom = 16.dp))
            }
        }
    }

    // =========================================================================
    // INTERACTIVE GUIDED SESSION PLAYER BOTTOM SHEET
    // =========================================================================
    activeGuidedActivity?.let { guidedActivity ->
        GuidedSessionBottomSheet(
            activity = guidedActivity,
            programTitle = progress.enrollment.titleSnapshot,
            accentColor = accentColor,
            onDismiss = { activeGuidedActivity = null },
            onCompleteSession = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onActivityStatus(guidedActivity.id, ProgramActivityStatus.COMPLETED)
                activeGuidedActivity = null
            }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor = Color(0xFF1E1C2A),
            titleContentColor = Color.White,
            textContentColor = ProgramMuted,
            title = { Text("Remove this program?", fontWeight = FontWeight.Bold) },
            text = { Text("Your local program history and pending sync changes for this enrollment will be removed. This cannot be undone from the app.") },
            confirmButton = {
                TextButton(onClick = { showDelete = false; onDelete() }) {
                    Text("Remove", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text("Cancel", color = ProgramLavender)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuidedSessionBottomSheet(
    activity: ScheduledProgramActivity,
    programTitle: String,
    accentColor: Color,
    onDismiss: () -> Unit,
    onCompleteSession: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val totalSecondsInitial = (activity.estimatedMinutes.coerceAtLeast(1) * 60)
    var totalSeconds by remember(activity.id) { mutableIntStateOf(totalSecondsInitial) }
    var remainingSeconds by remember(activity.id) { mutableIntStateOf(totalSecondsInitial) }
    var isRunning by remember(activity.id) { mutableStateOf(true) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1_000L)
            remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)
        }
    }

    val progressFraction by animateFloatAsState(
        targetValue = if (totalSeconds > 0) {
            ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
        } else 1f,
        animationSpec = tween(300),
        label = "guidedTimerFraction"
    )

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val formattedClock = String.format(Locale.US, "%02d:%02d", mins, secs)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF151320),
        contentColor = Color.White,
        scrimColor = Color(0xCC000000),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = accentColor.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "DAY ${activity.dayIndex} · ${activity.phaseTitle.uppercase()} · $programTitle",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Text(
                text = activity.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            // Circular Guided Timer Ring
            Box(
                modifier = Modifier.size(196.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF252136),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxSize(),
                    color = if (remainingSeconds == 0) Color(0xFF00DC82) else accentColor,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (remainingSeconds == 0) "COMPLETE" else if (isRunning) "IN SESSION" else "PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (remainingSeconds == 0) Color(0xFF00DC82) else accentColor,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = formattedClock,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}% elapsed",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9E98B5)
                    )
                }
            }

            // Timer Playback Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        remainingSeconds = totalSecondsInitial
                        totalSeconds = totalSecondsInitial
                        isRunning = false
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF211E30),
                    border = BorderStroke(1.dp, Color(0xFF342F49))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset timer",
                            tint = Color(0xFFD5CFE8),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD5CFE8)
                        )
                    }
                }

                Surface(
                    onClick = { isRunning = !isRunning },
                    shape = RoundedCornerShape(16.dp),
                    color = accentColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Resume",
                            tint = Color(0xFF130E1E),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isRunning) "Pause" else "Resume",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF130E1E)
                        )
                    }
                }

                Surface(
                    onClick = {
                        remainingSeconds += 60
                        totalSeconds += 60
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF211E30),
                    border = BorderStroke(1.dp, Color(0xFF342F49))
                ) {
                    Text(
                        text = "+1 Min",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD5CFE8),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // Protocol Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1829)),
                border = BorderStroke(1.dp, Color(0xFF2E2942))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SESSION EXECUTION PROTOCOL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        letterSpacing = 0.9.sp
                    )
                    Text(
                        text = activity.instructions,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2DCF5),
                        lineHeight = 21.sp
                    )
                }
            }

            Button(
                onClick = onCompleteSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00DC82),
                    contentColor = Color(0xFF042416)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Complete & Log Today's Session",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = Color(0xFF00DC82),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Free Guided Session Player • Progress syncs automatically",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E8AA7)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
