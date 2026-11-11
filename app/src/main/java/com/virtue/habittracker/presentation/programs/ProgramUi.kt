package com.virtue.habittracker.presentation.programs

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramArtwork
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

internal val ProgramViolet = Color(0xFFA78BFA)
internal val ProgramLavender = Color(0xFFC4B5FD)
internal val ProgramCard = Color(0xFF211B30)
internal val ProgramMuted = Color(0xFFAAA2BB)

/**
 * Cohesive card gradient matching the main progress card on the Home screen.
 */
internal val ProgramCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF393344),
        Color(0xFF302A3A),
        Color(0xFF24202D)
    ),
    start = Offset(0f, 0f),
    end = Offset(900f, 900f)
)

internal fun ProgramCategory.displayName(): String = when (this) {
    ProgramCategory.FITNESS -> "FITNESS"
    ProgramCategory.SELF_GROOMING -> "SELF-GROOMING"
    ProgramCategory.MINDFULNESS -> "MINDFULNESS"
    ProgramCategory.PERSONAL_DEVELOPMENT -> "PERSONAL DEVELOPMENT"
}

internal fun ProgramDifficulty.displayName(): String = when (this) {
    ProgramDifficulty.BEGINNER -> "Beginner"
    ProgramDifficulty.INTERMEDIATE -> "Intermediate"
}

internal fun ProgramExperience.displayName(): String = when (this) {
    ProgramExperience.BEGINNER -> "Beginner"
    ProgramExperience.SOME_EXPERIENCE -> "Some experience"
    ProgramExperience.EXPERIENCED -> "Experienced"
}

internal fun ProgramEquipment.displayName(): String = when (this) {
    ProgramEquipment.NONE -> "No equipment"
    ProgramEquipment.HOME_BASIC -> "Home equipment"
    ProgramEquipment.GYM -> "Gym"
}

internal fun WorkActivityLevel.displayName(): String = when (this) {
    WorkActivityLevel.MOSTLY_SEATED -> "Mostly seated"
    WorkActivityLevel.MIXED -> "Mixed"
    WorkActivityLevel.PHYSICALLY_ACTIVE -> "Physically active"
}


/** Maps a domain artwork identifier to a bundled Android drawable at the UI boundary. */
internal fun ProgramTemplate.artworkResource(): Int = when (artwork) {
    ProgramArtwork.FITNESS_WELLNESS -> com.virtue.habittracker.R.drawable.program_fitness_wellness
    ProgramArtwork.PERSONAL_DEVELOPMENT -> com.virtue.habittracker.R.drawable.program_personal_development
    ProgramArtwork.DEFAULT -> when (category) {
        ProgramCategory.FITNESS -> com.virtue.habittracker.R.drawable.program_fitness_wellness
        ProgramCategory.SELF_GROOMING -> com.virtue.habittracker.R.drawable.program_personal_development
        ProgramCategory.MINDFULNESS -> com.virtue.habittracker.R.drawable.program_personal_development
        ProgramCategory.PERSONAL_DEVELOPMENT -> com.virtue.habittracker.R.drawable.program_personal_development
    }
}
