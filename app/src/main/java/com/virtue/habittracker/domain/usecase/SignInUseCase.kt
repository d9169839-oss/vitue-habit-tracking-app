package com.virtue.habittracker.domain.usecase
import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.repository.AuthRepository
import javax.inject.Inject
class SignInUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthOutcome {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank() || !normalizedEmail.contains('@')) return AuthOutcome.Failure("Enter a valid email address.")
        if (password.isBlank()) return AuthOutcome.Failure("Enter your password.")
        return repository.signIn(normalizedEmail, password)
    }
}
