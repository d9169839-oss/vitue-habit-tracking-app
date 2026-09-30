package com.virtue.habittracker.domain.usecase

import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(name: String, email: String, password: String): AuthOutcome {
        val cleanName = name.trim()
        val normalizedEmail = email.trim()
        if (cleanName.isBlank()) return AuthOutcome.Failure("Enter your name.")
        if (cleanName.length > 80) return AuthOutcome.Failure("Name must be 80 characters or fewer.")
        if (normalizedEmail.isBlank() || !normalizedEmail.contains('@')) {
            return AuthOutcome.Failure("Enter a valid email address.")
        }
        if (password.length < 8) return AuthOutcome.Failure("Password must be at least 8 characters.")
        if (password.any(Char::isWhitespace)) return AuthOutcome.Failure("Password cannot contain spaces.")
        if (password.none(Char::isDigit)) return AuthOutcome.Failure("Password must contain at least one number.")
        if (password.any { !it.isLetterOrDigit() }) return AuthOutcome.Failure("Password cannot contain symbols.")
        return repository.register(cleanName, normalizedEmail, password)
    }
}
