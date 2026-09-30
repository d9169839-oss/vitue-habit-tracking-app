package com.virtue.habittracker.domain.model

/**
 * A calendar-day summary calculated from active habits and their check-ins.
 *
 * This is a read model, not a second source of truth. Recalculate it from habits/check-ins
 * so summary counts cannot drift away from the actual history.
 */
data class HabitDaySummary(
    val dateEpochDay: Long,
    val activeHabitCount: Int,
    val completedCount: Int,
    val notCompletedCount: Int,
    val unrecordedCount: Int
) {
    val completionRatePercent: Int
        get() = if (activeHabitCount == 0) 0
        else (completedCount * 100f / activeHabitCount).toInt()
}

/** Keep summary calculation pure so it is easy to test and reuse on the History screen. */
fun summarizeHabitDay(dateEpochDay: Long, entries: List<HabitDayEntry>): HabitDaySummary =
    HabitDaySummary(
        dateEpochDay = dateEpochDay,
        activeHabitCount = entries.size,
        completedCount = entries.count { it.status == HabitDayStatus.COMPLETED },
        notCompletedCount = entries.count { it.status == HabitDayStatus.NOT_COMPLETED },
        unrecordedCount = entries.count { it.status == HabitDayStatus.UNRECORDED }
    )
