package com.virtue.habittracker.domain.repository
import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.model.User
import kotlinx.coroutines.flow.Flow
interface AuthRepository {
    val authState: Flow<User?>
    suspend fun signIn(email: String, password: String): AuthOutcome
    suspend fun register(email: String, password: String): AuthOutcome
    suspend fun signInWithGoogle(idToken: String): AuthOutcome
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun signOut()
}
