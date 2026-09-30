package com.virtue.habittracker.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.virtue.habittracker.data.local.HabitDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Runs in the background, checks Room's local data (including while offline), and reminds
 * the user about active habits that have not been marked completed for today.
 */
@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitDao: HabitDao,
    private val scheduler: HabitReminderScheduler
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Schedule tomorrow first so a temporary database/notification failure doesn't stop
        // the reminder cycle permanently.
        scheduler.scheduleFor(LocalDate.now().plusDays(1).atTime(20, 0))

        return try {
            val today = LocalDate.now().toEpochDay()
            val activeHabits = habitDao.getActiveHabitsForDay(today)
            if (activeHabits.isEmpty()) return Result.success()

            val completedHabitIds = habitDao.getCheckInsForDay(today)
                .filter { it.completed }
                .mapTo(mutableSetOf()) { it.habitId }
            val remainingCount = activeHabits.count { it.id !in completedHabitIds }

            HabitNotificationHelper.showReminder(applicationContext, remainingCount)
            Result.success()
        } catch (_: Exception) {
            // WorkManager may retry transient Room failures rather than crashing the process.
            Result.retry()
        }
    }
}
