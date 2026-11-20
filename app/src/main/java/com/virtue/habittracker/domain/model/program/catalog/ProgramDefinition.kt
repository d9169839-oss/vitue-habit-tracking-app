package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPhaseTemplate
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel
import com.virtue.habittracker.domain.service.FitnessMetabolicCalculator

/**
 * Self-contained daily clinical or physical training module within a program.
 * Each program provides its own distinct sequence of daily modules so no two programs
 * ever share the same daily content.
 */
data class DailyProgramModule(
    val titleSuffix: String,
    val focusNote: String
)

/**
 * An interactive assessment option inside a program's bespoke Step 1 or Step 2 onboarding screen.
 */
data class AssessmentOptionSpec(
    val title: String,
    val subtitle: String,
    val badge: String? = null,
    val mappedExperience: ProgramExperience? = null,
    val mappedWorkLevel: WorkActivityLevel? = null,
    val mappedEquipment: ProgramEquipment? = null
)

/**
 * A single metric tile in Step 4's diagnostic summary (e.g., "64% -> 88% Sleep Efficiency",
 * "16.8h Weekly Time Reclaimed", or "2,450 kcal Anabolic Target").
 */
data class DiagnosticMetricSpec(
    val label: String,
    val value: String,
    val unit: String,
    val isHighlighted: Boolean = false
)

/**
 * A horizontal segment bar item in Step 4's diagnostic breakdown card.
 */
data class DiagnosticSegmentSpec(
    val label: String,
    val valueText: String,
    val sublabel: String,
    val weight: Float,
    val colorHex: Long
)

/**
 * The dynamically computed Step 4 "Aha! Diagnostic Payoff" tailored to the specific program
 * and the user's Step 1–3 inputs.
 */
data class ProgramDiagnosticSummary(
    val step4Headline: String,
    val step4Subtitle: String,
    val metrics: List<DiagnosticMetricSpec>,
    val breakdownCardTitle: String,
    val breakdownBadge: String,
    val segments: List<DiagnosticSegmentSpec>,
    val prescriptionTitle: String,
    val prescriptionHeadline: String,
    val prescriptionDetail: String
)

/**
 * Full 4-step bespoke onboarding specification for a program.
 */
data class ProgramOnboardingSpec(
    val usesBiometricPhysicalStep1: Boolean = false,
    val step1Title: String,
    val step1Subtitle: String,
    val step1PrimaryQuestionLabel: String,
    val step1PrimaryOptions: List<AssessmentOptionSpec> = emptyList(),
    val step1SecondaryQuestionLabel: String = "",
    val step1SecondaryOptions: List<AssessmentOptionSpec> = emptyList(),
    val step1InsightNote: String,
    val step2Title: String,
    val step2Subtitle: String,
    val step2PrimaryQuestionLabel: String,
    val step2PrimaryBadge: String,
    val step2PrimaryOptions: List<AssessmentOptionSpec>,
    val step2SecondaryQuestionLabel: String,
    val step2SecondaryBadge: String,
    val step2SecondaryOptions: List<AssessmentOptionSpec>,
    val step2InsightNote: String,
    val step3Title: String,
    val step3Subtitle: String,
    val step3MinutesLabel: String,
    val step3MinuteChoices: List<Int>,
    val step3DaysLabel: String,
    val step3DayChoices: List<Int>,
    val showEquipmentSelector: Boolean,
    val step3EquipmentLabel: String = "EQUIPMENT ACCESS",
    val step3EquipmentOptions: List<AssessmentOptionSpec> = emptyList()
)

/**
 * Contract for a modular program definition. Each program lives in its own file and
 * encapsulates its metadata, phases, UI badges, daily rotating modules, and bespoke
 * 4-step onboarding assessment + diagnostic engine.
 */
interface ProgramDefinition {
    val template: ProgramTemplate
    val accentHex: Long
    val isRecommended: Boolean get() = false
    val highlightTags: List<String>
    val dailyModules: List<DailyProgramModule>
    val onboarding: ProgramOnboardingSpec

    fun buildDiagnosticSummary(
        preferences: ProgramPreferences,
        step1PrimaryIndex: Int,
        step1SecondaryIndex: Int,
        step2PrimaryIndex: Int,
        step2SecondaryIndex: Int
    ): ProgramDiagnosticSummary
}

