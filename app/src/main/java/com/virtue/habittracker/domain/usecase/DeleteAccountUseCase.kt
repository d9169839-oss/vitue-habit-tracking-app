package com.virtue.habittracker.domain.usecase

import com.virtue.habittracker.domain.repository.AuthRepository
import com.virtue.habittracker.domain.repository.AuthSessionCoordinator
import javax.inject.Inject

/**
 * Deletes the authenticated user's cloud data, deletes their Firebase Authentication
 * account, and wipes the local Room cache.
 */
class DeleteAccountUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val sessionCoordinator: AuthSessionCoordinator
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        sessionCoordinator.onDeleteAccountData()
        repository.deleteAccount().getOrThrow()
        sessionCoordinator.onAccountDeleted()
    }
}
