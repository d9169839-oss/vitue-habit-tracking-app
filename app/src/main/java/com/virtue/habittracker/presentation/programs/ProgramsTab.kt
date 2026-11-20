package com.virtue.habittracker.presentation.programs

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.domain.model.program.ProgramCatalog
import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramProgress
import com.virtue.habittracker.domain.model.program.ProgramStatus
import com.virtue.habittracker.domain.model.program.WorkActivityLevel
import com.virtue.habittracker.domain.model.program.catalog.ProgramDefinition
import com.virtue.habittracker.presentation.SectionLoading
import java.time.LocalDate

private enum class ProgramRoute { CATALOG, DETAIL, PERSONALIZE, ACTIVE, ADJUST_PERSONALIZE }

data class ProgramFeatureTag(
    val label: String,
    val icon: ImageVector
)

data class PrimaryFocusProgram(
    val id: String,
    val title: String,
    val description: String,
    val category: ProgramCategory,
    val iconVector: ImageVector,
    val accentColor: Color,
    val isRecommended: Boolean = false,
    val tags: List<ProgramFeatureTag>,
    val templateId: String
)

private fun ProgramDefinition.toPrimaryFocusProgram(): PrimaryFocusProgram {
    val icon = when (template.id) {
        "home-strength-60" -> Icons.Filled.FitnessCenter
        "weight-habits-90" -> Icons.Filled.MonitorWeight
        "stamina-60" -> Icons.Filled.Bolt
        "movement-30", "fitness-wellness-60" -> Icons.Filled.SelfImprovement
        "mindful-30" -> Icons.Filled.Psychology
        "cbt-sleep-30" -> Icons.Filled.Bedtime
        "gratitude-30" -> Icons.Filled.Favorite
        "dopamine-detox-30", "personal-development-30" -> Icons.Filled.AutoAwesome
        "grooming-30", "skincare-30" -> Icons.Filled.Spa
        else -> Icons.Filled.SelfImprovement
    }
    val tagIcons = listOf(
        Icons.Filled.CalendarMonth,
        Icons.Filled.AutoAwesome,
        Icons.Filled.Schedule
    )
    return PrimaryFocusProgram(
        id = template.id,
        title = template.title,
        description = template.description,
        category = template.category,
        iconVector = icon,
        accentColor = Color(accentHex),
        isRecommended = isRecommended,
        tags = highlightTags.mapIndexed { index, label ->
            ProgramFeatureTag(label, tagIcons.getOrElse(index) { Icons.Filled.Schedule })
        },
        templateId = template.id
    )
}

val focusPrograms: List<PrimaryFocusProgram>
    get() = ProgramCatalog.definitions.map { it.toPrimaryFocusProgram() }

