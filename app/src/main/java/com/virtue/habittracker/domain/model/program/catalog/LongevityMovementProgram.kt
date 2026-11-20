package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

object LongevityMovementProgram : ProgramDefinition {
    override val accentHex: Long = 0xFF38BDF8L
    override val isRecommended: Boolean = false
    override val highlightTags: List<String> = listOf(
        "3–5 days/wk",
        "Joint CARs & Mobility",
        "30-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "movement-30",
        version = 1,
        title = "Longevity & Movement Health",
        category = ProgramCategory.FITNESS,
        description = "Joint articulation, thoracic mobility, active tissue recovery, and sustainable low-fatigue functional movement.",
        durationDays = 30,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 20,
        isPremium = false,
        equipment = ProgramEquipment.NONE,
        safetyNote = "General wellness only. Stop for pain, dizziness, chest discomfort, or unusual breathlessness.",
        phases = buildPhases(
            duration = 30,
            names = listOf(
                "Phase 1: Joint Articulation & Baseline",
                "Phase 2: Thoracic & Hip Flow",
                "Phase 3: Longevity & Tissue Resilience"
            ),
            titles = listOf(
                "Joint Articulation & Spine Decompression",
                "Thoracic Flow & Active Hip Mobility",
                "Full-Body Longevity Flow & Breathwork"
            ),
            instructions = listOf(
                "Controlled articular rotations (CARs) for neck, shoulders, hips, and ankles followed by brisk low-impact walking. Stop if you feel pain, dizziness, or unusual shortness of breath.",
                "Deep squat holds, thoracic spine rotations, cat-cow flows, and 15 minutes of conversational movement to restore joint range of motion.",
                "Integrated full-body mobility sequence, single-leg balance stability, and parasympathetic nasal breathing cooldown."
            ),
            minutes = listOf(15, 20, 25)
        )
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "Cervical, Shoulder & Thoracic CARs",
            "Primary focus: slow controlled articular rotations for the neck, scapular glide, and thoracic windmills to reverse desk posture."
        ),
        DailyProgramModule(
            "Hip Capsule, 90/90 & Ankle Dorsiflexion",
            "Primary focus: 90/90 internal/external hip transitions, deep resting squat hold, and knee-to-wall ankle mobility."
        ),
        DailyProgramModule(
            "Spinal Segmentation & Posterior Chain",
            "Primary focus: vertebra-by-vertebra cat-cow waves, sciatic nerve glides, and psoas hip-flexor decompression."
        ),
        DailyProgramModule(
            "Single-Leg Balance & Proprioception",
            "Primary focus: barefoot foot-tripod rooting, single-leg clock reaches, and controlled tibialis raises for knee longevity."
        ),
        DailyProgramModule(
            "Full-Body Fascial Unwinding Flow",
            "Primary focus: continuous world's-greatest-stretch transitions, cossack lunges, and lateral ribcage expansion."
        ),
        DailyProgramModule(
            "Deep Tissue Recovery & Vagal Reset",
            "Primary focus: passive 90-second floor opener holds paired with slow 5-second nasal inhales and 7-second exhales."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = false,
        step1Title = "Joint & Posture Diagnostic",
        step1Subtitle = "Identify where stiffness, desk compression, or restricted range of motion is holding back your physical freedom.",
        step1PrimaryQuestionLabel = "PRIMARY STIFFNESS OR TENSION ZONE",
        step1PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Neck, Rounded Shoulders & Upper Back",
                subtitle = "Thoracic kyphosis, tight pecs, and cervical tension from screen/desk hours",
                badge = "DESK RELIEF"
            ),
            AssessmentOptionSpec(
                title = "Tight Hips, Lower Back & Hamstrings",
                subtitle = "Shortened psoas hip flexors, stiff lumbar spine, and restricted deep squat",
                badge = "HIP CAPSULE"
            ),
            AssessmentOptionSpec(
                title = "Ankles, Knees & Full-Body Stiffness",
                subtitle = "Morning joint creakiness, balance instability, and reduced stride elasticity"
            )
        ),
        step1SecondaryQuestionLabel = "DAILY SITTING & POSTURAL EXPOSURE",
        step1SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "6–10+ Hours Seated at a Desk or Screen",
                subtitle = "High static flexion load requiring daily spinal decompression & hip extension",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "Mixed Standing, Driving & Walking",
                subtitle = "Moderate postural fatigue requiring rotational thoracic & ankle CARs",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "Heavy Lifting or Manual Physical Work",
                subtitle = "High joint compression requiring gentle synovial flushing & passive tissue unloading",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step1InsightNote = "Controlled Articular Rotations (CARs) circulate synovial fluid into avascular joint cartilage every morning.",
        step2Title = "Mobility Range & Flow Depth",
        step2Subtitle = "Tell us your current functional mobility level so we can scale end-range isometric holds safely.",
        step2PrimaryQuestionLabel = "CURRENT FUNCTIONAL MOBILITY BASELINE",
        step2PrimaryBadge = "End-Range Control",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Stiff / Restricted Range of Motion",
                subtitle = "Heels lift in a squat, tight hamstrings · Gentle pain-free articular circles",
                badge = "GENTLE START",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "Moderate Mobility / Occasional Stretching",
                subtitle = "Comfortable floor transitions · Ready for 90/90 hip flows & segmental cat-cows",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "Flexible / Seeking End-Range Joint Strength",
                subtitle = "Deep squat comfort · Ready for PAILs/RAILs isometric end-range control",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step2SecondaryQuestionLabel = "PREFERRED MOBILITY TIMING",
        step2SecondaryBadge = "Daily Anchor",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Morning Joint Lubrication (Upon Waking)",
                subtitle = "Eliminates morning stiffness and activates proprioception for the day"
            ),
            AssessmentOptionSpec(
                title = "Mid-Day Desk Decompression Break",
                subtitle = "Reverses 4+ hours of sitting compression and restores mental alertness"
            ),
            AssessmentOptionSpec(
                title = "Evening Fascial Unwinding & Vagal Downshift",
                subtitle = "Releases accumulated muscular tension before sleep"
            )
        ),
        step2InsightNote = "Active end-range mobility builds usable joint resilience, whereas passive stretching alone only creates temporary tolerance.",
        step3Title = "Mobility Rhythm & Schedule",
        step3Subtitle = "Choose how many minutes per day and days per week you want to practice joint longevity.",
        step3MinutesLabel = "DAILY MOBILITY FLOW DURATION",
        step3MinuteChoices = listOf(10, 15, 20, 30),
        step3DaysLabel = "MOBILITY DAYS PER WEEK",
        step3DayChoices = listOf(3, 4, 5, 6, 7),
        showEquipmentSelector = false
    )

    override fun buildDiagnosticSummary(
        preferences: ProgramPreferences,
        step1PrimaryIndex: Int,
        step1SecondaryIndex: Int,
        step2PrimaryIndex: Int,
        step2SecondaryIndex: Int
    ): ProgramDiagnosticSummary {
        val focusZone = onboarding.step1PrimaryOptions.getOrNull(step1PrimaryIndex)?.title ?: "Full-Body Joint Longevity"
        val baselineScore = when (preferences.experience) {
            ProgramExperience.BEGINNER -> 58
            ProgramExperience.SOME_EXPERIENCE -> 72
            ProgramExperience.EXPERIENCED -> 82
        }
        val projectedScore = (baselineScore + 22).coerceAtMost(96)

        return ProgramDiagnosticSummary(
            step4Headline = "30-Day Joint Longevity Blueprint",
            step4Subtitle = "Tailored articular rotation (CARs), spinal segmentation, and fascial decompression protocol for your primary tension zones.",
            metrics = listOf(
                DiagnosticMetricSpec("BASELINE ROM", "$baselineScore", "/100"),
                DiagnosticMetricSpec("30-DAY TARGET", "$projectedScore", "/100", isHighlighted = true),
                DiagnosticMetricSpec("FLOW CADENCE", "${preferences.availableDaysPerWeek}×${preferences.availableMinutesPerDay}", "min/wk")
            ),
            breakdownCardTitle = "JOINT ARTICULATION & TISSUE ALLOCATION",
            breakdownBadge = "Synovial CARs + Fascial Flow",
            segments = listOf(
                DiagnosticSegmentSpec(
                    label = "Joint CARs",
                    valueText = "40%",
                    sublabel = "Capsule lubrication & rotation",
                    weight = 40f,
                    colorHex = 0xFF38BDF8L
                ),
                DiagnosticSegmentSpec(
                    label = "Spine & Hips",
                    valueText = "35%",
                    sublabel = "90/90 transitions & thoracic flow",
                    weight = 35f,
                    colorHex = 0xFF00DC82L
                ),
                DiagnosticSegmentSpec(
                    label = "Vagal Release",
                    valueText = "25%",
                    sublabel = "Nasal breathing & balance",
                    weight = 25f,
                    colorHex = 0xFFA78BFAL
                )
            ),
            prescriptionTitle = "PRIMARY DECOMPRESSION FOCUS",
            prescriptionHeadline = focusZone,
            prescriptionDetail = "${preferences.availableDaysPerWeek} days/wk · ${preferences.availableMinutesPerDay} min pain-free articular flows & postural resets"
        )
    }
}
