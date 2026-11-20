package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

object DopamineDetoxImpulseProgram : ProgramDefinition {
    override val accentHex: Long = 0xFF2DD4BFL
    override val isRecommended: Boolean = true
    override val highlightTags: List<String> = listOf(
        "Dopamine Baseline Reset",
        "Stop Doomscrolling",
        "30-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "dopamine-detox-30",
        version = 1,
        title = "Dopamine Reset & Impulse Mastery",
        category = ProgramCategory.PERSONAL_DEVELOPMENT,
        description = "Neuroscience-backed curriculum inspired by Dr. Anna Lembke & MBRP to break compulsive scrolling, cheap dopamine cravings, and attention fragmentation.",
        durationDays = 30,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 15,
        isPremium = false,
        equipment = ProgramEquipment.NONE,
        safetyNote = "Educational behavioral design for digital habits and impulse awareness. This is not clinical addiction treatment.",
        phases = buildPhases(
            duration = 30,
            names = listOf(
                "Phase 1: Cue Mapping & Friction Architecture",
                "Phase 2: Urge Surfing & Tonic Dopamine Restoration",
                "Phase 3: Deep Focus & Intentional Reward Systems"
            ),
            titles = listOf(
                "Trigger Audit & Digital Friction Setup",
                "10-Second Pause & Urge Surfing",
                "Low-Stimulation Focus & Effort-First Reward"
            ),
            instructions = listOf(
                "Identify your highest-compulsion dopamine triggers (short-form video, late-night scrolling) and insert physical or 10-second breathing friction before access.",
                "When an impulse spike hits, ride the craving wave for 90 seconds without acting on it, observing the physical restlessness rise and fall.",
                "Protect your morning tonic dopamine baseline by putting effort before cheap stimulation and practicing 15 minutes of unstimulated boredom tolerance."
            ),
            minutes = listOf(10, 15, 15)
        )
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "First-Hour Fast from Cheap Dopamine",
            "Neuroscience protocol: keep your phone off social feeds and short-form video for the first 45 minutes after waking so your brain doesn't set an artificially high stimulation threshold."
        ),
        DailyProgramModule(
            "The 'One-Sec' Breath Friction Pause",
            "Neuroscience protocol: before opening any high-dopamine app or tab, take one full 6-second breath and ask: 'Am I seeking a specific tool, or escaping discomfort?'"
        ),
        DailyProgramModule(
            "90-Second Urge Surfing (Marlatt MBRP)",
            "Neuroscience protocol: when a craving to scroll or procrastinate hits, set a 90-second timer. Observe the urge in your chest/hands like an ocean wave cresting and subsiding."
        ),
        DailyProgramModule(
            "Grayscale & Environmental Friction Audit",
            "Neuroscience protocol: remove infinite-scroll shortcuts from your home screen, log out of one distracting feed, or enable grayscale mode for the evening."
        ),
        DailyProgramModule(
            "Unstimulated Boredom Tolerance Walk",
            "Neuroscience protocol: spend 10–15 minutes walking, waiting, or drinking tea with zero podcasts, music, or screens to let your default mode network recalibrate."
        ),
        DailyProgramModule(
            "Effort-Before-Reward Dopamine Pairing",
            "Neuroscience protocol: earn your evening leisure intentionally by completing one meaningful physical or cognitive task before consuming entertainment."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = false,
        step1Title = "Dopamine & Impulse Audit",
        step1Subtitle = "Supernormal digital stimuli flood the striatum with cheap phasic dopamine, down-regulating your baseline motivation. Let's audit your triggers.",
        step1PrimaryQuestionLabel = "PRIMARY HIGH-DOPAMINE COMPULSION LOOP",
        step1PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Short-Form Video & Doomscrolling (Reels / TikTok / Shorts)",
                subtitle = "Variable-ratio reward loops fragmenting your attention span into 15-second clips",
                badge = "MOST COMMON",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "Constant Tab-Switching, Notifications & Info-Binging",
                subtitle = "Inability to sit with a single deep task without checking messages, news, or feeds",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "Late-Night Gaming, Streaming or Impulse Consumption",
                subtitle = "Revenge bedtime procrastination and escaping stress through instant gratification",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step1SecondaryQuestionLabel = "ESTIMATED DAILY UNINTENTIONAL SCREEN / SCROLL TIME",
        step1SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "1–2 Hours / Day (~22 Full Days Lost Per Year)",
                subtitle = "Mild attention fragmentation · Focus on morning dopamine protection"
            ),
            AssessmentOptionSpec(
                title = "3–4 Hours / Day (~53 Full Days Lost Per Year)",
                subtitle = "Moderate receptor down-regulation · Focus on One-Sec breath friction & urge surfing"
            ),
            AssessmentOptionSpec(
                title = "5+ Hours / Day (~76+ Full Days Lost Per Year)",
                subtitle = "Severe tonic dopamine depletion · Full environmental friction & 45-min morning fast"
            )
        ),
        step1InsightNote = "3.5 hours of daily scrolling equals 53 full 24-hour days every year. Reclaiming just 60% restores over 750 hours of life annually.",
        step2Title = "Vulnerability Window & Friction Style",
        step2Subtitle = "Identify when your prefrontal impulse control is weakest and how you want to interrupt automatic muscle memory.",
        step2PrimaryQuestionLabel = "PEAK DOPAMINE VULNERABILITY WINDOW",
        step2PrimaryBadge = "Cue Trigger",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "First Thing in the Morning (In Bed Before Rising)",
                subtitle = "Hijacks your morning tonic dopamine baseline before your feet touch the floor"
            ),
            AssessmentOptionSpec(
                title = "During Workday Boredom or Difficult Tasks",
                subtitle = "Reaching for the phone the moment a task feels ambiguous or frustrating"
            ),
            AssessmentOptionSpec(
                title = "Late Evening Exhaustion & Bedtime Procrastination",
                subtitle = "Scrolling in a fatigued haze when willpower is depleted"
            )
        ),
        step2SecondaryQuestionLabel = "PREFERRED IMPULSE-INTERRUPTION PROTOCOL",
        step2SecondaryBadge = "Neuro-Friction",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "The 'One-Sec' 6-Second Breath Pause",
                subtitle = "Inserts conscious prefrontal awareness before opening any distracting app",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "Physical Environment & Grayscale Friction",
                subtitle = "Phone charged outside bedroom, grayscale evenings, and home-screen purge",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "90-Second Marlatt Urge Surfing + Effort-First Rule",
                subtitle = "Riding craving waves for 90s and earning entertainment through effort",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step2InsightNote = "Neuroscience shows an impulse craving wave peaks and subsides within 90 seconds if you observe it without feeding it.",
        step3Title = "Dopamine Reset Cadence",
        step3Subtitle = "Choose your daily unstimulated focus & reset duration and weekly consistency target.",
        step3MinutesLabel = "DAILY DOPAMINE RESET & FOCUS PRACTICE",
        step3MinuteChoices = listOf(10, 15, 20, 30, 45),
        step3DaysLabel = "RESET DAYS PER WEEK",
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
        val dailyHoursLost = when (step1SecondaryIndex) {
            0 -> 1.5
            1 -> 3.5
            else -> 5.5
        }
        val weeklyHoursReclaimed = (dailyHoursLost * 7 * 0.65).toInt()
        val yearlyDaysReclaimed = ((weeklyHoursReclaimed * 52) / 24.0).toInt()
        val trigger = onboarding.step1PrimaryOptions
            .getOrNull(step1PrimaryIndex)?.title?.substringBefore(" (") ?: "Compulsive Scrolling"

        return ProgramDiagnosticSummary(
            step4Headline = "30-Day Dopamine Receptor Reset",
            step4Subtitle = "Your neuroscience-calibrated friction and urge-surfing plan to restore tonic dopamine sensitivity and reclaim lost hours.",
            metrics = listOf(
                DiagnosticMetricSpec("RECLAIMED", "${weeklyHoursReclaimed}h", "/week", isHighlighted = true),
                DiagnosticMetricSpec("YEARLY GAIN", "$yearlyDaysReclaimed", "full days"),
                DiagnosticMetricSpec("MORNING FAST", "45", "min no-feed")
            ),
            breakdownCardTitle = "DOPAMINE HOMEOSTASIS PROTOCOL SPLIT",
            breakdownBadge = "+${weeklyHoursReclaimed}h Weekly Focus Reclaimed",
            segments = listOf(
                DiagnosticSegmentSpec(
                    label = "Cue Friction",
                    valueText = "40%",
                    sublabel = "One-Sec pause & grayscale",
                    weight = 40f,
                    colorHex = 0xFF2DD4BFL
                ),
                DiagnosticSegmentSpec(
                    label = "Urge Surfing",
                    valueText = "30%",
                    sublabel = "90s MBRP craving waves",
                    weight = 30f,
                    colorHex = 0xFFA78BFAL
                ),
                DiagnosticSegmentSpec(
                    label = "Tonic Reset",
                    valueText = "30%",
                    sublabel = "Morning fast & boredom walk",
                    weight = 30f,
                    colorHex = 0xFF38BDF8L
                )
            ),
            prescriptionTitle = "PRIMARY COMPULSION INTERRUPT",
            prescriptionHeadline = trigger,
            prescriptionDetail = "${preferences.availableDaysPerWeek} days/wk · ${preferences.availableMinutesPerDay} min unstimulated focus + 45-min morning screen fast"
        )
    }
}
