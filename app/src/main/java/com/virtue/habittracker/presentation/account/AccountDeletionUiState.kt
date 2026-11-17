package com.virtue.habittracker.presentation.account

data class AccountDeletionUiState(
    val showConfirmation: Boolean = false,
    val confirmationText: String = "",
    val isDeleting: Boolean = false,
    val errorMessage: String? = null
)
