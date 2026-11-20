package com.virtue.habittracker.domain.model.program.catalog

import com.virtue.habittracker.domain.model.program.ProgramArtwork
import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

object PersonalDevelopmentProgram : ProgramDefinition {
    override val accentHex: Long = 0xFFF59E0BL
    override val isRecommended: Boolean = true
    override val highlightTags: List<String> = listOf(
        "Executive Function",
        "Anti-Procrastination",
        "30-day cycle"
    )

    override val template: ProgramTemplate = ProgramTemplate(
        id = "personal-development-30",
        version = 1,
        title = "Personal Development",
        category = ProgramCategory.PERSONAL_DEVELOPMENT,
        description = "Build self-awareness, focus, planning, and confidence through small daily actions that fit around work, study, and family life.",
        durationDays = 30,
        difficulty = ProgramDifficulty.BEGINNER,
        defaultMinutesPerDay = 10,
        isPremium = false,
        equipment = ProgramEquipment.NONE,
        safetyNote = "This is an educational habit-building program, not therapy or a guarantee of personal or professional outcomes. Skip prompts that do not suit you and adapt the plan to your circumstances.",
        phases = buildPhases(
            duration = 30,
            names = listOf("Know your starting point", "Practice intentionally", "Reflect and continue"),
            titles = listOf("Two-minute daily check-in", "Focused work or learning session", "Weekly reflection and next step"),
            instructions = listOf(
                "Write one priority for today, one thing that may distract you, and one small action you can realistically complete.",
                "Try one focused 10-minute session for a task you choose. Put away one distraction and take a break afterward.",
                "Reflect on one thing you learned, one action you are proud of, and one small adjustment for next week. Keep the plan realistic."
            ),
            minutes = listOf(5, 10, 10)
        ),
        artwork = ProgramArtwork.PERSONAL_DEVELOPMENT
    )

    override val dailyModules: List<DailyProgramModule> = listOf(
        DailyProgramModule(
            "Implementation Intentions (If-Then Planning)",
            "Behavioral science focus: define your #1 priority using Peter Gollwitzer's formula: 'If it is [Time/Location], then I will start [Specific 5-Minute First Step].'"
        ),
        DailyProgramModule(
            "The 5-Minute Friction-Breaker Sprint",
            "Behavioral science focus: procrastination is an emotion-regulation block, not laziness. Commit to working on your avoided task for just 5 minutes with permission to stop after."
        ),
        DailyProgramModule(
            "Single-Tasking & Attention Residue Audit",
            "Behavioral science focus: close all background browser tabs, put your phone in another room, and complete one uninterrupted cognitive block without context-switching."
        ),
        DailyProgramModule(
            "Eisenhower Triage & Boundary Setting",
            "Behavioral science focus: separate today's urgent noise from your 1–2 high-leverage important outcomes, and decline or defer one low-value commitment."
        ),
        DailyProgramModule(
            "Identity-Based Habit Reinforcement",
            "Behavioral science focus: log one small completed promise to yourself today as a vote for the identity you are building, regardless of external outcomes."
        ),
        DailyProgramModule(
            "Shutdown Ritual & Zeigarnik Loop Closure",
            "Behavioral science focus: review today's progress without self-criticism, write down tomorrow's exact starting step, and declare your workday mentally complete."
        )
    )

