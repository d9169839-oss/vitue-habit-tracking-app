package com.virtue.habittracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("SELECT * FROM habit_check_ins WHERE habitId = :habitId AND epochDay = :epochDay LIMIT 1")
    suspend fun getCheckIn(habitId: String, epochDay: Long): HabitCheckInEntity?

    @Query("SELECT * FROM habit_check_ins WHERE epochDay = :epochDay")
    suspend fun getCheckInsForDay(epochDay: Long): List<HabitCheckInEntity>

    @Query("SELECT * FROM habit_check_ins WHERE epochDay <= :epochDay ORDER BY habitId, epochDay DESC")
    fun observeCheckInsThroughDay(epochDay: Long): Flow<List<HabitCheckInEntity>>

    // Account switching clears the unscoped local cache before another user can see it.
    @Query("DELETE FROM habit_check_ins")
    suspend fun deleteAllCheckIns()

    @Query("DELETE FROM sync_operations")
    suspend fun clearSyncQueueForAccountSwitch()

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    // These full-table reads support a simple retry pass for local writes made while offline.
    @Query("SELECT * FROM sync_operations ORDER BY queuedAtMillis LIMIT 500")
    suspend fun getPendingSyncOperations(): List<SyncOperationEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM sync_operations WHERE operationKey = :operationKey)")
    suspend fun hasPendingSyncOperation(operationKey: String): Boolean

    @Query("SELECT queuedAtMillis FROM sync_operations WHERE operationKey = :operationKey LIMIT 1")
    suspend fun getPendingSyncTimestamp(operationKey: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueSyncOperation(operation: SyncOperationEntity)

    @Query("DELETE FROM sync_operations WHERE operationKey = :operationKey AND queuedAtMillis = :queuedAtMillis")
    suspend fun deleteSyncOperationIfUnchanged(operationKey: String, queuedAtMillis: Long)

    @Query("DELETE FROM sync_operations")
    suspend fun deleteAllSyncOperations()

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabitFromCloud(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheckInsIfMissing(checkIns: List<HabitCheckInEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckInFromCloud(checkIn: HabitCheckInEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIn(checkIn: HabitCheckInEntity)

    @Query("DELETE FROM habit_check_ins WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun deleteCheckIn(habitId: String, epochDay: Long)

    @Query("UPDATE habits SET inactiveFromEpochDay = :inactiveFromEpochDay WHERE id = :habitId AND (inactiveFromEpochDay IS NULL OR inactiveFromEpochDay > :inactiveFromEpochDay)")
    suspend fun archiveHabit(habitId: String, inactiveFromEpochDay: Long)

    /**
     * Each local mutation and its outbox entry commit together. A crash cannot leave a
     * successful local edit with no durable instruction to upload it.
     */
    @Transaction
    suspend fun saveHabitAndQueue(habit: HabitEntity) {
        insertHabit(habit)
        queueOperation("habit:${habit.id}", "HABIT", habit.id, "UPSERT")
    }

    @Transaction
    suspend fun saveCheckInAndQueue(checkIn: HabitCheckInEntity) {
        upsertCheckIn(checkIn)
        queueOperation("checkin:${checkIn.habitId}_${checkIn.epochDay}", "CHECK_IN",
            "${checkIn.habitId}_${checkIn.epochDay}", "UPSERT")
    }

    @Transaction
    suspend fun clearCheckInAndQueue(habitId: String, epochDay: Long) {
        deleteCheckIn(habitId, epochDay)
        queueOperation("checkin:${habitId}_${epochDay}", "CHECK_IN",
            "${habitId}_${epochDay}", "DELETE")
    }

    @Transaction
    suspend fun archiveHabitAndQueue(habitId: String, inactiveFromEpochDay: Long) {
        archiveHabit(habitId, inactiveFromEpochDay)
        queueOperation("habit:$habitId", "HABIT", habitId, "UPSERT")
    }

    private suspend fun queueOperation(
        key: String,
        entityType: String,
        entityId: String,
        operationType: String
    ) {
        // Strictly increasing revisions prevent a sync completion from erasing a newer edit.
        val previous = getPendingSyncTimestamp(key) ?: 0L
        val revision = maxOf(System.currentTimeMillis(), previous + 1L)
        enqueueSyncOperation(SyncOperationEntity(key, entityType, entityId, operationType, revision))
    }
}
