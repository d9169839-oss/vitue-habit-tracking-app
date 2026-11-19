package com.virtue.habittracker.domain.model.program

/**
 * Curated, versioned starter content. Fitness content is general wellness only and must receive
 * qualified review before production release. It does not prescribe calories or promise outcomes.
 */
object ProgramCatalog {
    private fun phases(
        duration: Int,
        names: List<String>,
        titles: List<String>,
        instructions: List<String>,
        minutes: List<Int>
    ): List<ProgramPhaseTemplate> {
        val phaseLength = duration / names.size
        return names.indices.map { index ->
            ProgramPhaseTemplate(
                number = index + 1,
                title = names[index],
                startDay = index * phaseLength + 1,
                endDay = if (index == names.lastIndex) duration else (index + 1) * phaseLength,
                focus = names[index],
                activityTitle = titles[index],
                instructions = instructions[index],
                minutes = minutes[index]
            )
        }
    }

    private val movementPhases = phases(
        30,
        listOf(
            "Phase 1: Joint Articulation & Baseline",
            "Phase 2: Thoracic & Hip Flow",
            "Phase 3: Longevity & Tissue Resilience"
        ),
        listOf(
            "Joint Articulation & Spine Decompression",
            "Thoracic Flow & Active Hip Mobility",
            "Full-Body Longevity Flow & Breathwork"
        ),
        listOf(
            "Controlled articular rotations (CARs) for neck, shoulders, hips, and ankles followed by brisk low-impact walking. Stop if you feel pain, dizziness, or unusual shortness of breath.",
            "Deep squat holds, thoracic spine rotations, cat-cow flows, and 15 minutes of conversational movement to restore joint range of motion.",
            "Integrated full-body mobility sequence, single-leg balance stability, and parasympathetic nasal breathing cooldown."
        ),
        listOf(15, 20, 25)
    )

    private val strengthPhases = phases(
        60,
        listOf(
            "Phase 1: Neuromuscular Foundation",
            "Phase 2: Progressive Volume Accumulation",
            "Phase 3: Peak Hypertrophy & Structural Density"
        ),
        listOf(
            "Foundational Compound Mechanics",
            "Hypertrophic Overload Session",
            "Structural Density & Tempo Strength"
        ),
        listOf(
            "Focus on controlled eccentric tempo (3s lowering) across primary push, pull, squat, and hinge patterns. Leave 2–3 reps in reserve (RIR) and prioritize joint alignment.",
            "Add 1–2 working sets per primary muscle group. Push working sets to 1–2 reps in reserve with 90-second recovery intervals between compound bouts.",
            "Peak tension phase combining slow-eccentric compound work, isometric pauses at peak contraction, and post-session mobility decompression."
        ),
        listOf(25, 35, 45)
    )

    private val reflectionPhases = phases(
        30,
        listOf(
            "Phase 1: Nervous System Down-Regulation",
            "Phase 2: Cognitive Clarity & Framing",
            "Phase 3: Autonomous Daily Ritual"
        ),
        listOf(
            "Box Breathing & Sensory Grounding",
            "Cognitive Reframing & Gratitude Log",
            "Evening Synthesis & Intention Setting"
        ),
        listOf(
            "Pause for 4-4-4-4 box breathing, relax jaw and shoulder tension, and observe your immediate surroundings without judgment.",
            "Write down one meaningful win from today, one stressor you can release, and why today's effort mattered.",
            "Review your energy and focus across the day, close open mental loops, and set one clear focal intention for tomorrow."
        ),
        listOf(5, 8, 10)
    )

    private val gratitudePhases = phases(
        30,
        listOf(
            "Phase 1: Micro-Appreciation Anchors",
            "Phase 2: Relational & Effort Recognition",
            "Phase 3: Perspective & Emotional Resilience"
        ),
        listOf(
            "Three Specific Micro-Gratitudes",
            "Appreciation & Effort Reflection",
            "Resilience & Perspective Journal"
        ),
        listOf(
            "Note three specific moments, sensory details, or small comforts from today rather than broad generalizations.",
            "Acknowledge one person or personal effort that supported your wellbeing today and reflect on its impact.",
            "Reflect on a recent challenge and identify one lesson, strength, or supportive factor that helped you navigate it."
        ),
        listOf(5, 8, 10)
    )

