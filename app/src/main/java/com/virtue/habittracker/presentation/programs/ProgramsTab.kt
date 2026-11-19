package com.virtue.habittracker.presentation.programs

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramProgress
import com.virtue.habittracker.domain.model.program.WorkActivityLevel
import com.virtue.habittracker.presentation.SectionLoading
import java.time.LocalDate

private enum class ProgramRoute { CATALOG, DETAIL, PERSONALIZE, REVIEW, ACTIVE, ADJUST_PERSONALIZE, ADJUST_REVIEW }

data class ProgramFeatureTag(
    val label: String,
    val icon: ImageVector
)

data class PrimaryFocusProgram(
    val id: String,
    val title: String,
    val description: String,
    val iconVector: ImageVector,
    val accentColor: Color,
    val isRecommended: Boolean = false,
    val tags: List<ProgramFeatureTag>,
    val templateId: String
)

val focusPrograms = listOf(
    PrimaryFocusProgram(
        id = "build-lean-muscle",
        title = "Build Lean Muscle",
        description = "Progressive overload focused on hypertrophic adaptation, structural density, and functional power.",
        iconVector = Icons.Filled.FitnessCenter,
        accentColor = Color(0xFFA78BFA),
        isRecommended = true,
        tags = listOf(
            ProgramFeatureTag("4–5 days/wk", Icons.Filled.CalendarMonth),
            ProgramFeatureTag("Hypertrophic Macros", Icons.Filled.Restaurant),
            ProgramFeatureTag("3–6 months", Icons.Filled.Schedule)
        ),
        templateId = "home-strength-60"
    ),
    PrimaryFocusProgram(
        id = "calm-recomposition",
        title = "Calm Recomposition",
        description = "Simultaneous fat loss and muscle retention with precise caloric partitioning and steady, controlled volume.",
        iconVector = Icons.Filled.MonitorWeight,
        accentColor = Color(0xFF00DC82),
        isRecommended = false,
        tags = listOf(
            ProgramFeatureTag("3–4 days/wk", Icons.Filled.CalendarMonth),
            ProgramFeatureTag("High-Protein Fueling", Icons.Filled.Restaurant),
            ProgramFeatureTag("16–24 weeks", Icons.Filled.Schedule)
        ),
        templateId = "weight-habits-90"
    ),
    PrimaryFocusProgram(
        id = "aerobic-engine",
        title = "Aerobic Engine & Vitality",
        description = "Cardiovascular resilience, metabolic conditioning, and sustained baseline energy for demanding workdays.",
        iconVector = Icons.Filled.Bolt,
        accentColor = Color(0xFFFBBF24),
        isRecommended = false,
        tags = listOf(
            ProgramFeatureTag("3–5 days/wk", Icons.Filled.CalendarMonth),
            ProgramFeatureTag("Balanced Glycogen", Icons.Filled.Bolt),
            ProgramFeatureTag("60-day cycle", Icons.Filled.Schedule)
        ),
        templateId = "stamina-60"
    ),
    PrimaryFocusProgram(
        id = "longevity-movement",
        title = "Longevity & Movement Health",
        description = "Joint articulation, thoracic mobility, active tissue recovery, and sustainable low-fatigue functional movement.",
        iconVector = Icons.Filled.SelfImprovement,
        accentColor = Color(0xFF38BDF8),
        isRecommended = false,
        tags = listOf(
            ProgramFeatureTag("3–4 days/wk", Icons.Filled.CalendarMonth),
            ProgramFeatureTag("Maintenance & Vitality", Icons.Filled.SelfImprovement),
            ProgramFeatureTag("30-day cycle", Icons.Filled.Schedule)
        ),
        templateId = "movement-30"
    )
)

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

    val openPersonalizeForTemplate: (String) -> Unit = { templateId ->
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

    // Keep app navigation visible while the preview is shown over the catalogue.
    LaunchedEffect(route) { onFullScreenChange(route != ProgramRoute.CATALOG && route != ProgramRoute.DETAIL) }

    BackHandler(enabled = route != ProgramRoute.CATALOG) {
        route = when (route) {
            ProgramRoute.DETAIL -> ProgramRoute.CATALOG
            ProgramRoute.PERSONALIZE -> ProgramRoute.CATALOG
            ProgramRoute.REVIEW -> ProgramRoute.PERSONALIZE
            ProgramRoute.ACTIVE -> ProgramRoute.CATALOG
            ProgramRoute.ADJUST_PERSONALIZE -> ProgramRoute.ACTIVE
            ProgramRoute.ADJUST_REVIEW -> ProgramRoute.ADJUST_PERSONALIZE
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
                    onOpenTemplate = openPersonalizeForTemplate,
                    onPreviewTemplate = { templateId ->
                        selectedTemplateId = templateId
                        route = ProgramRoute.DETAIL
                    },
                    onOpenProgram = { progress ->
                        selectedEnrollmentId = progress.enrollment.id
                        route = ProgramRoute.ACTIVE
                    }
                )
                if (route == ProgramRoute.DETAIL && selectedTemplate != null) {
                    ProgramDetailScreen(
                        template = selectedTemplate,
                        onBack = { route = ProgramRoute.CATALOG },
                        onPersonalize = { openPersonalizeForTemplate(selectedTemplate.id) }
                    )
                }
            }
            ProgramRoute.PERSONALIZE, ProgramRoute.ADJUST_PERSONALIZE -> selectedTemplate?.let { template ->
                ProgramPersonalizationScreen(
                    template = template,
                    initialPreferences = draftPreferences,
                    initialStartEpochDay = draftStartEpochDay,
                    isAdjusting = route == ProgramRoute.ADJUST_PERSONALIZE,
                    onBack = {
                        route = if (route == ProgramRoute.ADJUST_PERSONALIZE) ProgramRoute.ACTIVE else ProgramRoute.CATALOG
                    },
                    onContinue = { preferences, startEpochDay, _ ->
                        draftPreferences = preferences
                        draftStartEpochDay = startEpochDay
                        route = if (route == ProgramRoute.ADJUST_PERSONALIZE) ProgramRoute.ADJUST_REVIEW else ProgramRoute.REVIEW
                    }
                )
            }
            ProgramRoute.REVIEW, ProgramRoute.ADJUST_REVIEW -> selectedTemplate?.let { template ->
                val generatedPreview = vm.previewSchedule(template, draftStartEpochDay, draftPreferences)
                val preview = if (route == ProgramRoute.ADJUST_REVIEW) {
                    generatedPreview.filter { it.epochDay >= LocalDate.now().toEpochDay() }.take(7)
                } else generatedPreview.take(7)
                ProgramReviewScreen(
                    template = template,
                    preferences = draftPreferences,
                    startEpochDay = draftStartEpochDay,
                    previewActivities = preview,
                    isAdjusting = route == ProgramRoute.ADJUST_REVIEW,
                    isSaving = state.isStarting,
                    onBack = {
                        route = if (route == ProgramRoute.ADJUST_REVIEW) ProgramRoute.ADJUST_PERSONALIZE else ProgramRoute.PERSONALIZE
                    },
                    onConfirm = {
                        if (route == ProgramRoute.ADJUST_REVIEW && selectedEnrollmentId != null) {
                            vm.replanProgram(selectedEnrollmentId!!, draftPreferences)
                            route = ProgramRoute.ACTIVE
                        } else {
                            vm.startProgram(template.id, draftStartEpochDay, draftPreferences)
                            selectedTemplateId = template.id
                            route = ProgramRoute.ACTIVE
                        }
                    }
                )
            }
            ProgramRoute.ACTIVE -> {
                if (state.isInitialLoading) {
                    SectionLoading(Modifier.fillMaxSize(), "Loading your program…")
                } else {
                    val progress = selectedProgress
                        ?: state.programs.firstOrNull { if (selectedEnrollmentId != null) it.enrollment.id == selectedEnrollmentId else it.enrollment.templateId == selectedTemplateId }
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
    onOpenTemplate: (String) -> Unit,
    onPreviewTemplate: (String) -> Unit,
    onOpenProgram: (ProgramProgress) -> Unit
) {
    val activeEnrollment = state.programs.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF12111A))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header Row with Eyebrow on left and "Progress" pill on top right
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
                shape = RoundedCornerShape(50),
                color = Color(0xFF1E1C2B),
                border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFA78BFA), CircleShape)
                    )
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC4B5FD),
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
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
                                text = "Open schedule",
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
                text = "Choose the foundation for your 3 to 6 month tailored progression. You can adjust this anytime without losing data.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF9E8AA7),
                lineHeight = 20.sp
            )
        }

        // Focus Program Cards List
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            focusPrograms.forEach { program ->
                FocusProgramCard(
                    program = program,
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
                tint = Color(0xFFA78BFA),
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Adaptive periodization • Auto-syncs to your rhythm",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF8E8AA7),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FocusProgramCard(
    program: PrimaryFocusProgram,
    onClick: () -> Unit,
    onPreviewClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = if (program.isRecommended) 1.5.dp else 1.dp,
            color = if (program.isRecommended) Color(0xFFA78BFA) else Color(0xFF383245)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = ProgramCardGradient,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Radial-Glow Vector Icon + Recommended Badge + Preview / Action Affordances
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
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

                    if (program.isRecommended) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF392B58),
                            border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "RECOMMENDED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC4B5FD),
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 0.7.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
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
                                contentDescription = "Preview phases",
                                tint = Color(0xFFB5AFC7),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Phases",
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
                            .background(Color(0xFF2E283E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Personalize program",
                            tint = Color(0xFFC4B5FD),
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
