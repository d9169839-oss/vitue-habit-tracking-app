package com.virtue.habittracker.domain.usecase

import com.virtue.habittracker.domain.repository.AccountDeletionRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: AccountDeletionRepository
) {
    suspend operator fun invoke() = repository.deleteCurrentAccount()
}