    private val groomingPhases = phases(
        30,
        listOf(
            "Phase 1: Frictionless Environment Setup",
            "Phase 2: Morning & Evening Rhythm",
            "Phase 3: Executive Presence & Refinement"
        ),
        listOf(
            "Essential Grooming & Prep Reset",
            "AM/PM Grooming & Wardrobe Staging",
            "Full Routine Audit & Signature Polish"
        ),
        listOf(
            "Organize your essential hygiene tools on a clear counter and complete your core dental, hair, and tidy-up checklist.",
            "Complete your daily grooming sequence and lay out tomorrow's outfit and essentials the night before to eliminate morning friction.",
            "Refine your weekly maintenance habits (nails, trim, fabric care) and streamline your daily ritual so it runs on autopilot."
        ),
        listOf(8, 10, 12)
    )

    private val staminaPhases = phases(
        60,
        listOf(
            "Phase 1: Zone 2 Aerobic Base",
            "Phase 2: Aerobic Threshold & Work Capacity",
            "Phase 3: Metabolic Conditioning & Recovery"
        ),
        listOf(
            "Zone 2 Conversational Cardio",
            "Sustained Tempo & Aerobic Threshold",
            "Interval Conditioning & Parasympathetic Cooldown"
        ),
        listOf(
            "Sustained low-impact cardio (brisk incline walk, cycle, or jog) at a pace where you can speak full sentences comfortably through nasal breathing.",
            "Extend continuous aerobic duration and weave in 2–3 moderate tempo surges while keeping heart-rate recovery controlled.",
            "Alternate structured higher-output aerobic intervals with active recovery bouts, finishing with a 5-minute heart-rate down-regulation cooldown."
        ),
        listOf(20, 30, 35)
    )

