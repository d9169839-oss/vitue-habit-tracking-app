package com.virtue.habittracker.domain.usecase
import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.repository.AuthRepository
import javax.inject.Inject
class RegisterUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthOutcome {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank() || !normalizedEmail.contains('@')) return AuthOutcome.Failure("Enter a valid email address.")
        if (password.length < 8) return AuthOutcome.Failure("Password must be at least 8 characters.")
        if (password.any(Char::isWhitespace)) return AuthOutcome.Failure("Password cannot contain spaces.")
        if (password.none(Char::isDigit)) return AuthOutcome.Failure("Password must contain at least one number.")
        if (password.any { !it.isLetterOrDigit() }) return AuthOutcome.Failure("Password cannot contain symbols.")
        return repository.register(normalizedEmail, password)
    }
}
