package com.virtue.habittracker.domain.usecase
import com.virtue.habittracker.domain.repository.AuthRepository
import javax.inject.Inject
class SendPasswordResetUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        val normalized = email.trim()
        if (normalized.isBlank() || !normalized.contains('@')) return Result.failure(IllegalArgumentException("Enter a valid email address."))
        return repository.sendPasswordReset(normalized)
    }
}
