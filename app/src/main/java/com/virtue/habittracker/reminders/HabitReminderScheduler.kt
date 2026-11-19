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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules a daily reminder in the device's current time zone (defaults to 8:00 PM).
 *
 * A dated unique-work name prevents app launches from creating duplicate reminders.
 * WorkManager is not an exact alarm: Android battery restrictions can delay delivery.
 */
@Singleton
class HabitReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isRemindersEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, true)

    fun setRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            scheduleNextReminder()
        } else {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        }
    }

    fun getReminderTime(): LocalTime {
        val hour = prefs.getInt(KEY_HOUR, DEFAULT_HOUR).coerceIn(0, 23)
        val minute = prefs.getInt(KEY_MINUTE, DEFAULT_MINUTE).coerceIn(0, 59)
        return LocalTime.of(hour, minute)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_MINUTE, minute.coerceIn(0, 59))
            .apply()
        if (isRemindersEnabled()) {
            scheduleNextReminder()
        }
    }

    fun scheduleNextReminder() {
        if (!isRemindersEnabled()) return
        val reminderTime = getReminderTime()
        val now = LocalDateTime.now()
        val todayAtReminderTime = LocalDate.now().atTime(reminderTime)
        val targetDateTime = if (now.isBefore(todayAtReminderTime)) {
            todayAtReminderTime
        } else {
            LocalDate.now().plusDays(1).atTime(reminderTime)
        }
        scheduleFor(targetDateTime)
    }

    fun scheduleFor(dateTime: LocalDateTime) {
        if (!isRemindersEnabled()) return
        val now = LocalDateTime.now()
        val delay = Duration.between(now, dateTime).toMillis().coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "habit_reminder_${dateTime.toLocalDate()}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        private const val PREFS_NAME = "habit_reminder_prefs"
        private const val KEY_ENABLED = "reminders_enabled"
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_MINUTE = "reminder_minute"
        private const val DEFAULT_HOUR = 20
        private const val DEFAULT_MINUTE = 0
        private const val WORK_TAG = "daily_habit_reminder_tag"
    }
}
