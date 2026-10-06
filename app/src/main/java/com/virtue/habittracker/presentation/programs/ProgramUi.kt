package com.virtue.habittracker.presentation.programs

import androidx.compose.ui.graphics.Color
import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.WorkActivityLevel

internal val ProgramViolet = Color(0xFFA78BFA)
internal val ProgramLavender = Color(0xFFC4B5FD)
internal val ProgramCard = Color(0xFF211B30)
internal val ProgramMuted = Color(0xFFAAA2BB)

internal fun ProgramCategory.displayName(): String = when (this) {
    ProgramCategory.FITNESS -> "FITNESS"
    ProgramCategory.SELF_GROOMING -> "SELF-GROOMING"
    ProgramCategory.MINDFULNESS -> "MINDFULNESS"
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