@Composable
fun ProgramsTab(
    modifier: Modifier,
    visible: Boolean,
    onFullScreenChange: (Boolean) -> Unit,
    vm: ProgramsViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var route by remember { mutableStateOf(ProgramRoute.CATALOG) }
    var selectedTemplateId by remember { mutableStateOf<String?>(null) }
    var selectedEnrollmentId by remember { mutableStateOf<String?>(null) }
    var draftPreferences by remember { mutableStateOf(ProgramPreferences()) }
    var draftStartEpochDay by remember { mutableStateOf(LocalDate.now().toEpochDay()) }

    val selectedTemplate = vm.catalog.firstOrNull { it.id == selectedTemplateId }
    val selectedProgress = state.programs.firstOrNull { it.enrollment.id == selectedEnrollmentId }

    val openPersonalizeOrActiveForTemplate: (String) -> Unit = { templateId ->
        // If user already has an ACTIVE or PAUSED enrollment for this template, open it directly!
        val existingEnrollment = state.programs.firstOrNull {
            it.enrollment.templateId == templateId &&
                (it.enrollment.status == ProgramStatus.ACTIVE || it.enrollment.status == ProgramStatus.PAUSED)
        }
        if (existingEnrollment != null) {
            selectedEnrollmentId = existingEnrollment.enrollment.id
            selectedTemplateId = templateId
            route = ProgramRoute.ACTIVE
        } else {
            selectedTemplateId = templateId
            val template = vm.catalog.firstOrNull { it.id == templateId }
            if (template != null) {
                draftPreferences = ProgramPreferences(
                    availableMinutesPerDay = template.defaultMinutesPerDay.coerceIn(5, 90),
                    availableDaysPerWeek = 5,
                    experience = ProgramExperience.BEGINNER,
                    workActivityLevel = WorkActivityLevel.MIXED,
                    equipment = template.equipment
                )
            }
            draftStartEpochDay = LocalDate.now().toEpochDay()
            route = ProgramRoute.PERSONALIZE
        }
    }

    // Keep app navigation visible while the preview is shown over the catalogue.
    LaunchedEffect(route) { onFullScreenChange(route != ProgramRoute.CATALOG && route != ProgramRoute.DETAIL) }

    BackHandler(enabled = route != ProgramRoute.CATALOG) {
        route = when (route) {
            ProgramRoute.DETAIL -> ProgramRoute.CATALOG
            ProgramRoute.PERSONALIZE -> ProgramRoute.CATALOG
            ProgramRoute.ACTIVE -> ProgramRoute.CATALOG
            ProgramRoute.ADJUST_PERSONALIZE -> ProgramRoute.ACTIVE
            ProgramRoute.CATALOG -> ProgramRoute.CATALOG
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF12111A))
            .then(if (visible) Modifier else Modifier.padding(start = 10_000.dp))
    ) {
        when (route) {
            ProgramRoute.CATALOG, ProgramRoute.DETAIL -> {
                PrimaryFocusProgramScreen(
                    state = state,
                    onSelectCategory = vm::selectCategory,
                    onOpenTemplate = openPersonalizeOrActiveForTemplate,
                    onPreviewTemplate = { templateId ->
                        selectedTemplateId = templateId
                        route = ProgramRoute.DETAIL
                    },
                    onOpenProgram = { progress ->
                        selectedEnrollmentId = progress.enrollment.id
                        selectedTemplateId = progress.enrollment.templateId
                        route = ProgramRoute.ACTIVE
                    }
                )
                if (route == ProgramRoute.DETAIL && selectedTemplate != null) {
                    val isAlreadyEnrolled = state.programs.any {
                        it.enrollment.templateId == selectedTemplate.id &&
                            (it.enrollment.status == ProgramStatus.ACTIVE || it.enrollment.status == ProgramStatus.PAUSED)
                    }
                    ProgramDetailScreen(
                        template = selectedTemplate,
                        isEnrolled = isAlreadyEnrolled,
                        onBack = { route = ProgramRoute.CATALOG },
                        onPersonalize = { openPersonalizeOrActiveForTemplate(selectedTemplate.id) }
                    )
                }
            }
            ProgramRoute.PERSONALIZE, ProgramRoute.ADJUST_PERSONALIZE -> selectedTemplate?.let { template ->
                ProgramPersonalizationScreen(
                    template = template,
                    initialPreferences = draftPreferences,
                    initialStartEpochDay = draftStartEpochDay,
                    isAdjusting = route == ProgramRoute.ADJUST_PERSONALIZE,
                    isSaving = state.isStarting,
                    onBack = {
                        route = if (route == ProgramRoute.ADJUST_PERSONALIZE) ProgramRoute.ACTIVE else ProgramRoute.CATALOG
                    },
                    onContinue = { preferences, startEpochDay, _ ->
                        draftPreferences = preferences
                        draftStartEpochDay = startEpochDay
                        if (route == ProgramRoute.ADJUST_PERSONALIZE && selectedEnrollmentId != null) {
                            vm.replanProgram(selectedEnrollmentId!!, preferences)
                            route = ProgramRoute.ACTIVE
                        } else {
                            vm.startProgram(template.id, startEpochDay, preferences)
                            selectedTemplateId = template.id
                            selectedEnrollmentId = null
                            route = ProgramRoute.ACTIVE
                        }
                    }
                )
            }
            ProgramRoute.ACTIVE -> {
                if (state.isInitialLoading || state.isStarting) {
                    SectionLoading(Modifier.fillMaxSize(), "Loading your program…")
                } else {
                    val progress = selectedProgress
                        ?: state.programs.firstOrNull {
                            if (selectedEnrollmentId != null) it.enrollment.id == selectedEnrollmentId
                            else it.enrollment.templateId == selectedTemplateId
                        }
                        ?: state.programs.firstOrNull()
                    if (progress != null) {
                        val template = vm.catalog.firstOrNull { it.id == progress.enrollment.templateId }
                        if (template == null) {
                            Text("This program is no longer available.", Modifier.padding(20.dp))
                        } else {
                            ActiveProgramScreen(
                                progress = progress,
                                onBack = { route = ProgramRoute.CATALOG },
                                onAdjust = {
                                    selectedEnrollmentId = progress.enrollment.id
                                    selectedTemplateId = template.id
                                    draftPreferences = progress.enrollment.preferences
                                    draftStartEpochDay = progress.enrollment.startEpochDay
                                    route = ProgramRoute.ADJUST_PERSONALIZE
                                },
                                onActivityStatus = vm::setActivityStatus,
                                onProgramStatus = { status -> vm.setEnrollmentStatus(progress.enrollment.id, status) },
                                onDelete = {
                                    vm.deleteEnrollment(progress.enrollment.id)
                                    selectedEnrollmentId = null
                                    route = ProgramRoute.CATALOG
                                }
                            )
                        }
                    } else {
                        Text("This program is no longer available.", Modifier.padding(20.dp))
                    }
                }
            }
        }
    }

    state.message?.let { message ->
        AlertDialog(
            onDismissRequest = vm::clearMessage,
            title = { Text("Programs") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = vm::clearMessage) { Text("OK") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrimaryFocusProgramScreen(
    state: ProgramsUiState,
    onSelectCategory: (ProgramCategory?) -> Unit,
    onOpenTemplate: (String) -> Unit,
    onPreviewTemplate: (String) -> Unit,
    onOpenProgram: (ProgramProgress) -> Unit
) {
    val activeEnrollments = remember(state.programs) {
        state.programs.filter {
            it.enrollment.status == ProgramStatus.ACTIVE || it.enrollment.status == ProgramStatus.PAUSED
        }.ifEmpty { state.programs }
    }
    var selectedActiveIndex by remember(activeEnrollments.size) { mutableIntStateOf(0) }
    val activeEnrollment = activeEnrollments.getOrNull(
        selectedActiveIndex.coerceIn(0, (activeEnrollments.size - 1).coerceAtLeast(0))
    )

    val enrolledByTemplateId = remember(state.programs) {
        state.programs
            .filter { it.enrollment.status == ProgramStatus.ACTIVE || it.enrollment.status == ProgramStatus.PAUSED }
            .associateBy { it.enrollment.templateId }
    }

    val visiblePrograms = remember(state.selectedCategory) {
        val allPrograms = focusPrograms
        val category = state.selectedCategory
        if (category == null) allPrograms else allPrograms.filter { it.category == category }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF12111A))
    ) {
        val isCompactWidth = maxWidth < 360.dp
        val horizontalPad = if (isCompactWidth) 14.dp else 20.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPad, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Header Row with Eyebrow on left and interactive "Progress" pill on top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PERIODIZED TRAINING",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8E8AA7),
                    letterSpacing = 1.4.sp
                )

                Surface(
                    onClick = { activeEnrollment?.let(onOpenProgram) },
                    enabled = activeEnrollment != null,
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF1E1C2B),
                    border = BorderStroke(
                        1.dp,
                        if (activeEnrollment != null) Color(0xFF00DC82).copy(alpha = 0.5f)
                        else Color(0xFFA78BFA).copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (activeEnrollment != null) Color(0xFF00DC82) else Color(0xFFA78BFA),
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (activeEnrollments.isNotEmpty()) "Progress (${activeEnrollments.size})" else "Progress",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (activeEnrollment != null) Color(0xFF00DC82) else Color(0xFFC4B5FD),
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // Multi-Enrollment Switcher Pills (when user has >1 active program, e.g., Fitness + Sleep)
            if (activeEnrollments.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeEnrollments.forEachIndexed { idx, enrollmentProgress ->
                        val isSelected = idx == selectedActiveIndex
                        Surface(
                            onClick = { selectedActiveIndex = idx },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) Color(0xFF0D3325) else Color(0xFF1C1A28),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF00DC82) else Color(0xFF322E45)
                            )
                        ) {
                            Text(
                                text = "${enrollmentProgress.enrollment.titleSnapshot} • ${enrollmentProgress.progressPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF00DC82) else Color(0xFFB5AFC7),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Ongoing Program Summary Hero Card
            if (activeEnrollment != null) {
                val animatedPercent by animateFloatAsState(
                    targetValue = (activeEnrollment.progressPercent / 100f).coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    label = "ongoingProgramProgress"
                )
                val todaySession = activeEnrollment.todayActivities.firstOrNull()
                Card(
                    onClick = { onOpenProgram(activeEnrollment) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.5.dp, Color(0xFFA78BFA))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = ProgramCardGradient,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF0D3325),
                                border = BorderStroke(1.dp, Color(0xFF00DC82).copy(alpha = 0.45f))
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
                                        text = "ACTIVE ENROLLMENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00DC82),
                                        fontSize = 10.sp,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }

                            Text(
                                text = "${activeEnrollment.progressPercent}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF00DC82),
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        Text(
                            text = activeEnrollment.enrollment.titleSnapshot,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = Color.White
                        )

                        if (todaySession != null) {
                            Text(
                                text = "Today: ${todaySession.title}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD5CFE8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        LinearProgressIndicator(
                            progress = { animatedPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(50)),
                            color = Color(0xFF00DC82),
                            trackColor = Color(0xFF171522)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeEnrollment.completedCount}/${activeEnrollment.totalCount} sessions completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB5AFC7)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Open dashboard",
                                    color = Color(0xFFC4B5FD),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFFC4B5FD),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Headline & Subtitle
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "What's your primary focus?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White
                )
                Text(
                    text = "Choose a periodized physical, CBT mind & sleep, or dopamine & focus program. Every program is 100% free with its own dedicated clinical curriculum.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF9E8AA7),
                    lineHeight = 20.sp
                )
            }

            // Category Filter Pills
            val categories: List<Pair<ProgramCategory?, String>> = listOf(
                null to "All (${focusPrograms.size})",
                ProgramCategory.FITNESS to "Fitness",
                ProgramCategory.MINDFULNESS to "Mind & Sleep (CBT)",
                ProgramCategory.PERSONAL_DEVELOPMENT to "Focus & Dopamine",
                ProgramCategory.SELF_GROOMING to "Grooming & Skin"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (category, label) ->
                    val isSelected = state.selectedCategory == category
                    Surface(
                        onClick = { onSelectCategory(category) },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) Color(0xFFA78BFA) else Color(0xFF1E1C2B),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFA78BFA) else Color(0xFF322E45)
                        )
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF171020) else Color(0xFFC4B5FD),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Focus Program Cards List
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                visiblePrograms.forEach { program ->
                    val enrolledProgress = enrolledByTemplateId[program.templateId]
                    FocusProgramCard(
                        program = program,
                        enrolledProgress = enrolledProgress,
                        onClick = { onOpenTemplate(program.templateId) },
                        onPreviewClick = { onPreviewTemplate(program.templateId) }
                    )
                }
            }

            // Subtle trust badge footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = Color(0xFF00DC82),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "100% Free Forever • Adaptive periodization",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF8E8AA7),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FocusProgramCard(
    program: PrimaryFocusProgram,
    enrolledProgress: ProgramProgress?,
    onClick: () -> Unit,
    onPreviewClick: () -> Unit
) {
    val isEnrolled = enrolledProgress != null
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = if (isEnrolled || program.isRecommended) 1.5.dp else 1.dp,
            color = when {
                isEnrolled -> Color(0xFF00DC82)
                program.isRecommended -> program.accentColor
                else -> Color(0xFF383245)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = ProgramCardGradient,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            // Top Row: Radial-Glow Vector Icon + Status/Recommended Badge + Preview / Action Affordances
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        program.accentColor.copy(alpha = 0.28f),
                                        Color(0xFF252036)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = program.iconVector,
                            contentDescription = null,
                            tint = program.accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    if (enrolledProgress != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF0D3325),
                            border = BorderStroke(1.dp, Color(0xFF00DC82).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF00DC82),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "ACTIVE • ${enrolledProgress.progressPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00DC82),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    } else if (program.isRecommended) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = program.accentColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, program.accentColor.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "RECOMMENDED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 0.7.sp,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF1F1C2E),
                            border = BorderStroke(1.dp, Color(0xFF332E47))
                        ) {
                            Text(
                                text = "FREE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00DC82),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF242132),
                        border = BorderStroke(1.dp, Color(0xFF3A344E)),
                        modifier = Modifier.clickable(onClick = onPreviewClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Preview syllabus",
                                tint = Color(0xFFB5AFC7),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Syllabus",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB5AFC7)
                            )
                        }
                    }

                    // Action Arrow Affordance
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (isEnrolled) Color(0xFF0D3325) else Color(0xFF2E283E),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = if (isEnrolled) "Open active program" else "Personalize program",
                            tint = if (isEnrolled) Color(0xFF00DC82) else Color(0xFFC4B5FD),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Title
            Text(
                text = program.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )

            // Description
            Text(
                text = program.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF9E8AA7),
                lineHeight = 20.sp
            )

            // Vector-Icon Tags Row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                program.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF221F30),
                        border = BorderStroke(1.dp, Color(0xFF312C44))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = tag.icon,
                                contentDescription = null,
                                tint = program.accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = tag.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFFD7CEFC)
                            )
                        }
                    }
                }
            }
        }
    }
}
