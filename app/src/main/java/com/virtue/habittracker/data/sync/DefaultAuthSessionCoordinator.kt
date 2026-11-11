package com.virtue.habittracker.data.sync

import com.virtue.habittracker.data.repository.HabitCloudDataSource
import com.virtue.habittracker.domain.model.User
import com.virtue.habittracker.domain.repository.AuthSessionCoordinator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete coordinator for preparing per-user local cache and scheduling background sync
 * after a successful user authentication.
 */
@Singleton
class DefaultAuthSessionCoordinator @Inject constructor(
    private val habitCloudDataSource: HabitCloudDataSource,
    private val syncScheduler: HabitSyncScheduler
) : AuthSessionCoordinator {

    override suspend fun onSessionStarted(user: User) {
        // Profile cache and cloud sync are best-effort; network delays must not block auth.
        runCatching { habitCloudDataSource.prepareForCurrentUser() }
        syncScheduler.enqueueSync()
    }
}
