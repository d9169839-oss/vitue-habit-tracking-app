package com.virtue.habittracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [HabitEntity::class, HabitCheckInEntity::class, SyncOperationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        /** Preserve existing user data while adding the durable offline-sync outbox. */
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
