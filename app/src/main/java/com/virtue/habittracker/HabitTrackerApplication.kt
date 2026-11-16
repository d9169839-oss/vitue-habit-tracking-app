package com.virtue.habittracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.virtue.habittracker.reminders.HabitReminderScheduler
import com.virtue.habittracker.data.sync.HabitSyncScheduler
import com.virtue.habittracker.data.repository.HabitCloudDataSource
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

    @Inject
    lateinit var habitCloudDataSource: HabitCloudDataSource

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // WorkManager persists scheduled work across process death and device restarts.
        reminderScheduler.scheduleNextReminder()
        // Listen for remote habit/check-in changes while signed in. Events are merged into Room,
        // so every screen continues observing the same local source of truth.
        habitCloudDataSource.startRealtimeSync()
        // Startup schedules durable work; the worker exits harmlessly when signed out/offline.
        habitSyncScheduler.enqueueSync()
        habitSyncScheduler.schedulePeriodicSync()
    }
}
