package com.virtue.habittracker.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtue.habittracker.domain.usecase.DeleteAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccountDeletionViewModel @Inject constructor(
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountDeletionUiState())
    val uiState = _uiState.asStateFlow()

    private val _completed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val completed = _completed.asSharedFlow()

    fun showConfirmation() {
        _uiState.update {
            it.copy(showConfirmation = true, confirmationText = "", errorMessage = null)
        }
    }

    fun dismissConfirmation() {
        if (_uiState.value.isDeleting) return
        _uiState.update {
            it.copy(showConfirmation = false, confirmationText = "", errorMessage = null)
        }
    }

    fun onConfirmationTextChanged(value: String) {
        _uiState.update { it.copy(confirmationText = value, errorMessage = null) }
    }

    fun deleteAccount() {
        if (_uiState.value.isDeleting || _uiState.value.confirmationText != CONFIRMATION_WORD) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            try {
                deleteAccountUseCase()
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        showConfirmation = false,
                        confirmationText = "",
                        errorMessage = null
                    )
                }
                _completed.emit(Unit)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = error.localizedMessage
                            ?: "Account deletion could not be completed. Please try again."
                    )
                }
            }
        }
    }

    private companion object {
        const val CONFIRMATION_WORD = "DELETE"
    }
}
