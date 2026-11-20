package com.virtue.habittracker.presentation.programs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtue.habittracker.domain.model.program.BiologicalSex
import com.virtue.habittracker.domain.model.program.Biometrics
import com.virtue.habittracker.domain.model.program.DailyOccupation
import com.virtue.habittracker.domain.model.program.ProgramCatalog
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.ScheduledProgramActivity
import com.virtue.habittracker.domain.model.program.WorkActivityLevel
import com.virtue.habittracker.domain.model.program.catalog.AssessmentOptionSpec
import com.virtue.habittracker.domain.model.program.catalog.BuildLeanMuscleProgram
import com.virtue.habittracker.domain.model.program.catalog.ProgramDiagnosticSummary
import com.virtue.habittracker.domain.model.program.catalog.ProgramOnboardingSpec
import com.virtue.habittracker.domain.service.ProgramPersonalizationEngine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val StepSectionTitles = listOf("Baseline", "Focus", "Cadence", "Blueprint")

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProgramPersonalizationScreen(
    template: ProgramTemplate,
    initialPreferences: ProgramPreferences,
    initialStartEpochDay: Long,
    isAdjusting: Boolean,
    isSaving: Boolean = false,
    onBack: () -> Unit,
    onContinue: (ProgramPreferences, Long, Boolean) -> Unit
) {
    val definition = remember(template.id) {
        ProgramCatalog.findDefinition(template.id) ?: BuildLeanMuscleProgram
    }
    val onboarding = definition.onboarding
    val accentColor = Color(definition.accentHex)
    val previewEngine = remember { ProgramPersonalizationEngine() }

    var currentStep by remember(template.id) { mutableIntStateOf(1) }
    val haptics = LocalHapticFeedback.current

    // Biometrics state (used when onboarding.usesBiometricPhysicalStep1 == true)
    var sex by remember(initialPreferences, template.id) { mutableStateOf(initialPreferences.biometrics.sex) }
    var age by remember(initialPreferences, template.id) { mutableIntStateOf(initialPreferences.biometrics.age) }
    var heightCm by remember(initialPreferences, template.id) { mutableStateOf(initialPreferences.biometrics.heightCm) }
    var weightKg by remember(initialPreferences, template.id) { mutableStateOf(initialPreferences.biometrics.weightKg) }

    // Bespoke Step 1 & Step 2 option indices for this program (persisted in ProgramPreferences)
    var step1PrimaryIndex by remember(initialPreferences, template.id) {
        mutableIntStateOf(
            initialPreferences.step1PrimaryIndex.coerceIn(
                0,
                (onboarding.step1PrimaryOptions.size - 1).coerceAtLeast(0)
            )
        )
    }
    var step1SecondaryIndex by remember(initialPreferences, template.id) {
        mutableIntStateOf(
            initialPreferences.step1SecondaryIndex.coerceIn(
                0,
                (onboarding.step1SecondaryOptions.size - 1).coerceAtLeast(0)
            )
        )
    }
    var step2PrimaryIndex by remember(initialPreferences, template.id) {
        val maxIdx = (onboarding.step2PrimaryOptions.size - 1).coerceAtLeast(0)
        val savedIdx = initialPreferences.step2PrimaryIndex.coerceIn(0, maxIdx)
        val mappedIdx = onboarding.step2PrimaryOptions.indexOfFirst { it.mappedExperience == initialPreferences.experience }
        mutableIntStateOf(if (savedIdx != 0 || mappedIdx < 0) savedIdx else mappedIdx)
    }
    var step2SecondaryIndex by remember(initialPreferences, template.id) {
        val maxIdx = (onboarding.step2SecondaryOptions.size - 1).coerceAtLeast(0)
        val savedIdx = initialPreferences.step2SecondaryIndex.coerceIn(0, maxIdx)
        val mappedIdx = onboarding.step2SecondaryOptions.indexOfFirst { it.mappedWorkLevel == initialPreferences.workActivityLevel }
        mutableIntStateOf(if (savedIdx != 0 || mappedIdx < 0) savedIdx else mappedIdx)
    }

    // Core scheduling preferences updated dynamically by the program's bespoke options
    var experience by remember(initialPreferences, template.id) { mutableStateOf(initialPreferences.experience) }
    var workActivityLevel by remember(initialPreferences, template.id) { mutableStateOf(initialPreferences.workActivityLevel) }
    var minutes by remember(initialPreferences, template.id) {
        val defaultMin = if (initialPreferences.availableMinutesPerDay in onboarding.step3MinuteChoices) {
            initialPreferences.availableMinutesPerDay
        } else {
            template.defaultMinutesPerDay
        }
        mutableIntStateOf(defaultMin)
    }
    var days by remember(initialPreferences, template.id) {
        val defaultDays = if (initialPreferences.availableDaysPerWeek in onboarding.step3DayChoices) {
            initialPreferences.availableDaysPerWeek
        } else {
            onboarding.step3DayChoices.getOrElse(1) { 5 }
        }
        mutableIntStateOf(defaultDays)
    }
    var equipment by remember(initialPreferences, template.id) { mutableStateOf(initialPreferences.equipment) }
    var startEpochDay by remember(initialStartEpochDay, template.id) { mutableStateOf(initialStartEpochDay) }
    var showDatePicker by remember { mutableStateOf(false) }

    val occupation = when (workActivityLevel) {
        WorkActivityLevel.MOSTLY_SEATED -> DailyOccupation.DESK
        WorkActivityLevel.MIXED -> DailyOccupation.LIGHT
        WorkActivityLevel.PHYSICALLY_ACTIVE -> DailyOccupation.PHYSICAL
    }
    val biometrics = Biometrics(sex, age, heightCm, weightKg, occupation)

    val currentPreferences = ProgramPreferences(
        biometrics = biometrics,
        availableMinutesPerDay = minutes,
        availableDaysPerWeek = days,
        experience = experience,
        workActivityLevel = workActivityLevel,
        equipment = equipment,
        step1PrimaryIndex = step1PrimaryIndex,
        step1SecondaryIndex = step1SecondaryIndex,
        step2PrimaryIndex = step2PrimaryIndex,
        step2SecondaryIndex = step2SecondaryIndex
    )

    val applyOptionMappings: (AssessmentOptionSpec) -> Unit = { spec ->
        spec.mappedExperience?.let { experience = it }
        spec.mappedWorkLevel?.let { workActivityLevel = it }
        spec.mappedEquipment?.let { equipment = it }
    }

    val diagnosticSummary = remember(
        definition,
        currentPreferences,
        step1PrimaryIndex,
        step1SecondaryIndex,
        step2PrimaryIndex,
        step2SecondaryIndex
    ) {
        definition.buildDiagnosticSummary(
            preferences = currentPreferences,
            step1PrimaryIndex = step1PrimaryIndex,
            step1SecondaryIndex = step1SecondaryIndex,
            step2PrimaryIndex = step2PrimaryIndex,
            step2SecondaryIndex = step2SecondaryIndex
        )
    }

    val previewActivities = remember(template, startEpochDay, currentPreferences, isAdjusting) {
        val generated = previewEngine.buildSchedule(
            template = template,
            enrollmentId = "preview-${template.id}",
            startEpochDay = startEpochDay,
            preferences = currentPreferences
        )
        if (isAdjusting) {
            generated.filter { it.epochDay >= LocalDate.now().toEpochDay() }.take(7)
        } else {
            generated.take(7)
        }
    }

    val ctaLabel = when (currentStep) {
        1 -> "Continue to ${StepSectionTitles[1]}"
        2 -> "Continue to ${StepSectionTitles[2]}"
        3 -> "Generate My Blueprint"
        else -> if (isAdjusting) "Apply Recalibrated Schedule" else "Start Free ${template.durationDays}-Day Program"
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.11f),
                        Color(0xFF12111A),
                        Color(0xFF100E18)
                    )
                )
            )
    ) {
        val isCompactWidth = maxWidth < 360.dp
        val isExpandedWidth = maxWidth >= 600.dp
        val horizontalPadding: Dp = when {
            isCompactWidth -> 14.dp
            isExpandedWidth -> 28.dp
            else -> 20.dp
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.TopCenter)
        ) {
            // =========================================================================
            // 1. PINNED LUXURY TOP HEADER + INTERACTIVE 4-STAGE STEPPER
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = if (isCompactWidth) 12.dp else 16.dp,
                        bottom = 12.dp
                    ),
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
                        Surface(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (currentStep > 1) currentStep-- else onBack()
                            },
                            shape = CircleShape,
                            color = Color(0xFF1E1B2B),
                            border = BorderStroke(1.dp, Color(0xFF332E47)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAdjusting) "RECALIBRATE PROTOCOL" else "PERSONALIZED CALIBRATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                letterSpacing = 1.1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = template.title,
                                style = if (isCompactWidth) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF0D3325),
                        border = BorderStroke(1.dp, Color(0xFF00DC82).copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF00DC82), CircleShape)
                            )
                            Text(
                                text = "100% FREE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00DC82),
                                fontSize = 10.sp,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }
                }

                // Interactive 4-Stage Pill Stepper (tap any visited step to jump back)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompactWidth) 6.dp else 8.dp)
                ) {
                    StepSectionTitles.forEachIndexed { idx, label ->
                        val stepNumber = idx + 1
                        val isCurrent = currentStep == stepNumber
                        val isCompleted = currentStep > stepNumber
                        val bgColor by animateColorAsState(
                            targetValue = when {
                                isCurrent -> accentColor.copy(alpha = 0.22f)
                                isCompleted -> Color(0xFF1A2A27)
                                else -> Color(0xFF191724)
                            },
                            animationSpec = tween(250),
                            label = "stepperBg"
                        )
                        val borderColor by animateColorAsState(
                            targetValue = when {
                                isCurrent -> accentColor
                                isCompleted -> Color(0xFF00DC82).copy(alpha = 0.55f)
                                else -> Color(0xFF2B273C)
                            },
                            animationSpec = tween(250),
                            label = "stepperBorder"
                        )

                        Surface(
                            onClick = {
                                if (stepNumber < currentStep) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentStep = stepNumber
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor,
                            border = BorderStroke(if (isCurrent) 1.5.dp else 1.dp, borderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = if (isCompactWidth) 6.dp else 8.dp,
                                    vertical = 7.dp
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = if (isCompleted) "✓ STEP $stepNumber" else "STEP $stepNumber",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp,
                                    color = when {
                                        isCurrent -> accentColor
                                        isCompleted -> Color(0xFF00DC82)
                                        else -> Color(0xFF7B7693)
                                    }
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = if (isCompactWidth) 10.sp else 11.sp,
                                    color = if (isCurrent || isCompleted) Color.White else Color(0xFF8E8AA7),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF252236), thickness = 1.dp)

            // =========================================================================
            // 2. SCROLLABLE STEP BODY (RESPONSIVE FOR COMPACT, STANDARD, & TABLET)
            // =========================================================================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 18.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { width -> width / 3 } + fadeIn(tween(260)))
                                .togetherWith(slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { width -> -width / 3 } + fadeOut(tween(200)))
                        } else {
                            (slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { width -> -width / 3 } + fadeIn(tween(260)))
                                .togetherWith(slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { width -> width / 3 } + fadeOut(tween(200)))
                        }
                    },
                    label = "calibrationStepTransition"
                ) { step ->
                    when (step) {
                        1 -> if (onboarding.usesBiometricPhysicalStep1) {
                            Step1BiometricAssessment(
                                onboarding = onboarding,
                                accentColor = accentColor,
                                isCompactWidth = isCompactWidth,
                                sex = sex,
                                onSexChange = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    sex = it
                                },
                                age = age,
                                onAgeChange = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    age = it
                                },
                                heightCm = heightCm,
                                onHeightChange = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    heightCm = it
                                },
                                weightKg = weightKg,
                                onWeightChange = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    weightKg = it
                                }
                            )
                        } else {
                            StepBespokeAssessmentQuestions(
                                title = onboarding.step1Title,
                                subtitle = onboarding.step1Subtitle,
                                accentColor = accentColor,
                                isCompactWidth = isCompactWidth,
                                primaryLabel = onboarding.step1PrimaryQuestionLabel,
                                primaryBadge = null,
                                primaryOptions = onboarding.step1PrimaryOptions,
                                selectedPrimaryIndex = step1PrimaryIndex,
                                onSelectPrimary = { index, spec ->
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    step1PrimaryIndex = index
                                    applyOptionMappings(spec)
                                },
                                secondaryLabel = onboarding.step1SecondaryQuestionLabel,
                                secondaryBadge = null,
                                secondaryOptions = onboarding.step1SecondaryOptions,
                                selectedSecondaryIndex = step1SecondaryIndex,
                                onSelectSecondary = { index, spec ->
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    step1SecondaryIndex = index
                                    applyOptionMappings(spec)
                                },
                                insightNote = onboarding.step1InsightNote
                            )
                        }
                        2 -> StepBespokeAssessmentQuestions(
                            title = onboarding.step2Title,
                            subtitle = onboarding.step2Subtitle,
                            accentColor = accentColor,
                            isCompactWidth = isCompactWidth,
                            primaryLabel = onboarding.step2PrimaryQuestionLabel,
                            primaryBadge = onboarding.step2PrimaryBadge,
                            primaryOptions = onboarding.step2PrimaryOptions,
                            selectedPrimaryIndex = step2PrimaryIndex,
                            onSelectPrimary = { index, spec ->
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                step2PrimaryIndex = index
                                applyOptionMappings(spec)
                            },
                            secondaryLabel = onboarding.step2SecondaryQuestionLabel,
                            secondaryBadge = onboarding.step2SecondaryBadge,
                            secondaryOptions = onboarding.step2SecondaryOptions,
                            selectedSecondaryIndex = step2SecondaryIndex,
                            onSelectSecondary = { index, spec ->
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                step2SecondaryIndex = index
                                applyOptionMappings(spec)
                            },
                            insightNote = onboarding.step2InsightNote
                        )
                        3 -> Step3BespokeScheduleAndSetup(
                            onboarding = onboarding,
                            accentColor = accentColor,
                            isCompactWidth = isCompactWidth,
                            minutes = minutes,
                            onMinutesChange = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                minutes = it
                            },
                            days = days,
                            onDaysChange = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                days = it
                            },
                            equipment = equipment,
                            onEquipmentChange = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                equipment = it
                            },
                            startEpochDay = startEpochDay,
                            onOpenDatePicker = { showDatePicker = true }
                        )
                        4 -> Step4BespokeDiagnosticReview(
                            template = template,
                            summary = diagnosticSummary,
                            accentColor = accentColor,
                            isCompactWidth = isCompactWidth,
                            previewActivities = previewActivities,
                            startEpochDay = startEpochDay,
                            isAdjusting = isAdjusting
                        )
                    }
                }
            }

            // =========================================================================
            // 3. PINNED BOTTOM ACTION DOCK (NEVER LOST BELOW THE FOLD ON SMALL PHONES)
            // =========================================================================
            Surface(
                color = Color(0xFF151320),
                border = BorderStroke(1.dp, Color(0xFF28243A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = horizontalPadding, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isFinalStep = currentStep == 4
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (currentStep < 4) {
                                currentStep++
                            } else {
                                onContinue(currentPreferences, startEpochDay, true)
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFinalStep) Color(0xFF00DC82) else accentColor,
                            contentColor = if (isFinalStep) Color(0xFF042416) else Color(0xFF140E21)
                        )
                    ) {
                        if (isSaving) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color(0xFF042416),
                                    strokeWidth = 2.dp
                                )
                                Text("Building Your Schedule…", fontWeight = FontWeight.ExtraBold)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = ctaLabel,
                                    fontSize = if (isCompactWidth) 14.sp else 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = if (isFinalStep) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF00DC82),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (isFinalStep) {
                                "100% Free Forever • Full ${template.durationDays}-Day Curriculum Included"
                            } else {
                                "Step $currentStep of 4 • Adaptive clinical & performance engine"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9E98B5),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.ofEpochDay(startEpochDay).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            startEpochDay = maxOf(LocalDate.now().toEpochDay(), selected.toEpochDay())
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select Date", fontWeight = FontWeight.Bold, color = accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color(0xFF8E8AA7)) }
            }
        ) { DatePicker(state = pickerState) }
    }
}

