package com.virtue.habittracker.domain.repository

/**
 * Permanently deletes the currently authenticated user's remote account and local app data.
 * The implementation must derive the UID from FirebaseAuth, never from UI input.
 */
interface AccountDeletionRepository {
    suspend fun deleteCurrentAccount()
}
