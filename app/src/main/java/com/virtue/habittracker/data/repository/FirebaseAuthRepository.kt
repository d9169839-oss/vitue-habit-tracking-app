package com.virtue.habittracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.model.User
import com.virtue.habittracker.domain.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Manages Firebase Authentication operations and observes authentication state.
 *
 * Adhering to SRP, this repository exclusively translates authentication operations
 * with the identity provider (Firebase Auth). Post-authentication concerns (such as
 * local cache isolation and cloud sync scheduling) are delegated to domain use cases
 * and [com.virtue.habittracker.domain.repository.AuthSessionCoordinator].
 */
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val authState: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val current = auth.currentUser
            trySend(current?.let { User(it.uid, it.email, it.displayName) })
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): AuthOutcome = safely {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        currentUser()
    }

    override suspend fun register(name: String, email: String, password: String): AuthOutcome = safely {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        // Firebase Authentication stores the display name; the profile document mirrors it.
        runCatching {
            firebaseAuth.currentUser?.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(name).build()
            )?.await()
        }
        currentUser()
    }

    override suspend fun signInWithGoogle(idToken: String): AuthOutcome = safely {
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        currentUser()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user found.")
        user.delete().await()
    }

    override suspend fun signOut() = firebaseAuth.signOut()

    private fun currentUser(): AuthOutcome {
        val user = firebaseAuth.currentUser
            ?: return AuthOutcome.Failure("No authenticated session was found.")
        return AuthOutcome.Success(User(user.uid, user.email, user.displayName))
    }

    private suspend fun safely(block: suspend () -> AuthOutcome): AuthOutcome = try {
        block()
    } catch (error: Exception) {
        val rawMsg = error.localizedMessage?.takeIf(String::isNotBlank) ?: ""
        val friendlyMessage = if (rawMsg.contains("API key", ignoreCase = true) || rawMsg.contains("API_KEY", ignoreCase = true)) {
            "Invalid Firebase API key in app/google-services.json. Please download your actual google-services.json from the Firebase Console and replace app/google-services.json."
        } else {
            rawMsg.ifBlank { "Authentication failed. Please try again." }
        }
        AuthOutcome.Failure(friendlyMessage)
    }
}
