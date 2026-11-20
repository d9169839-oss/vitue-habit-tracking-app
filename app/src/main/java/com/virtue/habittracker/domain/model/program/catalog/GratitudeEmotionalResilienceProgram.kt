package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

object GratitudeEmotionalResilienceProgram : ProgramDefinition {
    override val accentHex: Long = 0xFFF472B6L
    override val isRecommended: Boolean = false
    override val highlightTags: List<String> = listOf(
        "Behavioral Activation",
        "Negativity-Bias Rewiring",
        "30-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "gratitude-30",
        version = 1,
        title = "Gratitude & Emotional Resilience",
        category = ProgramCategory.MINDFULNESS,
        description = "Positive-psychology & Behavioral Activation curriculum to rewire threat-focused negativity bias, restore mood, and build self-compassion.",
        durationDays = 30,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 8,
        isPremium = false,
        equipment = ProgramEquipment.NONE,
        safetyNote = "Skip any prompt that feels forced or uncomfortable; genuine reflection should never enforce toxic positivity.",
        phases = buildPhases(
            duration = 30,
            names = listOf(
                "Phase 1: Reticular Activating System (RAS) Micro-Scanning",
                "Phase 2: Behavioral Activation & Relational Appreciation",
                "Phase 3: Post-Challenge Growth & Self-Compassion"
            ),
            titles = listOf(
                "Specific Sensory & Micro-Win Log",
                "Causal Gratitude & Connection Action",
                "Self-Compassion & Resilience Reframe"
            ),
            instructions = listOf(
                "Train your attention away from default threat-scanning by documenting three highly specific, sensory-rich moments from the last 24 hours.",
                "Connect appreciation to personal agency and relationships by identifying why a good moment happened and expressing brief thanks to someone.",
                "Apply Kristin Neff's self-compassion triad (mindfulness, common humanity, self-kindness) to a recent setback or stressful expectation."
            ),
            minutes = listOf(6, 8, 10)
        )
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "Seligman's 'Three Good Things' & Causal Analysis",
            "Psychiatry exercise: write 3 specific positive moments from today and explicitly answer 'What role did my effort or environment play in making each happen?'"
        ),
        DailyProgramModule(
            "Savoring Walk & Sensory Amplification",
            "Psychiatry exercise: spend 5 minutes lingering on one pleasant sensory input (warm drink, evening breeze, music) to lengthen positive dopamine-serotonin decay."
        ),
        DailyProgramModule(
            "Micro-Appreciation Message (Relational Oxytocin)",
            "Psychiatry exercise: send a 2-sentence text or voice note thanking a friend, family member, or colleague for one specific thing they did recently."
        ),
        DailyProgramModule(
            "Self-Compassion Break (Replacing the Inner Critic)",
            "Psychiatry exercise: recall a recent mistake and write to yourself with the exact fairness and warmth you would offer a close friend in the same position."
        ),
        DailyProgramModule(
            "Subtraction Visualization ('Mental Contrasting')",
            "Psychiatry exercise: pick one everyday comfort you take for granted, vividly imagine its temporary absence for 60 seconds, then return to appreciation."
        ),
        DailyProgramModule(
            "Silver-Lining & Coping Strength Audit",
            "Psychiatry exercise: look at one frustrating moment from this week and identify one personal boundary, skill, or lesson it forced you to strengthen."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = false,
        step1Title = "Emotional Baseline & Negativity Bias",
        step1Subtitle = "The human brain evolved a 3:1 threat-detection bias. Let's calibrate your Behavioral Activation and gratitude curriculum.",
        step1PrimaryQuestionLabel = "WHAT EMOTIONAL PATTERN ARE YOU EXPERIENCING MOST?",
        step1PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Emotional Flatness / Anhedonia & Burnout",
                subtitle = "Days blur together; accomplishments feel fleeting and joy feels muted",
                badge = "SAVORING FOCUS",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "Harsh Inner Critic & Perfectionist Guilt",
                subtitle = "Fixating on the 5% that went wrong while discounting the 95% you handled well",
                badge = "SELF-COMPASSION",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "Social Disconnection & Relational Fatigue",
                subtitle = "Feeling isolated in daily stress and wanting deeper appreciation & connection",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step1SecondaryQuestionLabel = "HOW OFTEN DO YOU PAUSE TO ACKNOWLEDGE DAILY WINS?",
        step1SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Rarely — I Immediately Jump to the Next Problem",
                subtitle = "High threat-scanning RAS · Needs concrete micro-win anchoring"
            ),
            AssessmentOptionSpec(
                title = "Occasionally, But It Feels Generic or Forced",
                subtitle = "Needs Seligman causal analysis ('Why did this good thing happen?')"
            ),
            AssessmentOptionSpec(
                title = "Regularly — Looking for Deeper Emotional Resilience",
                subtitle = "Ready for mental contrasting and post-challenge silver-lining reframes"
            )
        ),
        step1InsightNote = "Seligman's landmark randomized study found 14 days of 'Three Good Things + Causal Analysis' measurably reduced depressive symptoms for 6 months.",
        step2Title = "Reflection Style & Daily Anchor",
        step2Subtitle = "Choose the reflection modality and daily moment that feels most authentic and sustainable for you.",
        step2PrimaryQuestionLabel = "PREFERRED REFLECTION & ACTIVATION STYLE",
        step2PrimaryBadge = "Neural Pathway",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Specific Micro-Journaling (3 Bullet Points)",
                subtitle = "Fast, concrete sensory details rather than long diary essays",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "Relational Appreciation & Outward Connection",
                subtitle = "Brief 2-sentence thank-you messages and recognizing shared effort",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "Self-Compassion & Inner-Critic De-Escalation",
                subtitle = "Replacing self-judgment with Kristin Neff's clinical compassion triad",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step2SecondaryQuestionLabel = "BEST DAILY REFLECTION ANCHOR",
        step2SecondaryBadge = "Cue Timing",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Evening Bedside Reflection (Before Sleep)",
                subtitle = "Primes positive memory consolidation during REM sleep"
            ),
            AssessmentOptionSpec(
                title = "Morning Coffee / Tea Intention Check-In",
                subtitle = "Tunes your Reticular Activating System before work begins"
            ),
            AssessmentOptionSpec(
                title = "End-of-Workday Transition Ritual",
                subtitle = "Closes work stress by logging 3 concrete wins before dinner"
            )
        ),
        step2InsightNote = "Specificity is the secret: noting 'the warmth of my 8am coffee' activates reward circuitry far more than writing 'my life'.",
        step3Title = "Resilience Practice Cadence",
        step3Subtitle = "Select your daily journaling duration and weekly consistency target.",
        step3MinutesLabel = "DAILY REFLECTION DURATION",
        step3MinuteChoices = listOf(5, 8, 10, 15),
        step3DaysLabel = "REFLECTION DAYS PER WEEK",
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
        val style = onboarding.step2PrimaryOptions.getOrNull(step2PrimaryIndex)?.title ?: "Specific Micro-Journaling"
        val anchor = onboarding.step2SecondaryOptions.getOrNull(step2SecondaryIndex)?.title ?: "Evening Bedside Reflection"

        return ProgramDiagnosticSummary(
            step4Headline = "30-Day Emotional Resilience Blueprint",
            step4Subtitle = "Structured Positive Psychology & Behavioral Activation syllabus designed to shift your attention from threat-scanning to grounded appreciation.",
            metrics = listOf(
                DiagnosticMetricSpec("MICRO-WINS", "${preferences.availableDaysPerWeek * 3}", "/week"),
                DiagnosticMetricSpec("DAILY TIME", "${preferences.availableMinutesPerDay}", "minutes"),
                DiagnosticMetricSpec("MOOD LIFT", "+35%", "30d clinical avg", isHighlighted = true)
            ),
            breakdownCardTitle = "POSITIVE PSYCHOLOGY PROTOCOL SPLIT",
            breakdownBadge = "Seligman + Neff Protocol",
            segments = listOf(
                DiagnosticSegmentSpec(
                    label = "Causal Wins",
                    valueText = "40%",
                    sublabel = "3 Good Things + agency",
                    weight = 40f,
                    colorHex = 0xFFF472B6L
                ),
                DiagnosticSegmentSpec(
                    label = "Compassion",
                    valueText = "35%",
                    sublabel = "Inner-critic reframing",
                    weight = 35f,
                    colorHex = 0xFFA78BFAL
                ),
                DiagnosticSegmentSpec(
                    label = "Connection",
                    valueText = "25%",
                    sublabel = "Savoring & gratitude notes",
                    weight = 25f,
                    colorHex = 0xFF00DC82L
                )
            ),
            prescriptionTitle = "YOUR DAILY REFLECTION ANCHOR",
            prescriptionHeadline = "$anchor ($style)",
            prescriptionDetail = "${preferences.availableDaysPerWeek} days/wk · ${preferences.availableMinutesPerDay} min guided prompts — zero toxic positivity"
        )
    }
}
