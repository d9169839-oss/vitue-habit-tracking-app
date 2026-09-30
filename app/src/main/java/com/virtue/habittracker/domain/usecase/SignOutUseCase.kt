package com.virtue.habittracker.domain.usecase
import com.virtue.habittracker.domain.repository.AuthRepository
import javax.inject.Inject
class SignOutUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.signOut()
}
