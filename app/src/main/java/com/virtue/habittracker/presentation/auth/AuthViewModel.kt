package com.virtue.habittracker.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.usecase.DeleteAccountUseCase
import com.virtue.habittracker.domain.usecase.GoogleSignInUseCase
import com.virtue.habittracker.domain.usecase.RegisterUseCase
import com.virtue.habittracker.domain.usecase.SendPasswordResetUseCase
import com.virtue.habittracker.domain.usecase.SignInUseCase
import com.virtue.habittracker.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()
    fun showError(message: String) = _uiState.update { it.copy(errorMessage = message) }
    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    fun onNameChanged(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChanged(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun signIn() = execute { signInUseCase(_uiState.value.email, _uiState.value.password) }
    fun register() {
        if (_uiState.value.password != _uiState.value.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }
        execute { registerUseCase(_uiState.value.name, _uiState.value.email, _uiState.value.password) }
    }
    fun signInWithGoogle(idToken: String) = execute { googleSignInUseCase(idToken) }
    fun signOut(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { signOutUseCase() }
            onComplete()
        }
    }
    fun deleteAccount(onDeleted: () -> Unit = {}) {
        if (_uiState.value.isLoading) return
        val useCase = deleteAccountUseCase ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            try {
                useCase()
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.emit(AuthEvent.AccountDeleted)
                        onDeleted()
                    }
                    .onFailure { error ->
                        val raw = error.localizedMessage.orEmpty()
                        val msg = if (raw.contains("recent", ignoreCase = true) || raw.contains("REQUIRES_RECENT_LOGIN", ignoreCase = true)) {
                            "For security, please sign out, sign back in, and then delete your account."
                        } else {
                            raw.ifBlank { "Could not delete account. Please check your connection and try again." }
                        }
                        _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Could not delete account."
                    )
                }
            }
        }
    }
    fun sendPasswordReset() {
        val email = _uiState.value.email
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            try {
                sendPasswordResetUseCase(email)
                    .onSuccess { _uiState.update { it.copy(isLoading = false, infoMessage = "If an account exists for that email, a reset email has been requested.") } }
                    .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Could not request a password reset.") } }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Could not request a password reset.") }
            }
        }
    }
    private fun execute(operation: suspend () -> AuthOutcome) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            // Unexpected infrastructure errors become UI state rather than crashing the screen.
            val outcome = try {
                operation()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AuthOutcome.Failure(error.localizedMessage?.takeIf(String::isNotBlank)
                    ?: "Authentication failed. Please try again.")
            }
            when (outcome) {
                is AuthOutcome.Success -> {
                    _uiState.update { it.copy(isLoading = false, password = "", confirmPassword = "") }
                    _events.emit(AuthEvent.Authenticated)
                }
                is AuthOutcome.Failure -> _uiState.update { it.copy(isLoading = false, errorMessage = outcome.message) }
            }
        }
    }
}
