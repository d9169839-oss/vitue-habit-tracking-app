package com.virtue.habittracker.domain.usecase

import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.repository.AuthRepository
import com.virtue.habittracker.domain.repository.AuthSessionCoordinator
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val sessionCoordinator: AuthSessionCoordinator
) {
    suspend operator fun invoke(email: String, password: String): AuthOutcome {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank() || !normalizedEmail.contains('@')) {
            return AuthOutcome.Failure("Enter a valid email address.")
        }
        if (password.isBlank()) {
            return AuthOutcome.Failure("Enter your password.")
        }
        val outcome = repository.signIn(normalizedEmail, password)
        if (outcome is AuthOutcome.Success) {
            sessionCoordinator.onSessionStarted(outcome.user)
        }
        return outcome
    }
}