// ==================================================
// STEP 1 (A): BIOMETRIC ASSESSMENT (HYPERTROPHY / RECOMP)
// ==================================================
@Composable
private fun Step1BiometricAssessment(
    onboarding: ProgramOnboardingSpec,
    accentColor: Color,
    isCompactWidth: Boolean,
    sex: BiologicalSex,
    onSexChange: (BiologicalSex) -> Unit,
    age: Int,
    onAgeChange: (Int) -> Unit,
    heightCm: Double,
    onHeightChange: (Double) -> Unit,
    weightKg: Double,
    onWeightChange: (Double) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = onboarding.step1Title,
                style = if (isCompactWidth) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
            Text(
                text = onboarding.step1Subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFFA8A2BE),
                lineHeight = 20.sp
            )
        }

        SectionHeaderPill(label = onboarding.step1PrimaryQuestionLabel, badge = "Mifflin-St Jeor", accentColor = accentColor)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SexOptionCard(
                title = "Male",
                subtitle = "Metabolic baseline",
                icon = Icons.Filled.Male,
                accentColor = accentColor,
                isSelected = sex == BiologicalSex.MALE,
                onClick = { onSexChange(BiologicalSex.MALE) },
                modifier = Modifier.weight(1f)
            )

            SexOptionCard(
                title = "Female",
                subtitle = "Metabolic baseline",
                icon = Icons.Filled.Female,
                accentColor = accentColor,
                isSelected = sex == BiologicalSex.FEMALE,
                onClick = { onSexChange(BiologicalSex.FEMALE) },
                modifier = Modifier.weight(1f)
            )
        }

        SectionHeaderPill(label = onboarding.step1SecondaryQuestionLabel, badge = "Tap − / +", accentColor = accentColor)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DimensionStepperCard(
                label = "Age",
                subtitle = "Years old",
                valueText = age.toString(),
                unitText = "yrs",
                accentColor = accentColor,
                onDecrement = { if (age > 14) onAgeChange(age - 1) },
                onIncrement = { if (age < 100) onAgeChange(age + 1) }
            )

            DimensionStepperCard(
                label = "Height",
                subtitle = "Stature in centimeters",
                valueText = heightCm.toInt().toString(),
                unitText = "cm",
                accentColor = accentColor,
                onDecrement = { if (heightCm > 100) onHeightChange(heightCm - 1.0) },
                onIncrement = { if (heightCm < 240) onHeightChange(heightCm + 1.0) }
            )

            DimensionStepperCard(
                label = "Body Weight",
                subtitle = "Current morning weight",
                valueText = "%.1f".format(Locale.US, weightKg),
                unitText = "kg",
                accentColor = accentColor,
                onDecrement = { if (weightKg > 30.0) onWeightChange(weightKg - 0.5) },
                onIncrement = { if (weightKg < 250.0) onWeightChange(weightKg + 0.5) }
            )
        }

        ScientificInsightCard(note = onboarding.step1InsightNote, accentColor = accentColor, icon = Icons.Filled.Lock)
    }
}

