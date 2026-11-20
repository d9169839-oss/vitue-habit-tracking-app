package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

object SimpleSkincareProgram : ProgramDefinition {
    override val accentHex: Long = 0xFFFB7185L
    override val isRecommended: Boolean = false
    override val highlightTags: List<String> = listOf(
        "Dermatology Barrier-First",
        "AM SPF & PM Repair",
        "30-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "skincare-30",
        version = 1,
        title = "Simple Skincare Consistency",
        category = ProgramCategory.SELF_GROOMING,
        description = "Build a minimal barrier-friendly AM/PM skincare ritual and observe how your skin responds.",
        durationDays = 30,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 5,
        isPremium = false,
        equipment = ProgramEquipment.NONE,
        safetyNote = "Avoid introducing many products at once. Stop products that cause irritation and seek clinical advice for persistent concerns.",
        phases = buildPhases(
            duration = 30,
            names = listOf(
                "Phase 1: Cleanse, Moisturize & Protect",
                "Phase 2: Barrier Consistency & Tracking",
                "Phase 3: Minimalist Long-Term Protocol"
            ),
            titles = listOf(
                "Core AM SPF & PM Cleanse Ritual",
                "Skin Barrier & Hydration Check-In",
                "Routine Simplification & Tolerance Review"
            ),
            instructions = listOf(
                "Use gentle cleanser, moisturizer, and morning broad-spectrum SPF that you already tolerate. Avoid adding multiple new actives at once.",
                "Complete your AM and PM routine at the same time each day and note dryness, oiliness, or irritation.",
                "Keep the products that support calm, hydrated skin and eliminate redundant steps. This program does not diagnose skin conditions."
            ),
            minutes = listOf(5, 5, 5)
        )
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "AM Two-Finger Broad-Spectrum SPF Shield",
            "Barrier protocol: gentle lukewarm rinse, lightweight moisturizer on damp skin, and two finger-lengths of SPF 30+ across face, ears, and neck."
        ),
        DailyProgramModule(
            "PM 60-Second Gentle Emulsifying Cleanse",
            "Barrier protocol: massage a non-stripping pH-balanced cleanser for a full 60 seconds to dissolve sunscreen and urban particulates without disrupting lipids."
        ),
        DailyProgramModule(
            "Transepidermal Water Loss (TEWL) Lock-In",
            "Barrier protocol: apply ceramide or humectant moisturizer within 60 seconds of patting skin dry to seal hydration into the stratum corneum."
        ),
        DailyProgramModule(
            "Pillowcase & Touch-Hygiene Reset",
            "Barrier protocol: flip or swap your pillowcase to a fresh side, sanitize your phone screen, and avoid resting hands against your jawline."
        ),
        DailyProgramModule(
            "Skin Barrier Reactivity & Hydration Audit",
            "Barrier protocol: inspect cheek and T-zone comfort under natural light; if tightness or redness appears, skip exfoliants and double down on soothing moisture."
        ),
        DailyProgramModule(
            "Lip, Neck & Periorbital Barrier Care",
            "Barrier protocol: extend your gentle moisturizer down the neck and apply an occlusive balm to lips before sleep."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = false,
        step1Title = "Dermatological Barrier Intake",
        step1Subtitle = "Most skin irritation comes from over-exfoliating or inconsistent sunscreen and cleansing. Let's assess how your skin barrier feels by midday.",
        step1PrimaryQuestionLabel = "HOW DOES YOUR SKIN TYPICALLY FEEL BY MIDDAY?",
        step1PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Dry, Tight or Flaky (Moisture-Depleted Barrier)",
                subtitle = "High transepidermal water loss (TEWL) · Needs lukewarm rinse & damp-skin ceramide lock",
                badge = "BARRIER REPAIR",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "Combination (Shiny T-Zone, Normal/Dry Cheeks)",
                subtitle = "Needs balanced pH-neutral 60-second evening cleansing & lightweight SPF",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "Oily, Congested or Blemish-Prone",
                subtitle = "Needs thorough PM sunscreen emulsification, pillowcase hygiene & non-comedogenic moisture",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step1SecondaryQuestionLabel = "SKIN SENSITIVITY & REACTIVITY LEVEL",
        step1SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Reactive / Easily Stings or Flushes Red",
                subtitle = "Strict 3-step minimalist protocol (Cleanser, Moisturizer, Mineral SPF — zero harsh actives)"
            ),
            AssessmentOptionSpec(
                title = "Moderately Resilient (Occasional Dryness/Breakouts)",
                subtitle = "Focus on 30 days of unbroken AM/PM consistency before changing any products"
            ),
            AssessmentOptionSpec(
                title = "Resilient Barrier (Seeking Long-Term Photo-Aging Protection)",
                subtitle = "Focus on daily two-finger broad-spectrum UV protection and evening lipid repair"
            )
        ),
        step1InsightNote = "The stratum corneum skin barrier takes ~28 days to complete one full cellular turnover cycle.",
        step2Title = "Current Routine & Hygiene Habits",
        step2Subtitle = "Identify which half of the daily skincare equation currently slips through the cracks.",
        step2PrimaryQuestionLabel = "BIGGEST SKINCARE CONSISTENCY GAP",
        step2PrimaryBadge = "Adherence Gap",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Going to Bed Without Washing Off the Day",
                subtitle = "Leaving sweat, pollution, and SPF on overnight clogs pores and triggers oxidative stress"
            ),
            AssessmentOptionSpec(
                title = "Skipping Daily Morning Sunscreen (SPF)",
                subtitle = "UV exposure causes 80%+ of premature collagen breakdown and hyperpigmentation"
            ),
            AssessmentOptionSpec(
                title = "Product Hopping & Using Too Many Actives at Once",
                subtitle = "Destroys the acid mantle; simplifying to 3 tolerated products calms inflammation"
            )
        ),
        step2SecondaryQuestionLabel = "DAILY UV & ENVIRONMENTAL EXPOSURE",
        step2SecondaryBadge = "UV Shield",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Mostly Indoors / Window UVA Exposure",
                subtitle = "UVA rays penetrate window glass; anchors lightweight morning SPF",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "Daily Commuting & Mixed Outdoor Hours",
                subtitle = "Two-finger SPF application across face, ears, and neck every morning",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "High Outdoor Sun, Sweat or Dust Exposure",
                subtitle = "Emphasizes thorough 60-second evening emulsifying cleanse & barrier replenishment",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step2InsightNote = "Applying moisturizer within 60 seconds of washing while skin is still slightly damp traps up to 40% more hydration.",
        step3Title = "AM & PM Ritual Cadence",
        step3Subtitle = "A barrier-first skincare ritual takes just 5 minutes a day when kept simple.",
        step3MinutesLabel = "TOTAL DAILY RITUAL TIME (AM + PM)",
        step3MinuteChoices = listOf(5, 8, 10, 15),
        step3DaysLabel = "SKINCARE DAYS PER WEEK",
        step3DayChoices = listOf(5, 6, 7),
        showEquipmentSelector = false
    )

    override fun buildDiagnosticSummary(
        preferences: ProgramPreferences,
        step1PrimaryIndex: Int,
        step1SecondaryIndex: Int,
        step2PrimaryIndex: Int,
        step2SecondaryIndex: Int
    ): ProgramDiagnosticSummary {
        val skinType = onboarding.step1PrimaryOptions
            .getOrNull(step1PrimaryIndex)?.title?.substringBefore(" (") ?: "Barrier-First Protocol"
        val gap = onboarding.step2PrimaryOptions
            .getOrNull(step2PrimaryIndex)?.title ?: "AM SPF & PM Cleanse"

        return ProgramDiagnosticSummary(
            step4Headline = "28-Day Skin Barrier Turnover Plan",
            step4Subtitle = "Minimalist dermatology protocol engineered to protect your acid mantle, lock in moisture, and shield against UV photo-aging.",
            metrics = listOf(
                DiagnosticMetricSpec("CELL CYCLE", "28–30", "days"),
                DiagnosticMetricSpec("DAILY TIME", "${preferences.availableMinutesPerDay}", "min AM/PM"),
                DiagnosticMetricSpec("CORE STEPS", "3", "essential", isHighlighted = true)
            ),
            breakdownCardTitle = "DERMATOLOGICAL BARRIER PROTOCOL SPLIT",
            breakdownBadge = "pH-Balanced + Broad-Spectrum SPF",
            segments = listOf(
                DiagnosticSegmentSpec(
                    label = "AM UV Shield",
                    valueText = "40%",
                    sublabel = "Two-finger SPF 30+ protection",
                    weight = 40f,
                    colorHex = 0xFFFB7185L
                ),
                DiagnosticSegmentSpec(
                    label = "PM Cleanse",
                    valueText = "35%",
                    sublabel = "60s gentle lipid-safe wash",
                    weight = 35f,
                    colorHex = 0xFF38BDF8L
                ),
                DiagnosticSegmentSpec(
                    label = "TEWL Lock",
                    valueText = "25%",
                    sublabel = "Damp-skin moisturizer & hygiene",
                    weight = 25f,
                    colorHex = 0xFF00DC82L
                )
            ),
            prescriptionTitle = "CALIBRATED SKIN PROFILE",
            prescriptionHeadline = "$skinType · Focus: $gap",
            prescriptionDetail = "${preferences.availableDaysPerWeek} days/wk · ${preferences.availableMinutesPerDay} min minimalist AM protect + PM repair ritual"
        )
    }
}
