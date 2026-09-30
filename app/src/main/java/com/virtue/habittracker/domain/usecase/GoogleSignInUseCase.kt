package com.virtue.habittracker.domain.usecase
import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.repository.AuthRepository
import javax.inject.Inject
class GoogleSignInUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String): AuthOutcome =
        if (idToken.isBlank()) AuthOutcome.Failure("Google sign-in did not return an ID token.") else repository.signInWithGoogle(idToken)
}