// ===============================================================
// STEP 1 (B) & STEP 2: BESPOKE PROGRAM-SPECIFIC INTAKE QUESTIONS
// ===============================================================
@Composable
private fun StepBespokeAssessmentQuestions(
    title: String,
    subtitle: String,
    accentColor: Color,
    isCompactWidth: Boolean,
    primaryLabel: String,
    primaryBadge: String?,
    primaryOptions: List<AssessmentOptionSpec>,
    selectedPrimaryIndex: Int,
    onSelectPrimary: (Int, AssessmentOptionSpec) -> Unit,
    secondaryLabel: String,
    secondaryBadge: String?,
    secondaryOptions: List<AssessmentOptionSpec>,
    selectedSecondaryIndex: Int,
    onSelectSecondary: (Int, AssessmentOptionSpec) -> Unit,
    insightNote: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = if (isCompactWidth) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFFA8A2BE),
                lineHeight = 20.sp
            )
        }

        if (primaryOptions.isNotEmpty()) {
            SectionHeaderPill(
                label = primaryLabel,
                badge = primaryBadge ?: "Select 1",
                accentColor = accentColor
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                primaryOptions.forEachIndexed { index, option ->
                    BespokeAssessmentOptionCard(
                        indexNumber = index + 1,
                        option = option,
                        accentColor = accentColor,
                        isSelected = selectedPrimaryIndex == index,
                        onClick = { onSelectPrimary(index, option) }
                    )
                }
            }
        }

        if (secondaryOptions.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            SectionHeaderPill(
                label = secondaryLabel,
                badge = secondaryBadge ?: "Select 1",
                accentColor = Color(0xFF00DC82)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                secondaryOptions.forEachIndexed { index, option ->
                    BespokeAssessmentOptionCard(
                        indexNumber = index + 1,
                        option = option,
                        accentColor = accentColor,
                        isSelected = selectedSecondaryIndex == index,
                        onClick = { onSelectSecondary(index, option) }
                    )
                }
            }
        }

        ScientificInsightCard(
            note = insightNote,
            accentColor = accentColor,
            icon = Icons.Filled.AutoAwesome
        )
    }
}

