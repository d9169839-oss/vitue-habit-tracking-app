package com.virtue.habittracker.domain.repository

import com.virtue.habittracker.domain.model.User

/**
 * Coordinates post-authentication and session teardown actions such as local cache
 * preparation, cloud synchronization, and account data deletion.
 *
 * Separating session coordination from identity authentication adheres to the
 * Single Responsibility Principle (SRP).
 */
fun interface AuthSessionCoordinator {
    suspend fun onSessionStarted(user: User)
    suspend fun onBeforeSignOut() = Unit
    suspend fun onDeleteAccountData() = Unit
    suspend fun onAccountDeleted() = Unit
}
