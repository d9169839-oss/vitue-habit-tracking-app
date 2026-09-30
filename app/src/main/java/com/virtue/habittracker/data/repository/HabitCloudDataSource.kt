package com.virtue.habittracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.virtue.habittracker.data.local.HabitDao
import com.virtue.habittracker.data.local.HabitCheckInEntity
import com.virtue.habittracker.data.local.HabitEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/**
 * Coordinates the cloud copy of habit data.
 *
 * Room remains the fast, local source used by the UI. Firestore documents are scoped
 * below users/{uid}, so one signed-in user can never accidentally share another user's data.
 * Passwords are intentionally never copied into the profile document.
 */
@Singleton
class HabitCloudDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val dao: HabitDao
) {
    private fun userIdOrNull(): String? = auth.currentUser?.uid

    private fun userDocument(uid: String) = firestore.collection("users").document(uid)

    /** Upload the current user's profile basics; Firebase Authentication owns credentials. */
    suspend fun syncProfile() {
        val user = auth.currentUser ?: return
        val profile = mapOf(
            "uid" to user.uid,
            "name" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "photoUrl" to user.photoUrl?.toString(),
            "updatedAtMillis" to System.currentTimeMillis()
        )
        userDocument(user.uid).set(profile, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    /**
     * Download records missing locally. IGNORE semantics intentionally avoid overwriting
     * offline/local records with older cloud data. A later sync-queue can add version-based
     * conflict resolution for edits made on multiple devices.
     */
    suspend fun pullMissingRecords() {
        val uid = userIdOrNull() ?: return
        syncProfile()

        val habitsSnapshot = userDocument(uid).collection("habits").get().await()
        val habits = habitsSnapshot.documents.mapNotNull { document ->
            val id = document.getString("id") ?: document.id
            val title = document.getString("title") ?: return@mapNotNull null
            HabitEntity(
                id = id,
                title = title,
                description = document.getString("description").orEmpty(),
                createdEpochDay = document.getLong("createdEpochDay") ?: return@mapNotNull null,
                inactiveFromEpochDay = document.getLong("inactiveFromEpochDay")
            )
        }
        if (habits.isNotEmpty()) dao.insertHabitsIfMissing(habits)

        val checkInsSnapshot = userDocument(uid).collection("habitCheckIns").get().await()
        val checkIns = checkInsSnapshot.documents.mapNotNull { document ->
            val habitId = document.getString("habitId") ?: return@mapNotNull null
            val epochDay = document.getLong("epochDay") ?: return@mapNotNull null
            val completed = document.getBoolean("completed") ?: return@mapNotNull null
            HabitCheckInEntity(
                habitId = habitId,
                epochDay = epochDay,
                completed = completed,
                updatedAtMillis = document.getLong("updatedAtMillis") ?: 0L
            )
        }
        if (checkIns.isNotEmpty()) dao.insertCheckInsIfMissing(checkIns)

        // Retry local-only records as well. This covers writes made while the device was offline.
        pushLocalRecords(uid)
    }

    private suspend fun pushLocalRecords(uid: String) {
        val userRef = userDocument(uid)
        val habits = dao.getAllHabits()
        val checkIns = dao.getAllCheckIns()

        // Firestore batches have a write limit. Keep each batch comfortably below that limit.
        val habitChunks = habits.chunked(350)
        val checkInChunks = checkIns.chunked(350)
        habitChunks.forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { habit ->
                batch.set(userRef.collection("habits").document(habit.id), mapOf(
                    "id" to habit.id,
                    "title" to habit.title,
                    "description" to habit.description,
                    "createdEpochDay" to habit.createdEpochDay,
                    "inactiveFromEpochDay" to habit.inactiveFromEpochDay,
                    "updatedAtMillis" to System.currentTimeMillis()
                ), com.google.firebase.firestore.SetOptions.merge())
            }
            batch.commit().await()
        }
        checkInChunks.forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { checkIn ->
                batch.set(userRef.collection("habitCheckIns")
                    .document("${checkIn.habitId}_${checkIn.epochDay}"), mapOf(
                        "habitId" to checkIn.habitId,
                        "epochDay" to checkIn.epochDay,
                        "completed" to checkIn.completed,
                        "updatedAtMillis" to checkIn.updatedAtMillis
                    ), com.google.firebase.firestore.SetOptions.merge())
            }
            batch.commit().await()
        }
    }

    suspend fun pushHabit(habit: HabitEntity) {
        val uid = userIdOrNull() ?: return // Allow local-first use while signed out.
        syncProfile()
        val data = mapOf(
            "id" to habit.id,
            "title" to habit.title,
            "description" to habit.description,
            "createdEpochDay" to habit.createdEpochDay,
            "inactiveFromEpochDay" to habit.inactiveFromEpochDay,
            "updatedAtMillis" to System.currentTimeMillis()
        )
        userDocument(uid).collection("habits").document(habit.id)
            .set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    suspend fun pushCheckIn(checkIn: HabitCheckInEntity) {
        val uid = userIdOrNull() ?: return
        syncProfile()
        // A deterministic document ID makes setting the same habit/date idempotent.
        val documentId = "${checkIn.habitId}_${checkIn.epochDay}"
        val data = mapOf(
            "habitId" to checkIn.habitId,
            "epochDay" to checkIn.epochDay,
            "completed" to checkIn.completed,
            "updatedAtMillis" to checkIn.updatedAtMillis
        )
        userDocument(uid).collection("habitCheckIns").document(documentId)
            .set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    suspend fun deleteCheckIn(habitId: String, epochDay: Long) {
        val uid = userIdOrNull() ?: return
        userDocument(uid).collection("habitCheckIns").document("${habitId}_${epochDay}").delete().await()
    }
}
