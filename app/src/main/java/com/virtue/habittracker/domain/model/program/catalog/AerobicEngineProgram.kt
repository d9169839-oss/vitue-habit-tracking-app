package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

object AerobicEngineProgram : ProgramDefinition {
    override val accentHex: Long = 0xFFFBBF24L
    override val isRecommended: Boolean = false
    override val highlightTags: List<String> = listOf(
        "3–5 days/wk",
        "Zone 2 & VO2 Max",
        "60-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "stamina-60",
        version = 1,
        title = "Aerobic Engine & Vitality",
        category = ProgramCategory.FITNESS,
        description = "Cardiovascular resilience, metabolic conditioning, and sustained baseline energy for demanding workdays.",
        durationDays = 60,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 25,
        isPremium = false,
        equipment = ProgramEquipment.NONE,
        safetyNote = "Start at a comfortable pace. Seek professional advice if you have health concerns or are returning after injury.",
        phases = buildPhases(
            duration = 60,
            names = listOf(
                "Phase 1: Zone 2 Aerobic Base",
                "Phase 2: Aerobic Threshold & Work Capacity",
                "Phase 3: Metabolic Conditioning & Recovery"
            ),
            titles = listOf(
                "Zone 2 Conversational Cardio",
                "Sustained Tempo & Aerobic Threshold",
                "Interval Conditioning & Parasympathetic Cooldown"
            ),
            instructions = listOf(
                "Sustained low-impact cardio (brisk incline walk, cycle, or jog) at a pace where you can speak full sentences comfortably through nasal breathing.",
                "Extend continuous aerobic duration and weave in 2–3 moderate tempo surges while keeping heart-rate recovery controlled.",
                "Alternate structured higher-output aerobic intervals with active recovery bouts, finishing with a 5-minute heart-rate down-regulation cooldown."
            ),
            minutes = listOf(20, 30, 35)
        )
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "Zone 2 Nasal Aerobic Base",
            "Primary focus: steady conversational pace keeping heart rate in your aerobic fat-oxidation zone (65–75% max HR) using nasal breathing."
        ),
        DailyProgramModule(
            "Aerobic Threshold Tempo Intervals",
            "Primary focus: 3–4 sustained 4-minute moderate-hard tempo surges with controlled 2-minute active walking recovery."
        ),
        DailyProgramModule(
            "Cardio-Respiratory Endurance & Cadence",
            "Primary focus: smooth, rhythmic stride or pedal cadence paired with rhythmic 4-step inhale / 4-step exhale diaphragmatic breathing."
        ),
        DailyProgramModule(
            "VO2 Max Conditioning Bouts",
            "Primary focus: 4 rounds of 90-second higher-output work bouts followed by 2 minutes of full heart-rate recovery."
        ),
        DailyProgramModule(
            "Long Steady-State Vitality Session",
            "Primary focus: extended low-stress aerobic output to stimulate mitochondrial density and capillary stroke volume."
        ),
        DailyProgramModule(
            "Active Flush & Parasympathetic Cooldown",
            "Primary focus: low-intensity circulation flush followed by 5 minutes of extended-exhale (4s in, 8s out) vagal heart-rate recovery."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = false,
        step1Title = "Cardio-Respiratory Baseline",
        step1Subtitle = "We calibrate your 80/20 polarized endurance split (Zone 2 mitochondrial base vs. VO2 Max intervals) to your current aerobic capacity.",
        step1PrimaryQuestionLabel = "CURRENT CONTINUOUS CARDIO CAPACITY",
        step1PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Winded on Stairs / Rebuilding Base",
                subtitle = "10–15 min brisk walk or light jog before heart rate spikes above conversational Zone 2",
                badge = "FOUNDATION",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "20–30 Min Continuous Jog or Cycle",
                subtitle = "Comfortable aerobic base · Ready for structured Zone 2 + lactate threshold surges",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "45+ Min Endurance Athlete",
                subtitle = "Strong aerobic engine · Seeking VO2 Max peak intervals & faster HR recovery",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step1SecondaryQuestionLabel = "PRIMARY VITALITY & STAMINA GOAL",
        step1SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Eliminate Afternoon Brain Fog & Fatigue",
                subtitle = "Build mitochondrial ATP density for steady 14-hour workday cognitive stamina"
            ),
            AssessmentOptionSpec(
                title = "Lower Resting Heart Rate & Stress Reactivity",
                subtitle = "Expand left-ventricular stroke volume and parasympathetic vagal recovery"
            ),
            AssessmentOptionSpec(
                title = "Peak 5K / 10K / Sport Conditioning",
                subtitle = "Push lactate clearance threshold and high-output VO2 Max ceiling"
            )
        ),
        step1InsightNote = "Polarized 80/20 endurance training builds mitochondrial capillary density without burning out your nervous system.",
        step2Title = "Modality & Recovery Load",
        step2Subtitle = "Choose your preferred low-impact or running modality and tell us how physically demanding your day job is.",
        step2PrimaryQuestionLabel = "PREFERRED AEROBIC MODALITY",
        step2PrimaryBadge = "Joint Impact",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Brisk Incline Walking & Rucking",
                subtitle = "Zero joint impact · High Zone 2 fat oxidation and postural endurance",
                mappedEquipment = ProgramEquipment.NONE
            ),
            AssessmentOptionSpec(
                title = "Outdoor Running & Tempo Strides",
                subtitle = "Elastic tendon return, cadence efficiency, and outdoor aerobic flow",
                mappedEquipment = ProgramEquipment.HOME_BASIC
            ),
            AssessmentOptionSpec(
                title = "Cycling, Rowing or Stationary Erg",
                subtitle = "Precision wattage/pace control with zero eccentric landing stress",
                mappedEquipment = ProgramEquipment.GYM
            )
        ),
        step2SecondaryQuestionLabel = "DAILY WORKDAY PHYSICAL LOAD",
        step2SecondaryBadge = "CNS Recovery",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Mostly Seated / Desk Work",
                subtitle = "High cognitive load, low physical fatigue · Can tolerate 4–5 aerobic sessions/wk",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "Mixed Standing & Commuting",
                subtitle = "Moderate daily step count · Balanced Zone 2 and recovery cadence",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "On Feet / Physically Active Work",
                subtitle = "Auto-caps high-intensity intervals to prevent overtraining and shin/joint fatigue",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step2InsightNote = "Zone 2 pace means you can speak a full 12-word sentence out loud through nasal breathing without gasping.",
        step3Title = "Endurance Schedule & Cadence",
        step3Subtitle = "Select your daily cardio duration and how many days per week you want to train your aerobic engine.",
        step3MinutesLabel = "CARDIO SESSION DURATION",
        step3MinuteChoices = listOf(15, 20, 30, 45, 60),
        step3DaysLabel = "ENDURANCE DAYS PER WEEK",
        step3DayChoices = listOf(3, 4, 5, 6),
        showEquipmentSelector = false
    )

    override fun buildDiagnosticSummary(
        preferences: ProgramPreferences,
        step1PrimaryIndex: Int,
        step1SecondaryIndex: Int,
        step2PrimaryIndex: Int,
        step2SecondaryIndex: Int
    ): ProgramDiagnosticSummary {
        val weeklyMinutes = preferences.availableDaysPerWeek * preferences.availableMinutesPerDay
        val zone2Minutes = (weeklyMinutes * 0.78f).toInt()
        val thresholdMinutes = (weeklyMinutes * 0.14f).toInt()
        val vo2Minutes = (weeklyMinutes - zone2Minutes - thresholdMinutes).coerceAtLeast(5)
        val modality = onboarding.step2PrimaryOptions.getOrNull(step2PrimaryIndex)?.title ?: "Zone 2 Aerobic Conditioning"

        return ProgramDiagnosticSummary(
            step4Headline = "Polarized Aerobic Engine Blueprint",
            step4Subtitle = "Your 60-day cardiovascular periodization balances low-stress mitochondrial base work with targeted VO2 Max surges.",
            metrics = listOf(
                DiagnosticMetricSpec("ZONE 2 BASE", "$zone2Minutes", "min/wk"),
                DiagnosticMetricSpec("VO2 & TEMPO", "${thresholdMinutes + vo2Minutes}", "min/wk"),
                DiagnosticMetricSpec("WEEKLY LOAD", "$weeklyMinutes", "min total", isHighlighted = true)
            ),
            breakdownCardTitle = "80/20 POLARIZED INTENSITY DISTRIBUTION",
            breakdownBadge = "Nasal Zone 2 + VO2 Peak",
            segments = listOf(
                DiagnosticSegmentSpec(
                    label = "Zone 2 Base",
                    valueText = "${zone2Minutes}m",
                    sublabel = "78% · Mitochondrial fat oxidation",
                    weight = 78f,
                    colorHex = 0xFF00DC82L
                ),
                DiagnosticSegmentSpec(
                    label = "Threshold",
                    valueText = "${thresholdMinutes}m",
                    sublabel = "14% · Lactate clearance",
                    weight = 14f,
                    colorHex = 0xFFFBBF24L
                ),
                DiagnosticSegmentSpec(
                    label = "VO2 Max",
                    valueText = "${vo2Minutes}m",
                    sublabel = "8% · Peak stroke volume",
                    weight = 8f,
                    colorHex = 0xFFA78BFAL
                )
            ),
            prescriptionTitle = "CARDIOVASCULAR PRESCRIPTION",
            prescriptionHeadline = "$modality · ${preferences.availableDaysPerWeek} sessions/wk",
            prescriptionDetail = "Target HR Zone 2 (65–75% max) with 1 weekly VO2 interval session & parasympathetic nasal cooldowns"
        )
    }
}
