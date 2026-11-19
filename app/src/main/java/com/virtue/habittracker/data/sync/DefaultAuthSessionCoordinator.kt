package com.virtue.habittracker.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.virtue.habittracker.data.repository.HabitCloudDataSource
import com.virtue.habittracker.domain.model.User
import com.virtue.habittracker.domain.repository.AuthSessionCoordinator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete coordinator for preparing per-user local cache, scheduling background sync,
 * flushing changes before sign-out, and purging user data on account deletion.
 */
@Singleton
class DefaultAuthSessionCoordinator @Inject constructor(
    private val auth: FirebaseAuth,
    private val habitCloudDataSource: HabitCloudDataSource,
    private val syncScheduler: HabitSyncScheduler
) : AuthSessionCoordinator {

    @Volatile
    private var lastDeletingUid: String? = null

    override suspend fun onSessionStarted(user: User) {
        // Profile cache and cloud sync are best-effort; network delays must not block auth.
        runCatching { habitCloudDataSource.prepareForCurrentUser() }
        syncScheduler.enqueueSync()
    }

    override suspend fun onBeforeSignOut() {
        runCatching { habitCloudDataSource.flushAndClearLocalCacheForSignOut() }
    }

    override suspend fun onDeleteAccountData() {
        lastDeletingUid = auth.currentUser?.uid
        habitCloudDataSource.deleteRemoteUserData()
    }

    override suspend fun onAccountDeleted() {
        val uid = lastDeletingUid
        lastDeletingUid = null
        runCatching { habitCloudDataSource.clearAllLocalUserData(uid) }
    }
}
