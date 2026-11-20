package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

object CircadianSleepRestorationProgram : ProgramDefinition {
    override val accentHex: Long = 0xFF818CF8L
    override val isRecommended: Boolean = true
    override val highlightTags: List<String> = listOf(
        "CBT-I Gold Standard",
        "Circadian & Deep Sleep",
        "30-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "cbt-sleep-30",
        version = 1,
        title = "CBT-I Deep Sleep & Circadian Reset",
        category = ProgramCategory.MINDFULNESS,
        description = "Stanford/VA-inspired CBT for Insomnia (CBT-I) protocol to eliminate bedtime racing thoughts, anchor your wake window, and restore deep REM/slow-wave sleep.",
        durationDays = 30,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 10,
        isPremium = false,
        equipment = ProgramEquipment.NONE,
        safetyNote = "Educational sleep hygiene and CBT-I behavioral conditioning only. Do not drive or operate machinery when drowsy; consult a physician for sleep apnea or severe chronic insomnia.",
        phases = buildPhases(
            duration = 30,
            names = listOf(
                "Phase 1: Circadian Anchoring & Adenosine Pressure",
                "Phase 2: Stimulus Control & Bed-Sleep Reconditioning",
                "Phase 3: Cognitive De-Arousal & Sleep Architecture"
            ),
            titles = listOf(
                "Wake-Window Lock & Caffeine Cutoff",
                "Stimulus Control & Thermal Drop Ritual",
                "Constructive Worry Dump & Cognitive Shuffle"
            ),
            instructions = listOf(
                "Anchor your wake-up time within a 30-minute window every day and enforce an 8–10 hour caffeine cutoff before bedtime to build clean homeostatic sleep drive.",
                "Reserve the bed strictly for sleep—never scrolling or worrying. Trigger your core body temperature drop 60 minutes before sleep.",
                "Offload tomorrow's unfinished tasks onto paper before entering the bedroom and use Serial Diverse Imagining (cognitive shuffling) to quiet verbal rumination."
            ),
            minutes = listOf(10, 10, 12)
        )
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "Morning Photon Anchor & Fixed Wake Time",
            "CBT-I protocol: view outdoor daylight within 30–60 minutes of your fixed wake time to start the 16-hour melatonin countdown timer in your suprachiasmatic nucleus."
        ),
        DailyProgramModule(
            "Adenosine Protection & 10-Hour Caffeine Cutoff",
            "CBT-I protocol: cut off all caffeine at least 10 hours before bed and avoid late afternoon naps so homeostatic sleep pressure peaks smoothly at night."
        ),
        DailyProgramModule(
            "Constructive Worry & Open-Loop Brain Dump",
            "CBT-I protocol: 2 hours before bed, spend 5 minutes writing tomorrow's top 3 tasks on paper so your prefrontal cortex stops rehearsing them on the pillow."
        ),
        DailyProgramModule(
            "Thermal Core-Drop & Lux Dimming Ritual",
            "CBT-I protocol: dim overhead blue/white lighting to warm lamps 60 minutes before bed and take a warm shower to dilate peripheral blood vessels and drop core temperature."
        ),
        DailyProgramModule(
            "Stimulus Control & The 20-Minute Rule",
            "CBT-I protocol: charge your phone outside reach of the bed. If awake and frustrated for ~20 minutes, sit in dim light with a paper book until eyelids feel heavy."
        ),
        DailyProgramModule(
            "Cognitive Shuffling (Serial Diverse Imagining)",
            "CBT-I protocol: pick a neutral word (e.g., 'GARDEN') and slowly visualize unrelated objects for each letter (G-Guitar, A-Apple, R-River) to induce micro-dreams."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = false,
        step1Title = "Sleep Architecture & Latency Intake",
        step1Subtitle = "CBT for Insomnia (CBT-I) is the first-line clinical gold standard for sleep restoration. Let's diagnose your sleep efficiency bottleneck.",
        step1PrimaryQuestionLabel = "PRIMARY SLEEP DISRUPTION PATTERN",
        step1PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Sleep-Onset Insomnia (Mind Races on the Pillow)",
                subtitle = "Takes 30–60+ minutes to fall asleep due to cognitive hyper-arousal or late scrolling",
                badge = "ONSET LATENCY",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "Sleep-Maintenance Awakenings (Waking at 2–4 AM)",
                subtitle = "Fragmented sleep architecture and difficulty falling back asleep mid-night",
                badge = "FRAGMENTATION",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "Delayed Circadian Phase & Exhausted Mornings",
                subtitle = "Second wind late at night followed by heavy grogginess and snoozing at dawn",
                badge = "CIRCADIAN DRIFT",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step1SecondaryQuestionLabel = "AVERAGE TIME TO FALL ASLEEP (SLEEP LATENCY)",
        step1SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "15–30 Minutes (Mild Latency & Shallow Sleep)",
                subtitle = "Estimated baseline sleep efficiency: ~78%"
            ),
            AssessmentOptionSpec(
                title = "30–60 Minutes (Moderate Conditioned Arousal)",
                subtitle = "Estimated baseline sleep efficiency: ~68%"
            ),
            AssessmentOptionSpec(
                title = "60+ Minutes or Frequent Night Awakenings",
                subtitle = "Estimated baseline sleep efficiency: ~58% · High stimulus-control priority"
            )
        ),
        step1InsightNote = "Clinical trials show CBT-I outperforms sleeping pills long-term by rebuilding natural adenosine pressure and bed-sleep conditioning.",
        step2Title = "Circadian & Environmental Disruptors",
        step2Subtitle = "Identify the evening stimuli and biological signals currently suppressing your pineal melatonin release.",
        step2PrimaryQuestionLabel = "PRIMARY EVENING SLEEP DISRUPTOR",
        step2PrimaryBadge = "Melatonin Gate",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Phone / Screen Blue Light in Bed",
                subtitle = "Suppresses melatonin by up to 50% and pairs the mattress with dopamine alertness"
            ),
            AssessmentOptionSpec(
                title = "Afternoon Caffeine or Irregular Meal Timing",
                subtitle = "Caffeine's 6-hour half-life blocks adenosine receptors deep into the night"
            ),
            AssessmentOptionSpec(
                title = "Rehearsing Tomorrow's Tasks & Unfinished Loops",
                subtitle = "Keeps sympathetic cortisol elevated during your slow-wave sleep window"
            )
        ),
        step2SecondaryQuestionLabel = "CURRENT WAKE-TIME CONSISTENCY",
        step2SecondaryBadge = "Circadian Anchor",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Varies by 2+ Hours on Weekends (Social Jetlag)",
                subtitle = "Priority #1: Lock a consistent wake anchor + 10-min morning daylight viewing",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "Consistent Within ~60 Minutes",
                subtitle = "Good anchor · Focus on evening thermal drop and constructive worry dumps",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "Fixed Early Wake Time",
                subtitle = "Strong anchor · Focus on Cognitive Shuffling and 20-minute stimulus control",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step2InsightNote = "Dropping core body temperature by 1°C via a warm shower 60 minutes before bed accelerates slow-wave sleep onset.",
        step3Title = "Evening Wind-Down Schedule",
        step3Subtitle = "Set how many minutes each evening you will dedicate to your CBT-I wind-down ritual.",
        step3MinutesLabel = "EVENING WIND-DOWN RITUAL DURATION",
        step3MinuteChoices = listOf(8, 10, 15, 20, 30),
        step3DaysLabel = "NIGHTS PER WEEK",
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
        val baselineEfficiency = when (step1SecondaryIndex) {
            0 -> 78
            1 -> 68
            else -> 58
        }
        val targetEfficiency = (baselineEfficiency + 22).coerceAtMost(92)
        val pattern = onboarding.step1PrimaryOptions
            .getOrNull(step1PrimaryIndex)?.title?.substringBefore(" (") ?: "Sleep-Onset Restoration"

        return ProgramDiagnosticSummary(
            step4Headline = "CBT-I Sleep Restoration Profile",
            step4Subtitle = "Your 30-day circadian & stimulus-control prescription calibrated to elevate sleep efficiency above the 85% clinical threshold.",
            metrics = listOf(
                DiagnosticMetricSpec("CURRENT EFF.", "$baselineEfficiency%", "in bed"),
                DiagnosticMetricSpec("30-DAY TARGET", "$targetEfficiency%", "efficiency", isHighlighted = true),
                DiagnosticMetricSpec("LATENCY GOAL", "< 15", "minutes")
            ),
            breakdownCardTitle = "CBT-I CLINICAL INTERVENTION SPLIT",
            breakdownBadge = "+${targetEfficiency - baselineEfficiency}% Sleep Efficiency Gain",
            segments = listOf(
                DiagnosticSegmentSpec(
                    label = "Circadian Lock",
                    valueText = "35%",
                    sublabel = "Morning light & caffeine cutoff",
                    weight = 35f,
                    colorHex = 0xFF818CF8L
                ),
                DiagnosticSegmentSpec(
                    label = "Stimulus Ctrl",
                    valueText = "35%",
                    sublabel = "Phone-out-of-bed & thermal drop",
                    weight = 35f,
                    colorHex = 0xFF38BDF8L
                ),
                DiagnosticSegmentSpec(
                    label = "De-Arousal",
                    valueText = "30%",
                    sublabel = "Worry dump & cognitive shuffle",
                    weight = 30f,
                    colorHex = 0xFF00DC82L
                )
            ),
            prescriptionTitle = "PRIMARY CBT-I PROTOCOL",
            prescriptionHeadline = pattern,
            prescriptionDetail = "${preferences.availableDaysPerWeek} nights/wk · ${preferences.availableMinutesPerDay} min wind-down ritual + 10-hour caffeine cutoff"
        )
    }
}