// ==================================================
// STEP 3: BESPOKE SCHEDULE & SETUP
// ==================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step3BespokeScheduleAndSetup(
    onboarding: ProgramOnboardingSpec,
    accentColor: Color,
    isCompactWidth: Boolean,
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    days: Int,
    onDaysChange: (Int) -> Unit,
    equipment: ProgramEquipment,
    onEquipmentChange: (ProgramEquipment) -> Unit,
    startEpochDay: Long,
    onOpenDatePicker: () -> Unit
) {
    val weeklyTotalMinutes = minutes * days
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = onboarding.step3Title,
                style = if (isCompactWidth) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
            Text(
                text = onboarding.step3Subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFFA8A2BE),
                lineHeight = 20.sp
            )
        }

        // Live Weekly Dosage Summary Pill Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191726)),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WEEKLY PROTOCOL DOSE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 0.9.sp
                    )
                    Text(
                        text = "$days active days · ${7 - days} recovery days",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "$weeklyTotalMinutes min/wk",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        SectionHeaderPill(label = onboarding.step3MinutesLabel, badge = "$minutes min/day", accentColor = accentColor)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            onboarding.step3MinuteChoices.forEach { min ->
                val isSelected = minutes == min
                Surface(
                    onClick = { onMinutesChange(min) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) accentColor else Color(0xFF1B1928),
                    border = BorderStroke(1.5.dp, if (isSelected) accentColor else Color(0xFF2E2A40))
                ) {
                    Text(
                        text = "$min min",
                        color = if (isSelected) Color(0xFF130E1E) else Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)
                    )
                }
            }
        }

        SectionHeaderPill(label = onboarding.step3DaysLabel, badge = "$days days/wk", accentColor = accentColor)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            onboarding.step3DayChoices.forEach { d ->
                val isSelected = days == d
                Surface(
                    onClick = { onDaysChange(d) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) accentColor else Color(0xFF1B1928),
                    border = BorderStroke(1.5.dp, if (isSelected) accentColor else Color(0xFF2E2A40))
                ) {
                    Text(
                        text = "$d days/wk",
                        color = if (isSelected) Color(0xFF130E1E) else Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)
                    )
                }
            }
        }

        if (onboarding.showEquipmentSelector && onboarding.step3EquipmentOptions.isNotEmpty()) {
            SectionHeaderPill(label = onboarding.step3EquipmentLabel, badge = equipment.displayName(), accentColor = accentColor)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                onboarding.step3EquipmentOptions.forEach { option ->
                    val mappedEq = option.mappedEquipment ?: ProgramEquipment.NONE
                    val icon = when (mappedEq) {
                        ProgramEquipment.NONE -> Icons.Filled.SelfImprovement
                        ProgramEquipment.HOME_BASIC -> Icons.Filled.Home
                        ProgramEquipment.GYM -> Icons.Filled.FitnessCenter
                    }
                    EquipmentOptionCard(
                        title = option.title,
                        subtitle = option.subtitle,
                        icon = icon,
                        accentColor = accentColor,
                        isSelected = equipment == mappedEq,
                        onClick = { onEquipmentChange(mappedEq) }
                    )
                }
            }
        }

        SectionHeaderPill(label = "START DATE", badge = "Flexible", accentColor = accentColor)
        Card(
            onClick = onOpenDatePicker,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
            border = BorderStroke(1.dp, Color(0xFF2D2942))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(accentColor.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = LocalDate.ofEpochDay(startEpochDay).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (startEpochDay == LocalDate.now().toEpochDay()) "Day 1 begins today" else "Scheduled future launch",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF00DC82)
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF262238),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// STEP 4: BESPOKE DIAGNOSTIC BLUEPRINT + EMBEDDED 7-DAY SCHEDULE PREVIEW
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step4BespokeDiagnosticReview(
    template: ProgramTemplate,
    summary: ProgramDiagnosticSummary,
    accentColor: Color,
    isCompactWidth: Boolean,
    previewActivities: List<ScheduledProgramActivity>,
    startEpochDay: Long,
    isAdjusting: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                shape = RoundedCornerShape(50),
                color = accentColor.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "DIAGNOSTIC BLUEPRINT READY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    letterSpacing = 0.9.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(
                text = summary.step4Headline,
                style = if (isCompactWidth) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
            Text(
                text = summary.step4Subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFA8A2BE),
                lineHeight = 20.sp
            )
        }

        // RESPONSIVE DIAGNOSTIC METRICS GRID (never squishes on compact phones)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            summary.metrics.forEach { metric ->
                StatCard(
                    label = metric.label,
                    value = metric.value,
                    unit = metric.unit,
                    isCompactWidth = isCompactWidth,
                    isHighlighted = metric.isHighlighted,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // MULTI-SEGMENT PROTOCOL / MACRO DISTRIBUTION CARD
        if (summary.segments.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171524)),
                border = BorderStroke(1.dp, Color(0xFF2D2942))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = summary.breakdownCardTitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD5CCFF),
                            letterSpacing = 0.8.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF0D3325),
                            border = BorderStroke(1.dp, Color(0xFF00DC82).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = summary.breakdownBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00DC82),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Segmented Distribution Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF242132)),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        summary.segments.forEach { segment ->
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        summary.segments.forEach { segment ->
                            MacroItem(
                                label = segment.label,
                                value = segment.valueText,
                                sublabel = segment.sublabel,
                                color = Color(segment.colorHex),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // BESPOKE PRESCRIPTION & CADENCE SUMMARY CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.18f),
                                Color(0xFF181625)
                            )
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = summary.prescriptionTitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            letterSpacing = 0.9.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = summary.prescriptionHeadline,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = accentColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Text(
                    text = summary.prescriptionDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD5CFE8),
                    lineHeight = 19.sp
                )
                Text(
                    text = "Launch Date: ${LocalDate.ofEpochDay(startEpochDay).format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00DC82)
                )
            }
        }

        // 3-PHASE SYLLABUS ROADMAP
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
            border = BorderStroke(1.dp, Color(0xFF282538))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${template.durationDays}-DAY PERIODIZED SYLLABUS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    letterSpacing = 1.sp
                )
                template.phases.forEach { phase ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = accentColor.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "P${phase.number}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Days ${phase.startDay}–${phase.endDay} · ${phase.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = phase.activityTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9E98B5)
                            )
                        }
                    }
                }
            }
        }

        // EMBEDDED WEEK-1 DAILY SCHEDULE PREVIEW (Eliminates redundant 5th Review Screen!)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
            border = BorderStroke(1.dp, Color(0xFF282538))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAdjusting) "RECALIBRATED NEXT 7 DAYS" else "WEEK 1 DAILY SCHEDULE PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00DC82),
                        letterSpacing = 0.9.sp
                    )
                    Text(
                        text = "${previewActivities.count { it.estimatedMinutes > 0 }} active / ${previewActivities.count { it.estimatedMinutes == 0 }} rest",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9E98B5)
                    )
                }

                previewActivities.forEach { activity ->
                    val isRest = activity.estimatedMinutes == 0
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isRest) Color(0xFF14121E) else Color(0xFF1F1C2E),
                        border = BorderStroke(1.dp, if (isRest) Color(0xFF232033) else Color(0xFF322D49))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Day ${activity.dayIndex} · " + LocalDate.ofEpochDay(activity.epochDay).format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRest) Color(0xFF7D7894) else accentColor
                                )
                                Text(
                                    text = activity.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isRest) Color(0xFF9E98B5) else Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isRest) Color(0xFF211E30) else Color(0xFF0D3325),
                                border = BorderStroke(
                                    1.dp,
                                    if (isRest) Color(0xFF342F49) else Color(0xFF00DC82).copy(alpha = 0.4f)
                                )
                            ) {
                                Text(
                                    text = if (isRest) "Rest" else "${activity.estimatedMinutes}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isRest) Color(0xFF9E98B5) else Color(0xFF00DC82),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================================================
// SLEEK LUXURY HELPER COMPONENTS & CARDS
// ==================================================
@Composable
private fun SectionHeaderPill(
    label: String,
    badge: String?,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accentColor)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD5CFE8),
                letterSpacing = 0.9.sp
            )
        }
        if (!badge.isNullOrBlank()) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = accentColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ScientificInsightCard(
    note: String,
    accentColor: Color,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181626)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(accentColor.copy(alpha = 0.16f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "CLINICAL & PERFORMANCE RATIONALE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD0C9E8),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BespokeAssessmentOptionCard(
    indexNumber: Int,
    option: AssessmentOptionSpec,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = if (isSelected) 1.6.dp else 1.dp,
            color = if (isSelected) accentColor else Color(0xFF2B273D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (isSelected) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.20f),
                                Color(0xFF1B182B)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF181624), Color(0xFF161420))
                        )
                    }
                )
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left numbered badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        color = if (isSelected) accentColor else Color(0xFF242034),
                        shape = RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "0$indexNumber",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) Color(0xFF130E1E) else Color(0xFF9E98B5)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = option.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (!option.badge.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0xFF29243B),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) accentColor.copy(alpha = 0.6f) else Color(0xFF3B3552)
                            )
                        ) {
                            Text(
                                text = option.badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFFC4B5FD),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Color(0xFFD5CFE8) else Color(0xFF9892B0),
                    lineHeight = 17.sp
                )
            }

            SelectionCheckCircle(isSelected = isSelected, accentColor = accentColor)
        }
    }
}

