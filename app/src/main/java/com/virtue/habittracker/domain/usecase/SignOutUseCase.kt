package com.virtue.habittracker.domain.usecase

import com.virtue.habittracker.domain.model.User
import com.virtue.habittracker.domain.repository.AuthRepository
import com.virtue.habittracker.domain.repository.AuthSessionCoordinator
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val sessionCoordinator: AuthSessionCoordinator
) {
    constructor(repository: AuthRepository) : this(
        repository = repository,
        sessionCoordinator = object : AuthSessionCoordinator {
            override suspend fun onSessionStarted(user: User) = Unit
        }
    )

    suspend operator fun invoke() {
        sessionCoordinator.onBeforeSignOut()
        repository.signOut()
    }
}
