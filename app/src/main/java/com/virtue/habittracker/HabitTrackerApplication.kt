package com.virtue.habittracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.virtue.habittracker.data.repository.HabitCloudDataSource
import com.virtue.habittracker.data.sync.HabitSyncScheduler
import com.virtue.habittracker.reminders.HabitReminderScheduler
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

        // App Check protects callable account deletion from unauthenticated/unverified clients.
        AppCheckInstaller.install()

        reminderScheduler.scheduleNextReminder()
        // Listen for remote habit, check-in and program changes while signed in.
        habitCloudDataSource.startRealtimeSync()
        habitSyncScheduler.enqueueSync()
        habitSyncScheduler.schedulePeriodicSync()
    }
}