internal fun buildPhases(
    duration: Int,
    names: List<String>,
    titles: List<String>,
    instructions: List<String>,
    minutes: List<Int>
): List<ProgramPhaseTemplate> {
    val phaseLength = duration / names.size
    return names.indices.map { index ->
        ProgramPhaseTemplate(
            number = index + 1,
            title = names[index],
            startDay = index * phaseLength + 1,
            endDay = if (index == names.lastIndex) duration else (index + 1) * phaseLength,
            focus = names[index],
            activityTitle = titles[index],
            instructions = instructions[index],
            minutes = minutes[index]
        )
    }
}

internal fun defaultEquipmentOptions(): List<AssessmentOptionSpec> = listOf(
    AssessmentOptionSpec(
        title = "Bodyweight Only",
        subtitle = "Zero equipment · Calisthenics, tempo control, and mobility",
        mappedEquipment = ProgramEquipment.NONE
    ),
    AssessmentOptionSpec(
        title = "Home Dumbbells & Bands",
        subtitle = "Adjustable dumbbells, kettlebell, or resistance bands",
        mappedEquipment = ProgramEquipment.HOME_BASIC
    ),
    AssessmentOptionSpec(
        title = "Full Gym Suite",
        subtitle = "Barbells, cables, squat racks, and machine stations",
        mappedEquipment = ProgramEquipment.GYM
    )
)

internal fun buildMetabolicDiagnosticSummary(
    template: ProgramTemplate,
    preferences: ProgramPreferences,
    headline: String,
    subtitle: String,
    deltaBadgeOverride: String? = null,
    prescriptionTitle: String,
    prescriptionHeadline: String,
    prescriptionDetail: String
): ProgramDiagnosticSummary {
    val profile = FitnessMetabolicCalculator.calculate(
        biometrics = preferences.biometrics,
        experience = preferences.experience,
        focusProgramId = template.id
    )
    val proteinKcal = profile.proteinGrams * 4
    val carbsKcal = profile.carbsGrams * 4
    val fatKcal = profile.fatGrams * 9
    val totalMacroKcal = (proteinKcal + carbsKcal + fatKcal).coerceAtLeast(1)

    val proteinPct = (proteinKcal * 100f / totalMacroKcal).toInt()
    val carbsPct = (carbsKcal * 100f / totalMacroKcal).toInt()
    val fatPct = (100 - proteinPct - carbsPct).coerceAtLeast(0)

    val calorieDelta = profile.targetCalories - profile.tdee
    val deltaBadgeText = deltaBadgeOverride ?: when {
        calorieDelta > 50 -> "+$calorieDelta kcal Anabolic Surplus"
        calorieDelta < -50 -> "$calorieDelta kcal Recomp Deficit"
        else -> "Isocaloric Maintenance"
    }

    return ProgramDiagnosticSummary(
        step4Headline = headline,
        step4Subtitle = subtitle,
        metrics = listOf(
            DiagnosticMetricSpec("BMR", "${profile.bmr}", "kcal"),
            DiagnosticMetricSpec("TDEE", "${profile.tdee}", "kcal"),
            DiagnosticMetricSpec("TARGET", "${profile.targetCalories}", "kcal", isHighlighted = true)
        ),
        breakdownCardTitle = "TARGET MACRONUTRIENT PARTITIONING",
        breakdownBadge = deltaBadgeText,
        segments = listOf(
            DiagnosticSegmentSpec(
                label = "Protein",
                valueText = "${profile.proteinGrams}g",
                sublabel = "$proteinPct% · $proteinKcal kcal",
                weight = proteinKcal.toFloat().coerceAtLeast(1f),
                colorHex = 0xFFA78BFAL
            ),
            DiagnosticSegmentSpec(
                label = "Carbs",
                valueText = "${profile.carbsGrams}g",
                sublabel = "$carbsPct% · $carbsKcal kcal",
                weight = carbsKcal.toFloat().coerceAtLeast(1f),
                colorHex = 0xFF38BDF8L
            ),
            DiagnosticSegmentSpec(
                label = "Fats",
                valueText = "${profile.fatGrams}g",
                sublabel = "$fatPct% · $fatKcal kcal",
                weight = fatKcal.toFloat().coerceAtLeast(1f),
                colorHex = 0xFFFBBF24L
            )
        ),
        prescriptionTitle = prescriptionTitle,
        prescriptionHeadline = prescriptionHeadline,
        prescriptionDetail = prescriptionDetail
    )
}
