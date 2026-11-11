package com.virtue.habittracker.domain.repository

import com.virtue.habittracker.domain.model.User

/**
 * Coordinates post-authentication actions such as local cache preparation
 * and initial cloud synchronization.
 *
 * Separating session coordination from identity authentication adheres to the
 * Single Responsibility Principle (SRP).
 */
fun interface AuthSessionCoordinator {
    suspend fun onSessionStarted(user: User)
}
