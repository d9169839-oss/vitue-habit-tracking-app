package com.virtue.habittracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.virtue.habittracker.reminders.HabitReminderScheduler
import com.virtue.habittracker.data.sync.HabitSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitTrackerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var reminderScheduler: HabitReminderScheduler

    @Inject
    lateinit var habitSyncScheduler: HabitSyncScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // WorkManager persists scheduled work across process death and device restarts.
        reminderScheduler.scheduleNextReminder()
        // Startup schedules durable work; the worker exits harmlessly when signed out/offline.
        habitSyncScheduler.enqueueSync()
        habitSyncScheduler.schedulePeriodicSync()
    }
}
