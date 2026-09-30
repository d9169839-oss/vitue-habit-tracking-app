package com.virtue.habittracker.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.virtue.habittracker.data.repository.HabitCloudDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Upload pending Room changes first, then pull remote changes since the saved cursor. */
@HiltWorker
class HabitSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cloud: HabitCloudDataSource
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        // No signed-in user? Keep the durable outbox for the next authenticated session.
        cloud.syncPendingChanges()
        cloud.pullChangesSinceLastSync()
        Result.success()
    } catch (_: Exception) {
        // WorkManager retries with backoff; pending operations and the sync cursor are retained.
        Result.retry()
    }
}