    override val onboarding: ProgramOnboardingSpec = ProgramOnboardingSpec(
        usesBiometricPhysicalStep1 = false,
        step1Title = "Executive Function & Focus Intake",
        step1Subtitle = "Procrastination is rarely a time-management issue—it is an initiation and emotion-regulation bottleneck. Let's identify your friction point.",
        step1PrimaryQuestionLabel = "PRIMARY EXECUTIVE FUNCTION BOTTLENECK",
        step1PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Task Initiation Paralysis (Putting Off Big Tasks)",
                subtitle = "Knowing exactly what to do, yet feeling an invisible wall when trying to start",
                badge = "5-MIN SPRINT",
                mappedExperience = ProgramExperience.BEGINNER
            ),
            AssessmentOptionSpec(
                title = "Context-Switching & Unfinished Open Loops",
                subtitle = "Starting 5 things at once, losing focus mid-way, and carrying mental clutter",
                badge = "DEEP WORK",
                mappedExperience = ProgramExperience.SOME_EXPERIENCE
            ),
            AssessmentOptionSpec(
                title = "Over-Planning Without Consistent Follow-Through",
                subtitle = "Ambitious systems that collapse after 4 days due to perfectionist overload",
                mappedExperience = ProgramExperience.EXPERIENCED
            )
        ),
        step1SecondaryQuestionLabel = "PRIMARY 30-DAY PERSONAL GROWTH ARENA",
        step1SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Career, Deep Work & High-Leverage Output",
                subtitle = "Shipping meaningful work without distraction or procrastination"
            ),
            AssessmentOptionSpec(
                title = "Academic Study, Skill Mastery & Reading",
                subtitle = "Building daily deliberate practice and retention blocks"
            ),
            AssessmentOptionSpec(
                title = "Life Organization, Discipline & Self-Trust",
                subtitle = "Keeping small daily promises to yourself to rebuild confidence"
            )
        ),
        step1InsightNote = "Gollwitzer's meta-analysis of 94 studies showed 'If-Then' implementation intentions nearly triple goal completion rates.",
        step2Title = "Cognitive Peak & Planning Style",
        step2Subtitle = "Align your daily focus sprint with your brain's natural ultradian alertness peak.",
        step2PrimaryQuestionLabel = "PEAK COGNITIVE CLARITY WINDOW",
        step2PrimaryBadge = "Ultradian Peak",
        step2PrimaryOptions = listOf(
            AssessmentOptionSpec(
                title = "Early Morning Deep Work (Before Messages/Email)",
                subtitle = "Prefrontal cortex is fresh with zero attention residue"
            ),
            AssessmentOptionSpec(
                title = "Mid-Morning / Late-Morning Flow Block",
                subtitle = "After initial warm-up, tackling the #1 priority before lunch"
            ),
            AssessmentOptionSpec(
                title = "Late Afternoon / Evening Quiet Window",
                subtitle = "After external interruptions and meetings quiet down"
            )
        ),
        step2SecondaryQuestionLabel = "PREFERRED EXECUTION FRAMEWORK",
        step2SecondaryBadge = "Behavioral Tool",
        step2SecondaryOptions = listOf(
            AssessmentOptionSpec(
                title = "5-Minute Friction-Breaker Sprints",
                subtitle = "Lowers the activation threshold so starting feels effortless",
                mappedWorkLevel = WorkActivityLevel.MOSTLY_SEATED
            ),
            AssessmentOptionSpec(
                title = "If-Then Implementation Intentions + Single-Tasking",
                subtitle = "Pre-commits time, location, and exact first action",
                mappedWorkLevel = WorkActivityLevel.MIXED
            ),
            AssessmentOptionSpec(
                title = "Eisenhower Triage + Zeigarnik Shutdown Ritual",
                subtitle = "Eliminates busywork and closes open mental loops each evening",
                mappedWorkLevel = WorkActivityLevel.PHYSICALLY_ACTIVE
            )
        ),
        step2InsightNote = "Once you cross the 5-minute task-initiation threshold, the Zeigarnik effect naturally pulls your brain to keep going.",
        step3Title = "Daily Focus Commitment",
        step3Subtitle = "Choose a realistic daily focus block duration and weekly cadence you can hit with 90%+ consistency.",
        step3MinutesLabel = "DAILY FOCUSED SPRINT DURATION",
        step3MinuteChoices = listOf(5, 10, 15, 25, 45),
        step3DaysLabel = "EXECUTION DAYS PER WEEK",
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
        val totalDeepBlocks = preferences.availableDaysPerWeek * 4
        val monthlyDeepMinutes = totalDeepBlocks * preferences.availableMinutesPerDay
        val bottleneck = onboarding.step1PrimaryOptions
            .getOrNull(step1PrimaryIndex)?.title?.substringBefore(" (") ?: "Task Initiation"

        return ProgramDiagnosticSummary(
            step4Headline = "30-Day Executive Function Blueprint",
            step4Subtitle = "Behavioral science syllabus combining If-Then planning, 5-minute initiation sprints, and Zeigarnik shutdown rituals.",
            metrics = listOf(
                DiagnosticMetricSpec("DEEP BLOCKS", "$totalDeepBlocks", "/month", isHighlighted = true),
                DiagnosticMetricSpec("FOCUS TIME", "$monthlyDeepMinutes", "min/30d"),
                DiagnosticMetricSpec("INITIATION", "5-Min", "rule")
            ),
            breakdownCardTitle = "EXECUTIVE FUNCTION PROTOCOL SPLIT",
            breakdownBadge = "Gollwitzer + Zeigarnik Method",
            segments = listOf(
                DiagnosticSegmentSpec(
                    label = "Initiation",
                    valueText = "40%",
                    sublabel = "If-Then & 5-min sprints",
                    weight = 40f,
                    colorHex = 0xFFF59E0BL
                ),
                DiagnosticSegmentSpec(
                    label = "Deep Focus",
                    valueText = "35%",
                    sublabel = "Zero-residue single-tasking",
                    weight = 35f,
                    colorHex = 0xFFA78BFAL
                ),
                DiagnosticSegmentSpec(
                    label = "Loop Closure",
                    valueText = "25%",
                    sublabel = "Eisenhower & evening shutdown",
                    weight = 25f,
                    colorHex = 0xFF00DC82L
                )
            ),
            prescriptionTitle = "TARGETED BOTTLENECK INTERVENTION",
            prescriptionHeadline = bottleneck,
            prescriptionDetail = "${preferences.availableDaysPerWeek} days/wk · ${preferences.availableMinutesPerDay} min distraction-free execution sprint + daily priority lock"
        )
    }
}