    val all: List<ProgramTemplate> = listOf(
        ProgramTemplate(
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
            phases = movementPhases
        ),
        ProgramTemplate(
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
            phases = strengthPhases
        ),
        ProgramTemplate(
            id = "stamina-60",
            version = 1,
            title = "Aerobic Engine & Vitality",
            category = ProgramCategory.FITNESS,
            description = "Cardiovascular resilience, metabolic conditioning, and sustained baseline energy for demanding workdays.",
            durationDays = 60,
            difficulty = ProgramDifficulty.BEGINNER,
            defaultMinutesPerDay = 25,
            isPremium = false,
            equipment = ProgramEquipment.NONE,
            safetyNote = "Start at a comfortable pace. Seek professional advice if you have health concerns or are returning after injury.",
            phases = staminaPhases
        ),
        ProgramTemplate(
            id = "weight-habits-90",
            version = 1,
            title = "Calm Recomposition",
            category = ProgramCategory.FITNESS,
            description = "Simultaneous fat loss and muscle retention with precise caloric partitioning, NEAT movement, and steady volume.",
            durationDays = 90,
            difficulty = ProgramDifficulty.BEGINNER,
            defaultMinutesPerDay = 25,
            isPremium = false,
            equipment = ProgramEquipment.NONE,
            safetyNote = "This program does not prescribe crash diets or rapid weight change. Seek qualified guidance for medical or eating-related concerns.",
            phases = phases(
                90,
                listOf(
                    "Phase 1: Metabolic Baseline & Satiety",
                    "Phase 2: Resistance Retention & NEAT",
                    "Phase 3: Sustainable Recomp Maintenance"
                ),
                listOf(
                    "Resistance Primer & Protein-Anchor Check",
                    "Full-Body Retention Session & NEAT Walk",
                    "Metabolic Consolidation & Recovery Audit"
                ),
                listOf(
                    "Complete a 20-minute compound resistance or brisk movement session and anchor meals around whole-food protein and hydration.",
                    "Pair full-body muscle-retention resistance movements with an easy post-meal walk to elevate daily non-exercise energy expenditure.",
                    "Maintain strength training density while auditing sleep quality, recovery markers, and sustainable meal rhythm."
                ),
                listOf(20, 25, 30)
            )
        ),
        ProgramTemplate(
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
            phases = groomingPhases
        ),
        ProgramTemplate(
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
            phases = phases(
                30,
                listOf(
                    "Phase 1: Cleanse, Moisturize & Protect",
                    "Phase 2: Barrier Consistency & Tracking",
                    "Phase 3: Minimalist Long-Term Protocol"
                ),
                listOf(
                    "Core AM SPF & PM Cleanse Ritual",
                    "Skin Barrier & Hydration Check-In",
                    "Routine Simplification & Tolerance Review"
                ),
                listOf(
                    "Use gentle cleanser, moisturizer, and morning broad-spectrum SPF that you already tolerate. Avoid adding multiple new actives at once.",
                    "Complete your AM and PM routine at the same time each day and note dryness, oiliness, or irritation.",
                    "Keep the products that support calm, hydrated skin and eliminate redundant steps. This program does not diagnose skin conditions."
                ),
                listOf(5, 5, 5)
            )
        ),
        ProgramTemplate(
            id = "mindful-30",
            version = 1,
            title = "Mindfulness Foundations",
            category = ProgramCategory.MINDFULNESS,
            description = "Practice structured breath down-regulation, attentional control, and evening decompression.",
            durationDays = 30,
            difficulty = ProgramDifficulty.BEGINNER,
            defaultMinutesPerDay = 8,
            isPremium = false,
            equipment = ProgramEquipment.NONE,
            safetyNote = "You can stop or choose an eyes-open grounding exercise at any time. This is not mental health treatment.",
            phases = reflectionPhases
        ),
        ProgramTemplate(
            id = "gratitude-30",
            version = 1,
            title = "Gratitude and Reflection",
            category = ProgramCategory.MINDFULNESS,
            description = "Cultivate cognitive resilience and appreciation through specific, grounded daily journaling prompts.",
            durationDays = 30,
            difficulty = ProgramDifficulty.BEGINNER,
            defaultMinutesPerDay = 8,
            isPremium = false,
            equipment = ProgramEquipment.NONE,
            safetyNote = "Skip any prompt that feels uncomfortable; reflection should be voluntary.",
            phases = gratitudePhases
        ),

        // Built-in template 1: a gentle, equipment-free general-wellness plan.
        ProgramTemplate(
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
            phases = phases(
                60,
                listOf("Build a baseline", "Create a steady rhythm", "Make it sustainable"),
                listOf("Comfortable movement check-in", "Movement and mobility", "Recovery and routine review"),
                listOf(
                    "Choose 5–10 minutes of comfortable walking or another familiar low-impact activity. Notice how you feel; rest whenever needed.",
                    "Choose comfortable low-impact movement, add gentle mobility if it feels good, and keep the effort conversational. Include easier days.",
                    "Combine familiar movement with a short cooldown. Review sleep and hydration habits without strict targets or guilt."
                ),
                listOf(10, 15, 15)
            ),
            artwork = ProgramArtwork.FITNESS_WELLNESS
        ),

        // Built-in template 2: non-clinical personal development through small repeatable habits.
        ProgramTemplate(
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
            phases = phases(
                30,
                listOf("Know your starting point", "Practice intentionally", "Reflect and continue"),
                listOf("Two-minute daily check-in", "Focused work or learning session", "Weekly reflection and next step"),
                listOf(
                    "Write one priority for today, one thing that may distract you, and one small action you can realistically complete.",
                    "Try one focused 10-minute session for a task you choose. Put away one distraction and take a break afterward.",
                    "Reflect on one thing you learned, one action you are proud of, and one small adjustment for next week. Keep the plan realistic."
                ),
                listOf(5, 10, 10)
            ),
            artwork = ProgramArtwork.PERSONAL_DEVELOPMENT
        )
    )

    fun find(id: String): ProgramTemplate? = all.firstOrNull { it.id == id }
}
