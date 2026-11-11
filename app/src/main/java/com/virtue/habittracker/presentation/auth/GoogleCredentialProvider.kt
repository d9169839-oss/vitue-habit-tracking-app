package com.virtue.habittracker.presentation.auth

/**
 * Abstraction for acquiring a Google ID token.
 *
 * Moving credential acquisition behind this interface satisfies the
 * Dependency Inversion Principle (DIP) and allows UI and ViewModel tests
 * to supply deterministic fake credentials without depending on Android's CredentialManager.
 */
fun interface GoogleCredentialProvider {
    suspend fun getIdToken(): String
}
