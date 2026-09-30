package com.virtue.habittracker.domain.usecase
import com.virtue.habittracker.domain.repository.AuthRepository
import javax.inject.Inject
class ObserveAuthStateUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.authState
}
