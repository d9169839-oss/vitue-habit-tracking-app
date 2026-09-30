package com.virtue.habittracker.domain.model.program

/**
 * Curated, versioned starter content. Fitness content is general wellness only and must receive
 * qualified review before production release. It does not prescribe calories or promise outcomes.
 */
object ProgramCatalog {
    private fun phases(duration: Int, names: List<String>, titles: List<String>, instructions: List<String>,
                       minutes: List<Int>): List<ProgramPhaseTemplate> {
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
        30, listOf("Start gently", "Build consistency", "Keep showing up"),
        listOf("Comfortable movement", "Brisk walk or easy movement", "Movement and cooldown"),
        listOf(
            "Choose a comfortable pace. Stop if you feel pain, dizziness, or unusual shortness of breath.",
            "Choose walking or another familiar low-impact activity. Keep the pace conversational.",
            "Move at a comfortable pace, then take a few minutes to cool down and notice how you feel."
        ), listOf(10, 15, 20)
    )
    private val strengthPhases = phases(
        60, listOf("Learn the routine", "Practice steadily", "Consolidate"),
        listOf("Gentle strength basics", "Bodyweight practice", "Strength and mobility"),
        listOf(
            "Try comfortable sit-to-stands, wall push-ups, and gentle mobility. Use a stable support and rest as needed.",
            "Practice controlled bodyweight movements within a comfortable range. Prioritize technique over repetitions.",
            "Repeat familiar movements at an easy effort, with rest between sets. Do not train through pain."
        ), listOf(12, 18, 20)
    )
    private val reflectionPhases = phases(
        30, listOf("Notice", "Reflect", "Make it a ritual"),
        listOf("One-minute pause", "Gratitude journal", "Mindful reflection"),
        listOf(
            "Pause, breathe naturally, and notice one thing around you without judging it.",
            "Write down one thing you appreciated today and why it mattered to you.",
            "Take a quiet moment to reflect on your day and one small intention for tomorrow."
        ), listOf(5, 8, 10)
    )
    private val groomingPhases = phases(
        30, listOf("Make it easy", "Build consistency", "Personalize"),
        listOf("Simple hygiene checklist", "Prepare tomorrow", "Review your routine"),
        listOf(
            "Choose a simple, comfortable hygiene routine and prepare the items you need.",
            "Follow your personal hygiene routine and set out what you need for tomorrow.",
            "Keep what works for you, remove unnecessary steps, and make the routine easy to repeat."
        ), listOf(5, 8, 10)
    )
    private val staminaPhases = phases(
        60, listOf("Find your baseline", "Build a rhythm", "Stay consistent"),
        listOf("Easy paced movement", "Steady movement", "Movement with recovery"),
        listOf(
            "Choose an easy activity such as walking. You should be able to talk comfortably.",
            "If comfortable, add a little time to familiar low-impact movement. Keep at least one easier day.",
            "Choose a sustainable pace and finish with a gentle cooldown. Increase duration gradually, not all at once."
        ), listOf(10, 15, 20)
    )

    val all: List<ProgramTemplate> = listOf(
        ProgramTemplate("movement-30", 1, "Beginner Movement Foundations", ProgramCategory.FITNESS,
            "Build a gentle movement routine with short, adaptable sessions.", 30, ProgramDifficulty.BEGINNER, 10, false,
            ProgramEquipment.NONE, "General wellness only. Stop for pain, dizziness, chest discomfort, or unusual breathlessness.",
            movementPhases),
        ProgramTemplate("home-strength-60", 1, "Home Strength Foundations", ProgramCategory.FITNESS,
            "Practice simple bodyweight movements at a comfortable pace.", 60, ProgramDifficulty.BEGINNER, 15, true,
            ProgramEquipment.HOME_BASIC, "Use stable supports and comfortable ranges of motion. This is not individualized medical advice.",
            strengthPhases),
        ProgramTemplate("stamina-60", 1, "Everyday Stamina", ProgramCategory.FITNESS,
            "Build consistency with gradual, low-impact movement and recovery.", 60, ProgramDifficulty.BEGINNER, 15, true,
            ProgramEquipment.NONE, "Start at a comfortable pace. Seek professional advice if you have health concerns or are returning after injury.",
            staminaPhases),
        ProgramTemplate("weight-habits-90", 1, "Sustainable Wellness Habits", ProgramCategory.FITNESS,
            "Build routines around movement, regular meals, hydration, sleep, and reflection without crash diets or promised weight changes.", 90,
            ProgramDifficulty.BEGINNER, 15, true, ProgramEquipment.NONE,
            "This program does not prescribe calories, weight targets, or rapid weight change. Seek qualified guidance for medical or eating-related concerns.",
            phases(90, listOf("Establish", "Practice", "Maintain"),
                listOf("Notice one supportive habit", "Movement and routine check-in", "Reflect and reset"),
                listOf("Choose one small, non-restrictive wellness action that fits your day.",
                    "Do comfortable movement or another supportive routine. Keep meals regular and avoid punitive rules.",
                    "Reflect on what feels sustainable. Focus on consistency and wellbeing rather than a number on the scale."),
                listOf(8, 12, 12))),
        ProgramTemplate("grooming-30", 1, "Everyday Grooming Routine", ProgramCategory.SELF_GROOMING,
            "Create a simple personal hygiene and preparation routine.", 30, ProgramDifficulty.BEGINNER, 5, false,
            ProgramEquipment.NONE, "Adapt the routine to your needs, skin sensitivity, culture, and preferences.", groomingPhases),
        ProgramTemplate("skincare-30", 1, "Simple Skincare Consistency", ProgramCategory.SELF_GROOMING,
            "Build a minimal routine and observe how your skin responds.", 30, ProgramDifficulty.BEGINNER, 5, true,
            ProgramEquipment.NONE, "Avoid introducing many products at once. Stop products that cause irritation and seek clinical advice for persistent concerns.",
            phases(30, listOf("Keep it simple", "Stay consistent", "Review gently"),
                listOf("Basic routine check", "Consistency check", "Review what works"),
                listOf("Use products you already tolerate and follow their labels. A simple routine is enough to begin.",
                    "Repeat your established routine; do not add products just to complete the program.",
                    "Notice irritation or comfort and simplify if needed. This program does not diagnose skin conditions."),
                listOf(5, 5, 5))),
        ProgramTemplate("mindful-30", 1, "Mindfulness Foundations", ProgramCategory.MINDFULNESS,
            "Practice short pauses, breathing awareness, and reflection.", 30, ProgramDifficulty.BEGINNER, 5, false,
            ProgramEquipment.NONE, "You can stop or choose an eyes-open grounding exercise at any time. This is not mental health treatment.", reflectionPhases),
        ProgramTemplate("gratitude-30", 1, "Gratitude and Reflection", ProgramCategory.MINDFULNESS,
            "Create a gentle daily reflection practice without forcing positive feelings.", 30, ProgramDifficulty.BEGINNER, 5, true,
            ProgramEquipment.NONE, "Skip any prompt that feels uncomfortable; reflection should be voluntary.", reflectionPhases)
    )

    fun find(id: String): ProgramTemplate? = all.firstOrNull { it.id == id }
}
