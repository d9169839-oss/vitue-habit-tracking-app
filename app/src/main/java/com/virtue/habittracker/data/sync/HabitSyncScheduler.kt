package com.virtue.habittracker.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WorkManager persists these jobs across app closure and device restarts.
 * Network constraints mean the app never needs to block a user action waiting for Firebase.
 */
@Singleton
class HabitSyncScheduler @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    private val connectedConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun enqueueSync() {
        val request = OneTimeWorkRequestBuilder<HabitSyncWorker>()
            // A tiny debounce lets rapid taps/edit sequences collapse into one latest-value upload.
            .setInitialDelay(2, TimeUnit.SECONDS)
            .setConstraints(connectedConstraint)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .build()
        workManager.enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    /** A low-frequency incremental pull keeps other-device edits eventually consistent. */
    fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<HabitSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(connectedConstraint)
            .build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private companion object {
        const val ONE_TIME_WORK_NAME = "vitue-habit-sync"
        const val PERIODIC_WORK_NAME = "vitue-habit-periodic-sync"
    }
}
