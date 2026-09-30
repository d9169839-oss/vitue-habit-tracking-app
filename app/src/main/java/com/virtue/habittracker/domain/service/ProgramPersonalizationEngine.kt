package com.virtue.habittracker.domain.service

import com.virtue.habittracker.domain.model.program.ProgramEnrollment
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.ScheduledProgramActivity
import com.virtue.habittracker.domain.model.program.ProgramActivityStatus
import java.util.UUID

/** Pure deterministic schedule builder: no Android, Room, Firebase, or network dependency. */
class ProgramPersonalizationEngine {
    fun buildSchedule(
        template: ProgramTemplate,
        enrollmentId: String,
        startEpochDay: Long,
        preferences: ProgramPreferences
    ): List<ScheduledProgramActivity> {
        val availableMinutes = preferences.availableMinutesPerDay.coerceIn(5, 90)
        val daysPerWeek = preferences.availableDaysPerWeek.coerceIn(1, 7)
        return (1..template.durationDays).map { day ->
            val phase = template.phases.lastOrNull { day in it.startDay..it.endDay }
                ?: template.phases.last()
            val dayOfWeek = (day - 1) % 7
            val isRestDay = dayOfWeek >= daysPerWeek
            val baseMinutes = minOf(phase.minutes, availableMinutes)
            val equipmentMismatch = template.equipment == com.virtue.habittracker.domain.model.program.ProgramEquipment.GYM &&
                preferences.equipment != com.virtue.habittracker.domain.model.program.ProgramEquipment.GYM
            val title = when {
                isRestDay -> "Rest and reset"
                equipmentMismatch -> "Choose a no-equipment alternative"
                else -> phase.activityTitle
            }
            val instructions = when {
                isRestDay -> "A planned easier day. Rest, stretch gently if comfortable, or simply resume tomorrow."
                equipmentMismatch -> "This template expects gym equipment. For now, choose comfortable walking or mobility instead; do not attempt unfamiliar weighted movements."
                else -> phase.instructions
            }
            ScheduledProgramActivity(
                id = UUID.nameUUIDFromBytes("$enrollmentId:$day".toByteArray()).toString(),
                enrollmentId = enrollmentId,
                dayIndex = day,
                epochDay = startEpochDay + day - 1L,
                phaseTitle = phase.title,
                title = title,
                instructions = instructions,
                estimatedMinutes = if (isRestDay) 0 else baseMinutes,
                status = ProgramActivityStatus.PENDING,
                updatedAtMillis = System.currentTimeMillis()
            )
        }
    }
}
