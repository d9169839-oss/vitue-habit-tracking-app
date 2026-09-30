package com.virtue.habittracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    // Home only shows habits that were active on the selected date.
    @Query("SELECT * FROM habits WHERE createdEpochDay <= :epochDay AND (inactiveFromEpochDay IS NULL OR :epochDay < inactiveFromEpochDay) ORDER BY createdEpochDay, title")
    fun observeHabitsForDay(epochDay: Long): Flow<List<HabitEntity>>

    // History includes every habit created by the selected date, including archived habits.
    @Query("SELECT * FROM habits WHERE createdEpochDay <= :epochDay ORDER BY createdEpochDay DESC, title")
    fun observeAllHabitsCreatedByDay(epochDay: Long): Flow<List<HabitEntity>>

    // WorkManager reads the local cache so reminders can still work offline.
    @Query("SELECT * FROM habits WHERE createdEpochDay <= :epochDay AND (inactiveFromEpochDay IS NULL OR :epochDay < inactiveFromEpochDay) ORDER BY createdEpochDay, title")
    suspend fun getActiveHabitsForDay(epochDay: Long): List<HabitEntity>

    @Query("SELECT * FROM habit_check_ins WHERE epochDay = :epochDay")
    suspend fun getCheckInsForDay(epochDay: Long): List<HabitCheckInEntity>

    @Query("SELECT * FROM habit_check_ins WHERE epochDay <= :epochDay ORDER BY habitId, epochDay DESC")
    fun observeCheckInsThroughDay(epochDay: Long): Flow<List<HabitCheckInEntity>>

    // Account switching clears the unscoped local cache before another user can see it.
    @Query("DELETE FROM habit_check_ins")
    suspend fun deleteAllCheckIns()

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    // These full-table reads support a simple retry pass for local writes made while offline.
    @Query("SELECT * FROM habits")
    suspend fun getAllHabits(): List<HabitEntity>

    @Query("SELECT * FROM habit_check_ins")
    suspend fun getAllCheckIns(): List<HabitCheckInEntity>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabitById(habitId: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    // Merge cloud records into Room without deleting local-only/offline changes.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabitsIfMissing(habits: List<HabitEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheckInsIfMissing(checkIns: List<HabitCheckInEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIn(checkIn: HabitCheckInEntity)

    @Query("DELETE FROM habit_check_ins WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun deleteCheckIn(habitId: String, epochDay: Long)

    @Query("UPDATE habits SET inactiveFromEpochDay = :inactiveFromEpochDay WHERE id = :habitId AND (inactiveFromEpochDay IS NULL OR inactiveFromEpochDay > :inactiveFromEpochDay)")
    suspend fun archiveHabit(habitId: String, inactiveFromEpochDay: Long)
}
