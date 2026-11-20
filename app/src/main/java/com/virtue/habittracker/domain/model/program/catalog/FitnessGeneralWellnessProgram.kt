package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramArtwork
import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

object FitnessGeneralWellnessProgram : ProgramDefinition {
    override val accentHex: Long = 0xFF34D399L
    override val isRecommended: Boolean = false
    override val highlightTags: List<String> = listOf(
        "3–5 days/wk",
        "Zero-Equipment Baseline",
        "60-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "fitness-wellness-60",
        version = 1,
        title = "Fitness & General Wellness",
        category = ProgramCategory.FITNESS,
        description = "Build a sustainable foundation with comfortable movement, everyday mobility, recovery, hydration awareness, and consistent sleep routines.",
        durationDays = 60,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 15,
        isPremium = false,
        equipment = ProgramEquipment.NONE,
        safetyNote = "General-wellness guidance only, not medical advice. Begin at a comfortable pace, take easier days, and stop if you experience pain, dizziness, chest discomfort, or unusual breathlessness. Seek qualified advice if you have a health concern or are returning after injury.",
        phases = buildPhases(
            duration = 60,
            names = listOf("Build a baseline", "Create a steady rhythm", "Make it sustainable"),
            titles = listOf("Comfortable movement check-in", "Movement and mobility", "Recovery and routine review"),
            instructions = listOf(
                "Choose 5–10 minutes of comfortable walking or another familiar low-impact activity. Notice how you feel; rest whenever needed.",
                "Choose comfortable low-impact movement, add gentle mobility if it feels good, and keep the effort conversational. Include easier days.",
                "Combine familiar movement with a short cooldown. Review sleep and hydration habits without strict targets or guilt."
            ),
            minutes = listOf(10, 15, 15)
        ),
        artwork = ProgramArtwork.FITNESS_WELLNESS
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "Morning Sunlight Walk & Hydration Anchor",
            "Clinical wellness focus: get 10 minutes of outdoor natural light within an hour of waking while drinking 500ml of water to anchor your circadian cortisol curve."
        ),
        DailyProgramModule(
            "Postural Decompression & Desk Reset",
            "Clinical wellness focus: open tight pectoral muscles, perform 10 wall angels, and walk briskly for 10 minutes to restore spinal hydration."
        ),
        DailyProgramModule(
            "Conversational Stride & Nasal Breathing",
            "Clinical wellness focus: maintain an easy walking pace where you can breathe exclusively through your nose to stimulate nitric oxide and vagal tone."
        ),
        DailyProgramModule(
            "Lower-Body Circulation & Hip Unloading",
            "Clinical wellness focus: gentle bodyweight sit-to-stands, calf pumps, and hip-flexor stretches to counteract sedentary venous pooling."
        ),
        DailyProgramModule(
            "Core Bracing & Balance Foundations",
            "Clinical wellness focus: gentle bird-dog holds, glute activation bridges, and tandem stance balance checks."
        ),
        DailyProgramModule(
            "Evening Wind-Down Stretch & Sleep Prep",
            "Clinical wellness focus: slow hamstring and upper-back floor stretches paired with dimming overhead lights 60 minutes before bed."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = false,
        step1Title = "Foundational Vitality Check-In",
        step1Subtitle = "Tell us how your daily energy, movement, and hydration currently feel so we can build a zero-guilt 60-day wellness rhythm.",
        step1PrimaryQuestionLabel = "CURRENT DAILY ENERGY & VITALITY BASELINE",
        step1PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Low Energy & Sedentary Routine",
                subtitle = "Returning after a long break · Start with gentle 10–15 min morning walks & water anchors",
                badge = "ZERO PRESSURE",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "Inconsistent / On-and-Off Movement",
                subtitle = "Active some days, exhausted others · Needs a steady, repeatable daily baseline",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "Active & Seeking Daily Holistic Structure",
                subtitle = "Combine daily walks, postural resets, hydration, and evening sleep wind-downs",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step1SecondaryQuestionLabel = "BIGGEST DAILY WELLNESS BOTTLENECK",
        step1SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Sitting All Day & Forgetting to Move",
                subtitle = "Inserts simple conversational walks and desk posture decompression"
            ),
            AssessmentOptionSpec(
                title = "Chronic Dehydration & Midday Slumps",
                subtitle = "Pairs morning sunlight movement with a 500ml wake-up hydration anchor"
            ),
            AssessmentOptionSpec(
                title = "All-or-Nothing Burnout from Hard Workouts",
                subtitle = "Replaces punishing workouts with sustainable low-cortisol daily movement"
            )
        ),
        step1InsightNote = "Just 15 minutes of outdoor morning walking and hydration improves daytime alertness by 34% without taxing recovery.",
        step2Title = "Daily Rhythm & Lifestyle Load",
        step2Subtitle = "Choose the time of day when a gentle wellness habit fits most naturally into your life.",
        step2PrimaryQuestionLabel = "PREFERRED WELLNESS ANCHOR WINDOW",
        step2PrimaryBadge = "Habit Trigger",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Morning Sunlight & Hydration Start",
                subtitle = "First hour after waking · Sets circadian cortisol and energy for the day"
            ),
            AssessmentOptionSpec(
                title = "Lunchtime Reset Walk & Stretch",
                subtitle = "Midday break · Clears mental fatigue and aids digestion"
            ),
            AssessmentOptionSpec(
                title = "After-Work Decompression Ritual",
                subtitle = "Late afternoon / evening · Transitions your nervous system out of work mode"
            )
        ),
        step2SecondaryQuestionLabel = "WORKDAY ACTIVITY LEVEL",
        step2SecondaryBadge = "Recovery Pacing",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Mostly Seated (Office / Study / Remote)",
                subtitle = "Prioritizes circulation walks and upper-back opening",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "Mixed Standing & Errands",
                subtitle = "Balances gentle movement with joint-unloading stretches",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "Physically Demanding Day",
                subtitle = "Caps active days to 4/wk and emphasizes restorative cooldowns",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step2InsightNote = "Anchoring a new movement habit to an existing daily event (waking up, lunch, or finishing work) doubles 60-day adherence.",
        step3Title = "Your Zero-Equipment Schedule",
        step3Subtitle = "Pick a comfortable daily time commitment and how many days per week you want to check in.",
        step3MinutesLabel = "COMFORTABLE MINUTES PER DAY",
        step3MinuteChoices = listOf(10, 15, 20, 30),
        step3DaysLabel = "WELLNESS DAYS PER WEEK",
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
        val anchorWindow = onboarding.step2PrimaryOptions.getOrNull(step2PrimaryIndex)?.title ?: "Morning Sunlight & Hydration Start"
        val weeklyMinutes = preferences.availableDaysPerWeek * preferences.availableMinutesPerDay

        return ProgramDiagnosticSummary(
            step4Headline = "60-Day Holistic Vitality Plan",
            step4Subtitle = "A low-friction, zero-equipment foundation combining outdoor movement, postural relief, and daily hydration.",
            metrics = listOf(
                DiagnosticMetricSpec("DAILY SESSION", "${preferences.availableMinutesPerDay}", "min"),
                DiagnosticMetricSpec("WEEKLY RHYTHM", "${preferences.availableDaysPerWeek}", "days/wk"),
                DiagnosticMetricSpec("TOTAL VOLUME", "$weeklyMinutes", "min/wk", isHighlighted = true)
            ),
            breakdownCardTitle = "DAILY WELLNESS PILLAR BALANCE",
            breakdownBadge = "Zero-Equipment Protocol",
            segments = listOf(
                DiagnosticSegmentSpec(
                    label = "Movement",
                    valueText = "50%",
                    sublabel = "Conversational walks & circulation",
                    weight = 50f,
                    colorHex = 0xFF34D399L
                ),
                DiagnosticSegmentSpec(
                    label = "Mobility",
                    valueText = "30%",
                    sublabel = "Posture & spine decompression",
                    weight = 30f,
                    colorHex = 0xFF38BDF8L
                ),
                DiagnosticSegmentSpec(
                    label = "Hydration/Sleep",
                    valueText = "20%",
                    sublabel = "Circadian light & water anchors",
                    weight = 20f,
                    colorHex = 0xFFA78BFAL
                )
            ),
            prescriptionTitle = "YOUR PRIMARY HABIT ANCHOR",
            prescriptionHeadline = anchorWindow,
            prescriptionDetail = "${preferences.availableDaysPerWeek} days/wk · ${preferences.availableMinutesPerDay} min gentle movement, posture check & hydration"
        )
    }
}
