package com.virtue.habittracker.presentation.auth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.usecase.GoogleSignInUseCase
import com.virtue.habittracker.domain.usecase.RegisterUseCase
import com.virtue.habittracker.domain.usecase.SendPasswordResetUseCase
import com.virtue.habittracker.domain.usecase.SignInUseCase
import com.virtue.habittracker.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChanged(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun signIn() = execute { signInUseCase(_uiState.value.email, _uiState.value.password) }
    fun register() {
        if (_uiState.value.password != _uiState.value.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }
        execute { registerUseCase(_uiState.value.email, _uiState.value.password) }
    }
    fun signInWithGoogle(idToken: String) = execute { googleSignInUseCase(idToken) }
    fun signOut() { viewModelScope.launch { signOutUseCase() } }
    fun sendPasswordReset() {
        val email = _uiState.value.email
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            sendPasswordResetUseCase(email)
                .onSuccess { _uiState.update { it.copy(isLoading = false, infoMessage = "If an account exists for that email, a reset email has been requested.") } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Could not request a password reset.") } }
        }
    }
    private fun execute(operation: suspend () -> AuthOutcome) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            when (val outcome = operation()) {
                is AuthOutcome.Success -> {
                    _uiState.update { it.copy(isLoading = false, password = "", confirmPassword = "") }
                    _events.emit(AuthEvent.Authenticated)
                }
                is AuthOutcome.Failure -> _uiState.update { it.copy(isLoading = false, errorMessage = outcome.message) }
            }
        }
    }
}
