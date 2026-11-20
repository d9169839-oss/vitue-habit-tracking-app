package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

object EverydayGroomingProgram : ProgramDefinition {
    override val accentHex: Long = 0xFFC084FCL
    override val isRecommended: Boolean = false
    override val highlightTags: List<String> = listOf(
        "Executive Presence",
        "Zero-Friction AM/PM",
        "30-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "grooming-30",
        version = 1,
        title = "Everyday Grooming Routine",
        category = ProgramCategory.SELF_GROOMING,
        description = "Create a simple, high-consistency personal hygiene, presentation, and evening preparation routine.",
        durationDays = 30,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 10,
        isPremium = false,
        equipment = ProgramEquipment.NONE,
        safetyNote = "Adapt the routine to your needs, skin sensitivity, culture, and preferences.",
        phases = buildPhases(
            duration = 30,
            names = listOf(
                "Phase 1: Frictionless Environment Setup",
                "Phase 2: Morning & Evening Rhythm",
                "Phase 3: Executive Presence & Refinement"
            ),
            titles = listOf(
                "Essential Grooming & Prep Reset",
                "AM/PM Grooming & Wardrobe Staging",
                "Full Routine Audit & Signature Polish"
            ),
            instructions = listOf(
                "Organize your essential hygiene tools on a clear counter and complete your core dental, hair, and tidy-up checklist.",
                "Complete your daily grooming sequence and lay out tomorrow's outfit and essentials the night before to eliminate morning friction.",
                "Refine your weekly maintenance habits (nails, trim, fabric care) and streamline your daily ritual so it runs on autopilot."
            ),
            minutes = listOf(8, 10, 12)
        )
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "Oral Microbiome & Morning Freshness Protocol",
            "Daily ritual: 2-minute precision brushing, interdental flossing, tongue scraping, and cold-water facial rinse to trigger immediate morning alertness."
        ),
        DailyProgramModule(
            "Evening Wardrobe & EDC Staging",
            "Daily ritual: lay out tomorrow's clean outfit, shoes, and everyday carry items the night before to eliminate morning decision fatigue."
        ),
        DailyProgramModule(
            "Hair, Facial Hair & Edge Maintenance",
            "Daily ritual: check neckline, stray brow/beard edges, and scalp/hair hydration so your presentation stays sharp between barber or salon visits."
        ),
        DailyProgramModule(
            "Hands, Nails & Detail Grooming Check",
            "Daily ritual: trim or clean fingernails, apply cuticle/hand moisture, and check collar/cuff cleanliness."
        ),
        DailyProgramModule(
            "Posture, Footwear & Fabric Refresh",
            "Daily ritual: wipe down everyday footwear, steam or hang key garments, and practice 60 seconds of tall clavicular posture."
        ),
        DailyProgramModule(
            "Bathroom Counter Reset & Tool Sanitization",
            "Daily ritual: rinse razors/brushes, wipe the vanity mirror and sink clear, and restock depleted essentials for a calm hotel-clean environment."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = false,
        step1Title = "Grooming & Presence Audit",
        step1Subtitle = "Consistent personal presentation builds immediate self-respect and lowers daily social friction. Let's tailor your routine.",
        step1PrimaryQuestionLabel = "CURRENT GROOMING & PREPARATION CONSISTENCY",
        step1PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Rushed Mornings & Inconsistent Basics",
                subtitle = "Scrambling for clean clothes or skipping flossing/evening prep when tired",
                badge = "FRICTION RESET",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "Solid Basics, Missing Sharp Detail Maintenance",
                subtitle = "Daily hygiene is covered, but nails, edges, footwear, and wardrobe staging lag",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "Refining Executive Presence & Effortless Polish",
                subtitle = "Looking to systematize every daily and weekly grooming detail on autopilot",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step1SecondaryQuestionLabel = "PRIMARY PRESENTATION PRIORITY",
        step1SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Oral Hygiene, Freshness & Clean Vanity Setup",
                subtitle = "Precision brushing, interdental flossing, tongue scraping, and clutter-free counter"
            ),
            AssessmentOptionSpec(
                title = "Night-Before Wardrobe & EDC Staging",
                subtitle = "Eliminating morning decision fatigue by staging outfits, shoes, and bags each evening"
            ),
            AssessmentOptionSpec(
                title = "Hair, Beard/Brow Edges, Nails & Garment Care",
                subtitle = "Looking freshly groomed every day between barber or salon appointments"
            )
        ),
        step1InsightNote = "Enclothed cognition research shows structured grooming and outfit staging measurably increases executive confidence and task persistence.",
        step2Title = "Routine Environment & Pacing",
        step2Subtitle = "Tell us where friction usually derails your morning or evening preparation.",
        step2PrimaryQuestionLabel = "BIGGEST DAILY GROOMING FRICTION POINT",
        step2PrimaryBadge = "Environment",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Cluttered Counter / Missing Tools When Needed",
                subtitle = "Phase 1 starts by organizing your core essentials into a visible 3-item dock"
            ),
            AssessmentOptionSpec(
                title = "Evening Exhaustion (Skipping Nighttime Reset)",
                subtitle = "Moves evening prep to right after dinner before couch fatigue sets in"
            ),
            AssessmentOptionSpec(
                title = "Alarm Snoozing & Morning Time Crunch",
                subtitle = "Shifts 70% of preparation (outfit, bag, trim) to the evening before"
            )
        ),
        step2SecondaryQuestionLabel = "DAILY PROFESSIONAL / SOCIAL SETTING",
        step2SecondaryBadge = "Presence Level",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Remote / Work or Study From Home",
                subtitle = "Anchors a crisp morning dress-and-groom ritual to separate home from work mode",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "In-Person Office, Campus or Client-Facing",
                subtitle = "Emphasizes wardrobe staging, fabric/shoe polish, and detail checks",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "Active / On-the-Move Environment",
                subtitle = "Focuses on fast, durable freshness and quick evening recovery resets",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step2InsightNote = "Staging your outfit and everyday carry items the night before saves an average of 12 minutes and 4 decisions every morning.",
        step3Title = "Daily Grooming Cadence",
        step3Subtitle = "Select your daily routine window and weekly consistency target.",
        step3MinutesLabel = "DAILY AM/PM GROOMING WINDOW",
        step3MinuteChoices = listOf(5, 8, 10, 15, 20),
        step3DaysLabel = "ROUTINE DAYS PER WEEK",
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
        val priority = onboarding.step1SecondaryOptions
            .getOrNull(step1SecondaryIndex)?.title ?: "AM/PM Grooming & Wardrobe Staging"

        return ProgramDiagnosticSummary(
            step4Headline = "30-Day Executive Presence Protocol",
            step4Subtitle = "Your frictionless AM/PM hygiene, detail maintenance, and night-before wardrobe staging system.",
            metrics = listOf(
                DiagnosticMetricSpec("AM/PM RITUAL", "${preferences.availableMinutesPerDay}", "min/day"),
                DiagnosticMetricSpec("CONSISTENCY", "${preferences.availableDaysPerWeek}", "days/wk"),
                DiagnosticMetricSpec("AM SAVED", "~12m", "zero friction", isHighlighted = true)
            ),
            breakdownCardTitle = "DAILY GROOMING & STAGING ALLOCATION",
            breakdownBadge = "Autopilot AM/PM System",
            segments = listOf(
                DiagnosticSegmentSpec(
                    label = "Core Hygiene",
                    valueText = "45%",
                    sublabel = "Oral microbiome & freshness",
                    weight = 45f,
                    colorHex = 0xFFC084FCL
                ),
                DiagnosticSegmentSpec(
                    label = "PM Staging",
                    valueText = "30%",
                    sublabel = "Wardrobe, shoes & EDC prep",
                    weight = 30f,
                    colorHex = 0xFF38BDF8L
                ),
                DiagnosticSegmentSpec(
                    label = "Detail Polish",
                    valueText = "25%",
                    sublabel = "Edges, nails & vanity reset",
                    weight = 25f,
                    colorHex = 0xFF00DC82L
                )
            ),
            prescriptionTitle = "PRIMARY PRESENCE ANCHOR",
            prescriptionHeadline = priority,
            prescriptionDetail = "${preferences.availableDaysPerWeek} days/wk · ${preferences.availableMinutesPerDay} min structured AM hygiene & PM wardrobe staging"
        )
    }
}
