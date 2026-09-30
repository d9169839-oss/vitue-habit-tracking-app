package com.virtue.habittracker.presentation.auth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtue.habittracker.domain.model.User
import com.virtue.habittracker.domain.usecase.ObserveAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: User) : SessionState
}

@HiltViewModel
class AuthStateViewModel @Inject constructor(
    observeAuthState: ObserveAuthStateUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state = _state.asStateFlow()
    init {
        viewModelScope.launch {
            observeAuthState().collect { user ->
                _state.value = if (user == null) SessionState.SignedOut else SessionState.SignedIn(user)
            }
        }
    }
}
