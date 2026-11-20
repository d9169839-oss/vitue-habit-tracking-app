package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel
import com.virtue.habittracker.domain.service.FitnessMetabolicCalculator

object CalmRecompositionProgram : ProgramDefinition {
    override val accentHex: Long = 0xFF00DC82L
    override val isRecommended: Boolean = false
    override val highlightTags: List<String> = listOf(
        "3–4 days/wk",
        "High-Protein Fueling",
        "90-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "weight-habits-90",
        version = 1,
        title = "Calm Recomposition",
        category = ProgramCategory.FITNESS,
        description = "Simultaneous fat loss and muscle retention with precise caloric partitioning, NEAT movement, and steady volume.",
        durationDays = 90,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 25,
        isPremium = false,
        equipment = ProgramEquipment.NONE,
        safetyNote = "This program does not prescribe crash diets or rapid weight change. Seek qualified guidance for medical or eating-related concerns.",
        phases = buildPhases(
            duration = 90,
            names = listOf(
                "Phase 1: Metabolic Baseline & Satiety",
                "Phase 2: Resistance Retention & NEAT",
                "Phase 3: Sustainable Recomp Maintenance"
            ),
            titles = listOf(
                "Resistance Primer & Protein-Anchor Check",
                "Full-Body Retention Session & NEAT Walk",
                "Metabolic Consolidation & Recovery Audit"
            ),
            instructions = listOf(
                "Complete a 20-minute compound resistance or brisk movement session and anchor meals around whole-food protein and hydration.",
                "Pair full-body muscle-retention resistance movements with an easy post-meal walk to elevate daily non-exercise energy expenditure.",
                "Maintain strength training density while auditing sleep quality, recovery markers, and sustainable meal rhythm."
            ),
            minutes = listOf(20, 25, 30)
        )
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "Full-Body Muscle Retention & Protein Anchor",
            "Primary focus: compound muscle-preservation sets (push-ups, squats, rows) + hitting your first 35g+ whole-food protein meal anchor."
        ),
        DailyProgramModule(
            "NEAT Step Surge & Core Stability",
            "Primary focus: 20-minute post-meal brisk walk to blunt glucose spikes + anti-rotation plank and dead-bug trunk endurance."
        ),
        DailyProgramModule(
            "Lower Body Metabolic Resistance",
            "Primary focus: high-recruitment leg and glute circuits (lunges, tempo squats, glute bridges) with 60-second active rest intervals."
        ),
        DailyProgramModule(
            "Upper Body Density & Satiety Check-In",
            "Primary focus: push/pull superset pairing + high-fiber vegetable volume and mindful hunger-scale check before dinner."
        ),
        DailyProgramModule(
            "Low-Impact Fat-Oxidation & Mobility Flow",
            "Primary focus: steady conversational Zone 2 walk or cycle + hip flexor and thoracic opening to lower cortisol."
        ),
        DailyProgramModule(
            "Metabolic Reset & Sleep Recovery Audit",
            "Primary focus: light functional decompression movement + locking in an 8-hour sleep window to protect leptin and ghrelin balance."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = true,
        step1Title = "Body Recomposition Baseline",
        step1Subtitle = "Your biometrics establish your total daily energy expenditure (TDEE) so we can set a gentle -15% fat-loss deficit without triggering metabolic adaptation or muscle loss.",
        step1PrimaryQuestionLabel = "BIOLOGICAL SEX",
        step1SecondaryQuestionLabel = "CURRENT BODY METRICS",
        step1InsightNote = "Processed on-device · Calculates 2.2g/kg lean-mass protection protein target",
        step2Title = "Satiety, NEAT & Metabolic Rhythm",
        step2Subtitle = "Unlike crash diets, body recomposition relies on high protein satiety, post-meal NEAT walking, and muscle-retention tension.",
        step2PrimaryQuestionLabel = "CURRENT BODY-COMPOSITION STARTING POINT",
        step2PrimaryBadge = "Recomp Pace",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Restarting Consistency / Soft Baseline",
                subtitle = "Focus on effortless 35g+ protein meal anchors, post-meal walks, and joint-friendly resistance",
                badge = "MOST POPULAR",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "Skinny-Fat / Midsection Recomposition",
                subtitle = "Simultaneous visceral fat reduction and lean shoulder/back/glute density",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "Lean Definition & Final 5–8% Polish",
                subtitle = "High muscle-retention tension with strict protein-sparing caloric partitioning",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step2SecondaryQuestionLabel = "PRIMARY NUTRITION & ENERGY FRICTION",
        step2SecondaryBadge = "Satiety Strategy",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Evening Cravings & Late-Night Snacking",
                subtitle = "Anchors 40g protein at breakfast and fiber volume at dinner to silence ghrelin spikes",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "Low Workday Movement (Under 5,000 Steps)",
                subtitle = "Integrates two 15-minute post-meal glucose-clearing walks into your daily schedule",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "High Stress / Cortisol Fatigue",
                subtitle = "Caps high-cortisol training to 4 days/wk and prioritizes Zone 2 fat oxidation",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step2InsightNote = "A 15% deficit paired with 2.2g/kg protein preserves 95%+ of lean muscle tissue while oxidizing adipose stores.",
        step3Title = "Recomposition Cadence & Setup",
        step3Subtitle = "Choose your daily movement window, weekly muscle-retention days, and preferred training environment.",
        step3MinutesLabel = "DAILY MOVEMENT & RESISTANCE WINDOW",
        step3MinuteChoices = listOf(15, 20, 25, 30, 45),
        step3DaysLabel = "ACTIVE RECOMP DAYS PER WEEK",
        step3DayChoices = listOf(3, 4, 5, 6),
        showEquipmentSelector = true,
        step3EquipmentLabel = "PREFERRED MOVEMENT ENVIRONMENT",
        step3EquipmentOptions = defaultEquipmentOptions()
    )

    override fun buildDiagnosticSummary(
        preferences: ProgramPreferences,
        step1PrimaryIndex: Int,
        step1SecondaryIndex: Int,
        step2PrimaryIndex: Int,
        step2SecondaryIndex: Int
    ): ProgramDiagnosticSummary {
        val profile = FitnessMetabolicCalculator.calculate(
            biometrics = preferences.biometrics,
            experience = preferences.experience,
            focusProgramId = template.id
        )
        val calorieDeficit = (profile.tdee - profile.targetCalories).coerceAtLeast(150)
        val mealProtein = (profile.proteinGrams / 3).coerceAtLeast(30)
        return buildMetabolicDiagnosticSummary(
            template = template,
            preferences = preferences,
            headline = "90-Day Recomposition Partitioning",
            subtitle = "Calibrated -$calorieDeficit kcal/day fat-oxidation deficit with elevated 2.2g/kg protein to lock in lean muscle tissue.",
            deltaBadgeOverride = "-$calorieDeficit kcal Recomp Deficit",
            prescriptionTitle = "PROTEIN ANCHOR & NEAT TARGET",
            prescriptionHeadline = "${mealProtein}g protein × 3 meals + 8,000 NEAT steps/day",
            prescriptionDetail = "${preferences.availableDaysPerWeek} muscle-retention sessions/wk (${preferences.availableMinutesPerDay} min) · Sustainable ~0.4–0.6 kg/wk fat loss"
        )
    }
}
