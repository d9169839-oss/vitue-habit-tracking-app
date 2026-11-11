package com.virtue.habittracker.domain.usecase

import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.repository.AuthRepository
import com.virtue.habittracker.domain.repository.AuthSessionCoordinator
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val sessionCoordinator: AuthSessionCoordinator
) {
    suspend operator fun invoke(idToken: String): AuthOutcome {
        if (idToken.isBlank()) return AuthOutcome.Failure("Google sign-in did not return an ID token.")
        val outcome = repository.signInWithGoogle(idToken)
        if (outcome is AuthOutcome.Success) {
            sessionCoordinator.onSessionStarted(outcome.user)
        }
        return outcome
    }
}
