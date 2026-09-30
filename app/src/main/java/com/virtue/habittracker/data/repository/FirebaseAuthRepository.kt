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

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val habitCloudDataSource: HabitCloudDataSource
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
        // Profile writes are best-effort; an unavailable network must not invalidate login.
        runCatching { habitCloudDataSource.prepareForCurrentUser() }
        currentUser()
    }

    override suspend fun register(name: String, email: String, password: String): AuthOutcome = safely {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        // Firebase Authentication stores the display name; the profile document mirrors it.
        // Profile metadata is best-effort after account creation; it must not undo successful auth.
        runCatching {
            firebaseAuth.currentUser?.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(name).build()
            )?.await()
        }
        runCatching { habitCloudDataSource.prepareForCurrentUser() }
        currentUser()
    }

    override suspend fun signInWithGoogle(idToken: String): AuthOutcome = safely {
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        runCatching { habitCloudDataSource.prepareForCurrentUser() }
        currentUser()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        firebaseAuth.sendPasswordResetEmail(email).await()
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
        AuthOutcome.Failure(
            error.localizedMessage?.takeIf(String::isNotBlank)
                ?: "Authentication failed. Please try again."
        )
    }
}
