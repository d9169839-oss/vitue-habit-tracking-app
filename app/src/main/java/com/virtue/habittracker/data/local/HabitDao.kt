package com.virtue.habittracker.data.local
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE createdEpochDay <= :epochDay AND (inactiveFromEpochDay IS NULL OR :epochDay < inactiveFromEpochDay) ORDER BY createdEpochDay, title")
    fun observeHabitsForDay(epochDay: Long): Flow<List<HabitEntity>>
    @Query("SELECT * FROM habit_check_ins WHERE epochDay <= :epochDay ORDER BY habitId, epochDay DESC")
    fun observeCheckInsThroughDay(epochDay: Long): Flow<List<HabitCheckInEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIn(checkIn: HabitCheckInEntity)
    @Query("DELETE FROM habit_check_ins WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun deleteCheckIn(habitId: String, epochDay: Long)
    @Query("UPDATE habits SET inactiveFromEpochDay = :inactiveFromEpochDay WHERE id = :habitId AND (inactiveFromEpochDay IS NULL OR inactiveFromEpochDay > :inactiveFromEpochDay)")
    suspend fun archiveHabit(habitId: String, inactiveFromEpochDay: Long)
}
