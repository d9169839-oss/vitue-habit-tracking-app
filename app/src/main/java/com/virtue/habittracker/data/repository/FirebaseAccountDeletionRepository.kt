package com.virtue.habittracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.virtue.habittracker.data.local.HabitDatabase
import com.virtue.habittracker.data.sync.HabitSyncScheduler
import com.virtue.habittracker.domain.repository.AccountDeletionRepository
import com.virtue.habittracker.reminders.HabitReminderScheduler
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RecentAuthenticationRequiredException : Exception(
    "For your security, sign out and sign in again, then retry account deletion."
)

@Singleton
class FirebaseAccountDeletionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions,
    private val database: HabitDatabase,
    private val cloudDataSource: HabitCloudDataSource,
    private val syncScheduler: HabitSyncScheduler,
    private val reminderScheduler: HabitReminderScheduler
) : AccountDeletionRepository {

    override suspend fun deleteCurrentAccount() {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("No signed-in account was found.")

        try {
            // The server writes a deletion tombstone before removing data, so any concurrent
            // WorkManager write is rejected by Firestore rules rather than recreating documents.
            functions.getHttpsCallable(DELETE_ACCOUNT_FUNCTION).call().await()
        } catch (error: Exception) {
            // The callable response can be lost after the server has already deleted Auth.
            // Reload distinguishes that completed operation from a genuinely failed request.
            val reloadError = auth.currentUser?.let { user ->
                runCatching { user.reload().await() }.exceptionOrNull()
            }
            if (auth.currentUser == null || (reloadError is FirebaseAuthException &&
                reloadError.errorCode == AUTH_USER_NOT_FOUND)
            ) {
                clearLocalAccount(uid)
                return
            }

            if (error is FirebaseFunctionsException &&
                error.code == FirebaseFunctionsException.Code.FAILED_PRECONDITION
            ) {
                throw RecentAuthenticationRequiredException()
            }
            throw error
        }

        // The server has deleted the Firestore tree and Firebase Auth user successfully.
        clearLocalAccount(uid)
    }

    private suspend fun clearLocalAccount(uid: String) {
        auth.signOut()
        // The server tombstone already prevents stale writes, so local cleanup can proceed even
        // if WorkManager reports that a job was already stopping.
        runCatching { syncScheduler.cancelPendingSync() }
        runCatching { reminderScheduler.cancelScheduledReminders() }
        cloudDataSource.clearLocalSyncMetadata(uid)
        // Room is the offline source of truth; clear every table, including the sync outbox.
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }

    private companion object {
        const val DELETE_ACCOUNT_FUNCTION = "deleteMyAccount"
        const val AUTH_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"
    }
}
