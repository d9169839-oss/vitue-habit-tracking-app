package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

object MindfulnessAnxietyReliefProgram : ProgramDefinition {
    override val accentHex: Long = 0xFF60A5FAL
    override val isRecommended: Boolean = true
    override val highlightTags: List<String> = listOf(
        "CBT & ACT Protocol",
        "Vagal Down-Regulation",
        "30-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "mindful-30",
        version = 1,
        title = "CBT Calm & Nervous System Reset",
        category = ProgramCategory.MINDFULNESS,
        description = "Psychiatrist-designed CBT & ACT protocol for anxiety regulation, physiological sigh breathing, cognitive defusion, and somatic grounding.",
        durationDays = 30,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 10,
        isPremium = false,
        equipment = ProgramEquipment.NONE,
        safetyNote = "You can pause or switch to an eyes-open grounding exercise at any time. This educational self-regulation curriculum is not a substitute for clinical psychiatric or emergency care.",
        phases = buildPhases(
            duration = 30,
            names = listOf(
                "Phase 1: Autonomic Nervous System Down-Regulation",
                "Phase 2: Cognitive Defusion & Thought Records (CBT)",
                "Phase 3: Uncertainty Tolerance & Emotional Resilience"
            ),
            titles = listOf(
                "Somatic Vagal Braking & Grounding",
                "CBT Automatic Thought Catch & Reframe",
                "ACT Defusion & Values-Anchored Calm"
            ),
            instructions = listOf(
                "Down-regulate sympathetic fight-or-flight arousal first through physiological sighs and sensory orientation before attempting to analyze anxious thoughts.",
                "Identify one automatic negative thought (ANT), label the cognitive distortion (catastrophizing, mind-reading, all-or-nothing), and write one evidence-based balanced alternative.",
                "Practice observing intrusive worries as passing mental events ('I notice I am having the thought that...') while taking one small values-aligned action."
            ),
            minutes = listOf(8, 10, 12)
        )
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "Physiological Sigh & Vagal Braking",
            "Clinical protocol: perform 5 double-inhales through the nose followed by a long, slow mouth exhale to offload CO2 and trigger the parasympathetic vagus nerve brake."
        ),
        DailyProgramModule(
            "5-4-3-2-1 Exteroceptive Sensory Anchor",
            "Clinical protocol: interrupt internal rumination loops by naming 5 visual details, 4 tactile sensations, 3 ambient sounds, 2 scents, and 1 steady breath."
        ),
        DailyProgramModule(
            "CBT 3-Column Thought Record & Distortion Audit",
            "Clinical protocol: write down 1) The triggering event, 2) Your automatic anxious prediction, and 3) Objective evidence for and against that catastrophe."
        ),
        DailyProgramModule(
            "ACT Cognitive Defusion ('Leaves on a Stream')",
            "Clinical protocol: take a recurring worry and prefix it with 'I am noticing the thought that...'. Visualize placing the sentence on a leaf floating down a stream without fighting it."
        ),
        DailyProgramModule(
            "Progressive Muscle Relaxation (Jacobson PMR)",
            "Clinical protocol: systematically tense for 5 seconds and release for 10 seconds across jaw, shoulders, hands, stomach, and calves to discharge somatic armouring."
        ),
        DailyProgramModule(
            "Scheduled Worry Window & Container Closure",
            "Clinical protocol: confine open-loop anxieties to a strict 5-minute written dump, distinguish solvable actions from hypothetical noise, and physically close the notebook."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = false,
        step1Title = "Nervous System & Anxiety Intake",
        step1Subtitle = "In clinical CBT and somatic psychiatry, we match your daily intervention to how anxiety manifests in your body and thoughts.",
        step1PrimaryQuestionLabel = "HOW DOES STRESS OR ANXIETY SHOW UP MOST FOR YOU?",
        step1PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Racing Thoughts & 'What-If' Rumination Loops",
                subtitle = "Overanalyzing conversations, future uncertainty, and mental restlessness",
                badge = "CBT FOCUS"
            ),
            AssessmentOptionSpec(
                title = "Physical Tension, Tight Chest & Shallow Breathing",
                subtitle = "Sympathetic fight-or-flight arousal, clenched jaw, or elevated heart rate",
                badge = "VAGAL BRAKE"
            ),
            AssessmentOptionSpec(
                title = "Overwhelm, Avoidance & Decision Paralysis",
                subtitle = "Feeling flooded by responsibilities and shutting down under pressure",
                badge = "ACT GROUNDING"
            )
        ),
        step1SecondaryQuestionLabel = "WHEN DOES YOUR NERVOUS SYSTEM FEEL MOST OVERLOADED?",
        step1SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Morning Cortisol Spike Upon Waking",
                subtitle = "Waking up with immediate dread or urgency before the day even begins"
            ),
            AssessmentOptionSpec(
                title = "Midday Workday Pressure & Social Evaluation",
                subtitle = "Accumulated stress during meetings, deadlines, or high-stimulation tasks"
            ),
            AssessmentOptionSpec(
                title = "Late-Night Quiet When Distractions Fade",
                subtitle = "Unprocessed open loops surfacing as soon as you try to rest"
            )
        ),
        step1InsightNote = "68% of anxious rumination is driven by an upregulated sympathetic nervous system—calming the body first unlocks cognitive clarity.",
        step2Title = "Cognitive & Somatic Profile",
        step2Subtitle = "Identify your most common cognitive distortion pattern so your daily CBT thought records target the right mental habit.",
        step2PrimaryQuestionLabel = "MOST FAMILIAR COGNITIVE THINKING TRAP",
        step2PrimaryBadge = "CBT Target",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Catastrophizing ('Assuming the Worst-Case Outcome')",
                subtitle = "Jumping from a minor setback to a worst-case scenario",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "All-or-Nothing Perfectionism & Harsh Self-Criticism",
                subtitle = "Feeling like a failure if a task or day isn't 100% flawless",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "Mind-Reading & Hyper-Responsibility",
                subtitle = "Assuming others are judging you or carrying everyone else's emotional load",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step2SecondaryQuestionLabel = "PREFERRED REGULATION MODALITY",
        step2SecondaryBadge = "Clinical Tool",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Breathwork & Somatic Vagal Down-Regulation",
                subtitle = "Physiological sighs, Jacobson muscle release, and sensory grounding",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "Structured Written CBT Thought Records",
                subtitle = "Evidence testing, distortion labeling, and cognitive reframing",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "ACT Mindfulness & Defusion ('Leaves on a Stream')",
                subtitle = "Unhooking from intrusive thoughts without arguing with them",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step2InsightNote = "Stanford neuroscience shows 5 minutes of cyclic physiological sighing lowers autonomic anxiety faster than passive meditation.",
        step3Title = "Daily Regulation Cadence",
        step3Subtitle = "Short, consistent daily neural regulation rewires amygdala reactivity far more effectively than occasional long sessions.",
        step3MinutesLabel = "DAILY CBT & SOMATIC PRACTICE WINDOW",
        step3MinuteChoices = listOf(5, 8, 10, 15, 20),
        step3DaysLabel = "REGULATION DAYS PER WEEK",
        step3DayChoices = listOf(4, 5, 6, 7),
        showEquipmentSelector = false
    )

    override fun buildDiagnosticSummary(
        preferences: ProgramPreferences,
        step1PrimaryIndex: Int,
        step1SecondaryIndex: Int,
        step2PrimaryIndex: Int,
        step2SecondaryIndex: Int
    ): ProgramDiagnosticSummary {
        val primaryManifestation = onboarding.step1PrimaryOptions
            .getOrNull(step1PrimaryIndex)?.title ?: "Racing Thoughts & Rumination"
        val primaryDistortion = onboarding.step2PrimaryOptions
            .getOrNull(step2PrimaryIndex)?.title?.substringBefore(" (") ?: "Catastrophizing"

        return ProgramDiagnosticSummary(
            step4Headline = "Clinical CBT & Vagal Regulation Plan",
            step4Subtitle = "Tailored 3-phase psychiatric self-regulation syllabus targeting autonomic arousal and cognitive distortions.",
            metrics = listOf(
                DiagnosticMetricSpec("VAGAL TONE", "Phase 1", "Days 1–10"),
                DiagnosticMetricSpec("CBT REFRAME", "Phase 2", "Days 11–20"),
                DiagnosticMetricSpec("RESILIENCE", "+42%", "30d target", isHighlighted = true)
            ),
            breakdownCardTitle = "THERAPEUTIC PROTOCOL ALLOCATION",
            breakdownBadge = "CBT + ACT + Polyvagal",
            segments = listOf(
                DiagnosticSegmentSpec(
                    label = "Somatic Brake",
                    valueText = "40%",
                    sublabel = "Physiological sighs & PMR",
                    weight = 40f,
                    colorHex = 0xFF60A5FAL
                ),
                DiagnosticSegmentSpec(
                    label = "CBT Reframe",
                    valueText = "35%",
                    sublabel = "3-column thought records",
                    weight = 35f,
                    colorHex = 0xFFA78BFAL
                ),
                DiagnosticSegmentSpec(
                    label = "ACT Defusion",
                    valueText = "25%",
                    sublabel = "Sensory & worry container",
                    weight = 25f,
                    colorHex = 0xFF00DC82L
                )
            ),
            prescriptionTitle = "TARGETED CLINICAL FOCUS",
            prescriptionHeadline = "$primaryDistortion & $primaryManifestation",
            prescriptionDetail = "${preferences.availableDaysPerWeek} days/wk · ${preferences.availableMinutesPerDay} min/day guided vagal down-regulation & CBT worksheets"
        )
    }
}
