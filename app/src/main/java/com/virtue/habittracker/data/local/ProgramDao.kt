package com.virtue.habittracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Query("SELECT * FROM program_enrollments ORDER BY createdAtMillis DESC")
    fun observeEnrollments(): Flow<List<ProgramEnrollmentEntity>>

    @Query("SELECT * FROM program_activities ORDER BY dayIndex")
    fun observeActivities(): Flow<List<ProgramActivityEntity>>

    @Query("SELECT * FROM program_enrollments WHERE id = :id LIMIT 1")
    suspend fun getEnrollment(id: String): ProgramEnrollmentEntity?

    @Query("SELECT * FROM program_activities WHERE id = :id LIMIT 1")
    suspend fun getActivity(id: String): ProgramActivityEntity?

    @Query("SELECT * FROM program_activities WHERE enrollmentId = :enrollmentId ORDER BY dayIndex")
    suspend fun getActivitiesForEnrollment(enrollmentId: String): List<ProgramActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEnrollment(entity: ProgramEnrollmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActivities(entities: List<ProgramActivityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActivity(entity: ProgramActivityEntity)

    @Query("DELETE FROM program_activities WHERE id = :activityId")
    suspend fun deleteActivityRow(activityId: String)

    @Query("DELETE FROM program_activities WHERE enrollmentId = :enrollmentId")
    suspend fun deleteActivitiesForEnrollment(enrollmentId: String)

    @Query("DELETE FROM program_enrollments WHERE id = :enrollmentId")
    suspend fun deleteEnrollmentRow(enrollmentId: String)

    @Query("DELETE FROM program_activities")
    suspend fun deleteAllActivities()

    @Query("DELETE FROM program_enrollments")
    suspend fun deleteAllEnrollments()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueSyncOperation(operation: SyncOperationEntity)

    @Query("SELECT queuedAtMillis FROM sync_operations WHERE operationKey = :key LIMIT 1")
    suspend fun pendingRevision(key: String): Long?

    @Transaction
    suspend fun saveEnrollmentAndSchedule(enrollment: ProgramEnrollmentEntity, activities: List<ProgramActivityEntity>) {
        upsertEnrollment(enrollment)
        upsertActivities(activities)
        queue("program-enrollment:${enrollment.id}", "PROGRAM_ENROLLMENT", enrollment.id)
        activities.forEach { queue("program-activity:${it.id}", "PROGRAM_ACTIVITY", it.id) }
    }

    @Transaction
    suspend fun updateActivityAndQueue(activity: ProgramActivityEntity) {
        upsertActivity(activity)
        queue("program-activity:${activity.id}", "PROGRAM_ACTIVITY", activity.id)
    }

    @Transaction
    suspend fun updateEnrollmentAndQueue(enrollment: ProgramEnrollmentEntity) {
        upsertEnrollment(enrollment)
        queue("program-enrollment:${enrollment.id}", "PROGRAM_ENROLLMENT", enrollment.id)
    }

    /**
     * Applies a schedule revision atomically. The repository preserves completed/skipped history
     * and supplies regenerated future rows with the same stable IDs.
     */
    @Transaction
    suspend fun replanEnrollmentAndQueue(
        enrollment: ProgramEnrollmentEntity,
        activities: List<ProgramActivityEntity>
    ) {
        upsertEnrollment(enrollment)
        upsertActivities(activities)
        queue("program-enrollment:${enrollment.id}", "PROGRAM_ENROLLMENT", enrollment.id)
        activities.forEach { queue("program-activity:${it.id}", "PROGRAM_ACTIVITY", it.id) }
    }
    @Transaction
    suspend fun deleteEnrollmentAndQueue(enrollmentId: String) {
        deleteActivitiesForEnrollment(enrollmentId)
        deleteEnrollmentRow(enrollmentId)
        queue("program-enrollment:$enrollmentId", "PROGRAM_ENROLLMENT", enrollmentId, "DELETE")
    }

    private suspend fun queue(key: String, type: String, id: String, operationType: String = "UPSERT") {
        val previous = pendingRevision(key) ?: 0L
        val revision = maxOf(System.currentTimeMillis(), previous + 1L)
        enqueueSyncOperation(SyncOperationEntity(key, type, id, operationType, revision))
    }
}
