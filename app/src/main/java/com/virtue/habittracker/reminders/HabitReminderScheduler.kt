package com.virtue.habittracker.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules one reminder at 8:00 PM in the device's current time zone.
 *
 * A dated unique-work name prevents app launches from creating duplicate reminders.
 * WorkManager is not an exact alarm: Android battery restrictions can delay delivery.
 */
@Singleton
class HabitReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleNextReminder() {
        val now = LocalDateTime.now()
        val todayAtReminderTime = LocalDate.now().atTime(REMINDER_TIME)
        val targetDateTime = if (now.isBefore(todayAtReminderTime)) {
            todayAtReminderTime
        } else {
            LocalDate.now().plusDays(1).atTime(REMINDER_TIME)
        }
        scheduleFor(targetDateTime)
    }

    fun scheduleFor(dateTime: LocalDateTime) {
        val now = LocalDateTime.now()
        val delay = Duration.between(now, dateTime).toMillis().coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "habit_reminder_${dateTime.toLocalDate()}",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        private val REMINDER_TIME: LocalTime = LocalTime.of(20, 0)
    }
}
