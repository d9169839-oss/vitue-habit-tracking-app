package com.virtue.habittracker.domain.model
data class Habit(
    val id: String,
    val title: String,
    val description: String,
    val createdEpochDay: Long,
    val inactiveFromEpochDay: Long?
)
enum class HabitDayStatus { UNRECORDED, COMPLETED, NOT_COMPLETED }
data class HabitDayEntry(val habit: Habit, val status: HabitDayStatus)
