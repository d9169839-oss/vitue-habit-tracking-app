package com.virtue.habittracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [HabitEntity::class, HabitCheckInEntity::class, SyncOperationEntity::class, ProgramEnrollmentEntity::class, ProgramActivityEntity::class],
    version = 3,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun programDao(): ProgramDao

    companion object {
        /** Preserve existing user data while adding the durable offline-sync outbox. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS program_enrollments (id TEXT NOT NULL PRIMARY KEY, templateId TEXT NOT NULL, templateVersion INTEGER NOT NULL, titleSnapshot TEXT NOT NULL, category TEXT NOT NULL, durationDays INTEGER NOT NULL, startEpochDay INTEGER NOT NULL, status TEXT NOT NULL, availableMinutesPerDay INTEGER NOT NULL, availableDaysPerWeek INTEGER NOT NULL, experience TEXT NOT NULL, equipment TEXT NOT NULL, isPremium INTEGER NOT NULL, createdAtMillis INTEGER NOT NULL, updatedAtMillis INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_program_enrollments_startEpochDay ON program_enrollments(startEpochDay)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_program_enrollments_status ON program_enrollments(status)")
                db.execSQL("CREATE TABLE IF NOT EXISTS program_activities (id TEXT NOT NULL PRIMARY KEY, enrollmentId TEXT NOT NULL, dayIndex INTEGER NOT NULL, epochDay INTEGER NOT NULL, phaseTitle TEXT NOT NULL, title TEXT NOT NULL, instructions TEXT NOT NULL, estimatedMinutes INTEGER NOT NULL, status TEXT NOT NULL, updatedAtMillis INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_program_activities_enrollmentId ON program_activities(enrollmentId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_program_activities_epochDay ON program_activities(epochDay)")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_operations (
                        operationKey TEXT NOT NULL PRIMARY KEY,
                        entityType TEXT NOT NULL,
                        entityId TEXT NOT NULL,
                        operationType TEXT NOT NULL,
                        queuedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
