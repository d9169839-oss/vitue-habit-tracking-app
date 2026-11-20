package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel
import com.virtue.habittracker.domain.service.FitnessMetabolicCalculator

object BuildLeanMuscleProgram : ProgramDefinition {
    override val accentHex: Long = 0xFFA78BFAL
    override val isRecommended: Boolean = true
    override val highlightTags: List<String> = listOf(
        "4–5 days/wk",
        "Hypertrophic Macros",
        "60-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "home-strength-60",
        version = 1,
        title = "Build Lean Muscle",
        category = ProgramCategory.FITNESS,
        description = "Progressive overload focused on hypertrophic adaptation, structural density, and functional power.",
        durationDays = 60,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 30,
        isPremium = false,
        equipment = ProgramEquipment.HOME_BASIC,
        safetyNote = "Use stable supports and comfortable ranges of motion. This is not individualized medical advice.",
        phases = buildPhases(
            duration = 60,
            names = listOf(
                "Phase 1: Neuromuscular Foundation",
                "Phase 2: Progressive Volume Accumulation",
                "Phase 3: Peak Hypertrophy & Structural Density"
            ),
            titles = listOf(
                "Foundational Compound Mechanics",
                "Hypertrophic Overload Session",
                "Structural Density & Tempo Strength"
            ),
            instructions = listOf(
                "Focus on controlled eccentric tempo (3s lowering) across primary push, pull, squat, and hinge patterns. Leave 2–3 reps in reserve (RIR) and prioritize joint alignment.",
                "Add 1–2 working sets per primary muscle group. Push working sets to 1–2 reps in reserve with 90-second recovery intervals between compound bouts.",
                "Peak tension phase combining slow-eccentric compound work, isometric pauses at peak contraction, and post-session mobility decompression."
            ),
            minutes = listOf(25, 35, 45)
        )
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "Upper Body Push & Anterior Core",
            "Primary focus: chest, anterior deltoids, triceps, and anti-extension core control. Execute 3–4 working sets of horizontal and incline pressing with a 3-1-1 tempo."
        ),
        DailyProgramModule(
            "Lower Body Quad & Glute Drive",
            "Primary focus: squat mechanics, split-stance stability, and knee-over-toe control. Drive through mid-foot on squats and Bulgarian split squats."
        ),
        DailyProgramModule(
            "Upper Body Pull & Scapular Density",
            "Primary focus: lats, upper-back retraction, rear delts, and elbow flexor tension. Pause 1 second at peak contraction on rows and vertical pulls."
        ),
        DailyProgramModule(
            "Posterior Chain Hinge & Hamstrings",
            "Primary focus: hip-hinge mechanics, glute bridge isometric pauses, and lower-back endurance. Keep a neutral spine through Romanian deadlift patterns."
        ),
        DailyProgramModule(
            "Full-Body Hypertrophy & Metabolic Pump",
            "Primary focus: multi-joint antagonist supersets, controlled 2-second peak contractions, and thoracic decompression."
        ),
        DailyProgramModule(
            "Structural Symmetry & Unilateral Strength",
            "Primary focus: single-arm/single-leg balance, core anti-rotation carries, and rotator cuff/hip stabilizer integrity."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = true,
        step1Title = "Anabolic Biometrics",
        step1Subtitle = "Your baseline height, weight, and age calibrate your Mifflin-St Jeor resting metabolic rate and anabolic +10% lean-mass surplus.",
        step1PrimaryQuestionLabel = "BIOLOGICAL SEX",
        step1SecondaryQuestionLabel = "PHYSICAL DIMENSIONS",
        step1InsightNote = "Processed on-device · Used for hypertrophy protein & lean surplus calibration",
        step2Title = "Resistance & Hypertrophy History",
        step2Subtitle = "We calibrate weekly working sets per muscle group (MEV to MAV) and mechanical tension progression to your lifting background.",
        step2PrimaryQuestionLabel = "RESISTANCE TRAINING LEVEL",
        step2PrimaryBadge = "Weekly Set Volume",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Novice Lifter (< 1 Year)",
                subtitle = "12 working sets/muscle/wk · Linear progressive overload & 3s eccentric form mastery",
                badge = "RECOMMENDED",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "Intermediate Hypertrophy (1–3 Years)",
                subtitle = "16 working sets/muscle/wk · Double-progression & 1–2 Reps in Reserve (RIR)",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "Advanced Lifter (3+ Years)",
                subtitle = "20 working sets/muscle/wk · Myo-rep density, lengthened partials & periodized deloads",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step2SecondaryQuestionLabel = "PRIMARY MUSCLE ADAPTATION FOCUS",
        step2SecondaryBadge = "Split Architecture",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Upper-Body V-Taper & Pressing Power",
                subtitle = "Emphasizes clavicular pecs, lateral delts, and upper-back width",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "Balanced Full-Body Aesthetics & Symmetry",
                subtitle = "Equal volume distribution across push, pull, quads, and posterior chain",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "Athletic Power & Recovery-Capped Volume",
                subtitle = "High-output sessions with protected systemic CNS recovery days",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step2InsightNote = "Research shows 12–20 hard sets per muscle group within 1–3 RIR maximizes myofibrillar protein synthesis.",
        step3Title = "Training Split & Gym Setup",
        step3Subtitle = "Select your session duration, weekly lifting frequency, and available resistance equipment.",
        step3MinutesLabel = "SESSION DURATION",
        step3MinuteChoices = listOf(20, 30, 45, 60),
        step3DaysLabel = "LIFTING DAYS PER WEEK",
        step3DayChoices = listOf(3, 4, 5, 6),
        showEquipmentSelector = true,
        step3EquipmentLabel = "RESISTANCE EQUIPMENT ACCESS",
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
        val splitFocus = onboarding.step2SecondaryOptions
            .getOrNull(step2SecondaryIndex)?.title ?: "Balanced Full-Body Aesthetics"
        return buildMetabolicDiagnosticSummary(
            template = template,
            preferences = preferences,
            headline = "Anabolic Hypertrophy Blueprint",
            subtitle = "Calibrated +10% lean-mass caloric surplus (2.0g/kg protein) paired with progressive mechanical tension.",
            prescriptionTitle = "WEEKLY HYPERTROPHY PRESCRIPTION",
            prescriptionHeadline = "${profile.weeklySetsPerMuscleGroup} working sets / muscle group / week",
            prescriptionDetail = "Focus: $splitFocus · ${preferences.availableDaysPerWeek} days/wk (${preferences.availableMinutesPerDay} min) at 1–2 RIR"
        )
    }
}