@Composable
private fun SexOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF231F34) else Color(0xFF171522)
        ),
        border = BorderStroke(
            width = if (isSelected) 1.6.dp else 1.dp,
            color = if (isSelected) accentColor else Color(0xFF282538)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0xFF221F30),
                            shape = RoundedCornerShape(11.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) accentColor else Color(0xFF8E8AA7),
                        modifier = Modifier.size(20.dp)
                    )
                }

                SelectionCheckCircle(isSelected = isSelected, accentColor = accentColor)
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8AA7),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DimensionStepperCard(
    label: String,
    subtitle: String,
    valueText: String,
    unitText: String,
    accentColor: Color,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
        border = BorderStroke(1.dp, Color(0xFF282538))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E8AA7)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StepperButton(symbol = "−", accentColor = accentColor, onClick = onDecrement)

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.widthIn(min = 72.dp),
                ) {
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = unitText,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                StepperButton(symbol = "+", accentColor = accentColor, onClick = onIncrement)
            }
        }
    }
}

@Composable
private fun StepperButton(
    symbol: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(38.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF242134),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EquipmentOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF231F32) else Color(0xFF171522)
        ),
        border = BorderStroke(
            width = if (isSelected) 1.6.dp else 1.dp,
            color = if (isSelected) accentColor else Color(0xFF282538)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0xFF221F30),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) accentColor else Color(0xFF8E8AA7),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8AA7)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            SelectionCheckCircle(isSelected = isSelected, accentColor = accentColor)
        }
    }
}

@Composable
private fun SelectionCheckCircle(
    isSelected: Boolean,
    accentColor: Color = Color(0xFFA78BFA)
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isSelected) accentColor else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (isSelected) accentColor else Color(0xFF433E58),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = Color(0xFF130E1E),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    isCompactWidth: Boolean,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) Color(0xFF142824) else Color(0xFF171522)
        ),
        border = BorderStroke(
            width = if (isHighlighted) 1.4.dp else 1.dp,
            color = if (isHighlighted) Color(0xFF00DC82).copy(alpha = 0.6f) else Color(0xFF282538)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = if (isCompactWidth) 6.dp else 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (isCompactWidth) 9.sp else 10.sp,
                color = if (isHighlighted) Color(0xFF00DC82) else Color(0xFF9E98B5),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = if (isCompactWidth) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isHighlighted) Color(0xFF00DC82) else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                fontSize = if (isCompactWidth) 9.sp else 10.sp,
                color = Color(0xFF9E98B5),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MacroItem(
    label: String,
    value: String,
    sublabel: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD0C9E8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = Color(0xFF8E8AA7),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
