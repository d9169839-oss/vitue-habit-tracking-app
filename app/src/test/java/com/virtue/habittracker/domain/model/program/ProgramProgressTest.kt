package com.virtue.habittracker.domain.model.program

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgramProgressTest {

    private val enrollment = ProgramEnrollment(
        id = "enrollment-1",
        templateId = "starter",
        templateVersion = 1,
        titleSnapshot = "Starter program",
        category = ProgramCategory.FITNESS,
        durationDays = 7,
        startEpochDay = 20_000L,
        status = ProgramStatus.ACTIVE,
        preferences = ProgramPreferences(),
        isPremium = false,
        createdAtMillis = 1L,
        updatedAtMillis = 1L
    )

    @Test
    fun restDaysDoNotReduceProgramCompletionPercentage() {
        val activities = listOf(
            activity("day-1", minutes = 20, status = ProgramActivityStatus.COMPLETED),
            activity("day-2", minutes = 20, status = ProgramActivityStatus.PENDING),
            activity("day-3", minutes = 0, status = ProgramActivityStatus.PENDING),
            activity("day-4", minutes = 0, status = ProgramActivityStatus.PENDING)
        )

        val progress = ProgramProgress(enrollment, activities)

        assertEquals(2, progress.totalCount)
        assertEquals(1, progress.completedCount)
        assertEquals(50, progress.progressPercent)
    }

    @Test
    fun programWithOnlyRecoveryDaysHasZeroPercentWithoutDivisionByZero() {
        val progress = ProgramProgress(
            enrollment,
            listOf(
                activity("rest-1", minutes = 0, status = ProgramActivityStatus.PENDING),
                activity("rest-2", minutes = 0, status = ProgramActivityStatus.PENDING)
            )
        )

        assertEquals(0, progress.totalCount)
        assertEquals(0, progress.progressPercent)
    }

    private fun activity(
        id: String,
        minutes: Int,
        status: ProgramActivityStatus
    ) = ScheduledProgramActivity(
        id = id,
        enrollmentId = enrollment.id,
        dayIndex = id.filter(Char::isDigit).toIntOrNull() ?: 1,
        epochDay = enrollment.startEpochDay,
        phaseTitle = "Foundation",
        title = if (minutes == 0) "Rest and reset" else "Workout",
        instructions = "",
        estimatedMinutes = minutes,
        status = status,
        updatedAtMillis = 1L
    )
}
