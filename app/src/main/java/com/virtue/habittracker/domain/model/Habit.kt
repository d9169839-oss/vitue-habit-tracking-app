package com.virtue.habittracker.domain.model

data class Habit(
    val id: String,
    val title: String,
    val description: String,
    val createdEpochDay: Long,
    /** First day the habit is inactive; null means it has not been archived. */
    val inactiveFromEpochDay: Long?
)

enum class HabitDayStatus { UNRECORDED, COMPLETED, NOT_COMPLETED }

/**
 * A habit's state on a selected date. History can include archived habits, so isActiveOnDate
 * is kept separate from completion status: an inactive habit may still have historical records.
 */
data class HabitDayEntry(
    val habit: Habit,
    val status: HabitDayStatus,
    val currentStreak: Int = 0,
    val isActiveOnDate: Boolean = true
)
