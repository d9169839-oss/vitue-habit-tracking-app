package com.virtue.habittracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HabitDaySummaryTest {
    @Test
    fun summaryCountsAllThreeDailyStates() {
        val entries = listOf(
            entry(HabitDayStatus.COMPLETED),
            entry(HabitDayStatus.COMPLETED),
            entry(HabitDayStatus.NOT_COMPLETED),
            entry(HabitDayStatus.UNRECORDED)
        )

        val summary = summarizeHabitDay(dateEpochDay = 20L, entries = entries)

        assertEquals(4, summary.activeHabitCount)
        assertEquals(2, summary.completedCount)
        assertEquals(1, summary.notCompletedCount)
        assertEquals(1, summary.unrecordedCount)
        assertEquals(50, summary.completionRatePercent)
    }

    @Test
    fun emptyDayHasZeroCountsAndZeroCompletionRate() {
        val summary = summarizeHabitDay(dateEpochDay = 20L, entries = emptyList())

        assertEquals(0, summary.activeHabitCount)
        assertEquals(0, summary.completionRatePercent)
    }

    private fun entry(status: HabitDayStatus) = HabitDayEntry(
        habit = Habit(
            id = status.name,
            title = status.name,
            description = "",
            createdEpochDay = 1L,
            inactiveFromEpochDay = null
        ),
        status = status
    )
}
