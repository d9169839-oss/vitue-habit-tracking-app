package com.virtue.habittracker.domain.repository

import com.virtue.habittracker.domain.model.HabitDayEntry
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    /** Habits active on this date, used by the daily practice screen. */
    fun observeHabitsForDay(epochDay: Long): Flow<List<HabitDayEntry>>

    /** All habits created by this date, including archived habits, for historical review. */
    fun observeHistoryForDay(epochDay: Long): Flow<List<HabitDayEntry>>

    suspend fun createHabit(title: String, description: String, createdEpochDay: Long)
    suspend fun setCompletion(habitId: String, epochDay: Long, completed: Boolean)
    suspend fun clearCompletion(habitId: String, epochDay: Long)
    suspend fun archiveHabit(habitId: String, inactiveFromEpochDay: Long)
}
