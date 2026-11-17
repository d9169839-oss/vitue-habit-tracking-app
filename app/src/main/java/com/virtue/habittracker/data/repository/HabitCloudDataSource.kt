package com.virtue.habittracker.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.virtue.habittracker.data.local.HabitDao
import com.virtue.habittracker.data.local.HabitCheckInEntity
import com.virtue.habittracker.data.local.HabitEntity
import com.virtue.habittracker.data.local.SyncOperationEntity
import com.virtue.habittracker.data.local.ProgramDao
import com.virtue.habittracker.data.local.ProgramEnrollmentEntity
import com.virtue.habittracker.data.local.ProgramActivityEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Cloud boundary for Vitue's local-first sync.
 *
 * Normal habit actions never call Firestore. Room mutations and outbox entries commit first;
 * WorkManager later uploads only queued entities. Initial snapshots hydrate Room before the first
 * signed-in Home/History emission; listeners then merge live changes, with incremental WorkManager
 * pulls retained as the offline/process-death recovery path.
 */
@Singleton
class HabitCloudDataSource @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val dao: HabitDao,
    private val programDao: ProgramDao
) {
    private fun userIdOrNull(): String? = auth.currentUser?.uid

    private val _initialRealtimeSnapshotReady = MutableStateFlow(false)
    private var initialHabitsSnapshotReady = false
    private var initialCheckInsSnapshotReady = false
    private val _initialProgramSnapshotReady = MutableStateFlow(false)
    private var initialProgramEnrollmentsSnapshotReady = false
    private var initialProgramActivitiesSnapshotReady = false

    /**
     * Wait until the first authoritative Firestore snapshot for both habit collections has been
     * merged into Room. When offline, the cached snapshot is the best available initial dataset.
     */
    suspend fun awaitInitialRealtimeSnapshot() {
        val expectedUid = auth.currentUser?.uid ?: return
        // A bounded fallback prevents a stalled network/listener from trapping the user on splash.
        withTimeoutOrNull(INITIAL_REALTIME_SYNC_TIMEOUT_MILLIS) {
            _initialRealtimeSnapshotReady.first {
                auth.currentUser?.uid != expectedUid || it
            }
        }
    }

    /** Programs are also hydrated before their first catalogue snapshot is exposed. */
    suspend fun awaitInitialProgramSnapshot() {
        val expectedUid = auth.currentUser?.uid ?: return
        withTimeoutOrNull(INITIAL_REALTIME_SYNC_TIMEOUT_MILLIS) {
            _initialProgramSnapshotReady.first {
                auth.currentUser?.uid != expectedUid || it
            }
        }
    }

    private fun hasValidatedInternet(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    @Synchronized
    private fun resetInitialSnapshotState(uid: String?) {
        initialHabitsSnapshotReady = false
        initialCheckInsSnapshotReady = false
        initialProgramEnrollmentsSnapshotReady = false
        initialProgramActivitiesSnapshotReady = false
        _initialRealtimeSnapshotReady.value = uid == null
        _initialProgramSnapshotReady.value = uid == null
    }

    @Synchronized
    private fun markInitialSnapshotReady(uid: String, collection: String) {
        if (auth.currentUser?.uid != uid) return
        if (collection == HABITS_COLLECTION) initialHabitsSnapshotReady = true
        if (collection == CHECK_INS_COLLECTION) initialCheckInsSnapshotReady = true
        if (initialHabitsSnapshotReady && initialCheckInsSnapshotReady) {
            _initialRealtimeSnapshotReady.value = true
        }
    }

    @Synchronized
    private fun markInitialProgramSnapshotReady(uid: String, collection: String) {
        if (auth.currentUser?.uid != uid) return
        if (collection == PROGRAM_ENROLLMENTS_COLLECTION) initialProgramEnrollmentsSnapshotReady = true
        if (collection == PROGRAM_ACTIVITIES_COLLECTION) initialProgramActivitiesSnapshotReady = true
        if (initialProgramEnrollmentsSnapshotReady && initialProgramActivitiesSnapshotReady) {
            _initialProgramSnapshotReady.value = true
        }
    }
    private fun userDocument(uid: String) = firestore.collection("users").document(uid)
    private fun syncPreferences(uid: String) =
        context.getSharedPreferences("habit_sync_state_$uid", Context.MODE_PRIVATE)

    /** Remove only this UID's local sync cursor/preferences after successful account deletion. */
    fun clearLocalSyncMetadata(uid: String) {
        syncPreferences(uid).edit().clear().apply()
        val ownerPreferences = context.getSharedPreferences("habit_cache_owner", Context.MODE_PRIVATE)
        if (ownerPreferences.getString("uid", null) == uid) {
            ownerPreferences.edit().remove("uid").apply()
        }
    }

    private suspend fun ensureLocalOwner(uid: String) {
        val preferences = context.getSharedPreferences("habit_cache_owner", Context.MODE_PRIVATE)
        val previousUid = preferences.getString("uid", null)
        if (previousUid != null && previousUid != uid) {
            // Never expose one account's cache or queued operations to another account.
            dao.deleteAllSyncOperations()
            dao.deleteAllCheckIns()
            dao.deleteAllHabits()
            programDao.deleteAllActivities()
            programDao.deleteAllEnrollments()
        }
        preferences.edit().putString("uid", uid).apply()
    }

    /** Clear a previous account's local cache before exposing data; this method is local-only. */
    suspend fun prepareLocalCacheForCurrentUser() {
        val uid = userIdOrNull() ?: return
        ensureLocalOwner(uid)
    }

    /**
     * Authentication must not wait for Firestore. Prepare the local owner and mark the profile
     * for background refresh; WorkManager will perform the network work when connectivity exists.
     */
    suspend fun prepareForCurrentUser() {
        val uid = userIdOrNull() ?: return
        ensureLocalOwner(uid)
        syncPreferences(uid).edit().putBoolean("profileSyncPending", true).apply()
    }

    suspend fun syncProfileIfPending() {
        val uid = userIdOrNull() ?: return
        val preferences = syncPreferences(uid)
        if (!preferences.getBoolean("profileSyncPending", false)) return
        syncProfile()
        preferences.edit().putBoolean("profileSyncPending", false).apply()
    }

    /** Profile metadata is intentionally independent from habit writes. */
    suspend fun syncProfile() {
        val user = auth.currentUser ?: return
        val ref = userDocument(user.uid)
        val existing = ref.get().await()
        val now = System.currentTimeMillis()
        val profile = mapOf(
            "uid" to user.uid,
            "name" to (user.displayName?.takeIf(String::isNotBlank)
                ?: user.email?.substringBefore("@").orEmpty()),
            "email" to (user.email ?: ""),
            "photoUrl" to user.photoUrl?.toString(),
            "createdAtMillis" to (existing.getLong("createdAtMillis") ?: now),
            "updatedAtMillis" to now
        )
        ref.set(profile, SetOptions.merge()).await()
    }

    /**
     * Upload only dirty entities. A queued row is deleted only if its revision is unchanged
     * after Firestore confirms the corresponding batch, so edits made during upload stay queued.
     */
    suspend fun syncPendingChanges() {
        val uid = userIdOrNull() ?: return
        ensureLocalOwner(uid)
        val userRef = userDocument(uid)
        while (true) {
            val pending = dao.getPendingSyncOperations()
            if (pending.isEmpty()) return

            // Stay comfortably below Firestore's 500-write batch limit.
            pending.chunked(350).forEach { chunk ->
                val batch = firestore.batch()
                var hasWrites = false
                val operationsToAcknowledge = mutableListOf<SyncOperationEntity>()

                for (operation in chunk) {
                    when (operation.entityType) {
                        "HABIT" -> {
                            val habit = dao.getHabitById(operation.entityId)
                            if (habit == null) {
                                operationsToAcknowledge += operation
                            } else {
                                batch.set(userRef.collection("habits").document(habit.id), mapOf(
                                    "id" to habit.id,
                                    "title" to habit.title,
                                    "description" to habit.description,
                                    "createdEpochDay" to habit.createdEpochDay,
                                    "inactiveFromEpochDay" to habit.inactiveFromEpochDay,
                                    "updatedAtMillis" to System.currentTimeMillis()
                                ), SetOptions.merge())
                                hasWrites = true
                                operationsToAcknowledge += operation
                            }
                        }
                        "PROGRAM_ENROLLMENT" -> {
                            val ref = userRef.collection("programEnrollments").document(operation.entityId)
                            val enrollment = programDao.getEnrollment(operation.entityId)
                            if (operation.operationType == "DELETE" || enrollment == null) {
                                batch.set(ref, mapOf("id" to operation.entityId, "deleted" to true,
                                    "updatedAtMillis" to System.currentTimeMillis()), SetOptions.merge())
                            } else {
                                batch.set(ref, mapOf(
                                    "id" to enrollment.id, "templateId" to enrollment.templateId,
                                    "templateVersion" to enrollment.templateVersion, "titleSnapshot" to enrollment.titleSnapshot,
                                    "category" to enrollment.category, "durationDays" to enrollment.durationDays,
                                    "startEpochDay" to enrollment.startEpochDay, "status" to enrollment.status,
                                    "availableMinutesPerDay" to enrollment.availableMinutesPerDay,
                                    "availableDaysPerWeek" to enrollment.availableDaysPerWeek,
                                    "experience" to enrollment.experience, "workActivityLevel" to enrollment.workActivityLevel, "equipment" to enrollment.equipment,
                                    "isPremium" to enrollment.isPremium, "createdAtMillis" to enrollment.createdAtMillis,
                                    "updatedAtMillis" to maxOf(enrollment.updatedAtMillis, System.currentTimeMillis()),
                                    "deleted" to false
                                ), SetOptions.merge())
                            }
                            hasWrites = true
                            operationsToAcknowledge += operation
                        }
                        "PROGRAM_ACTIVITY" -> {
                            val ref = userRef.collection("programActivities").document(operation.entityId)
                            val activity = programDao.getActivity(operation.entityId)
                            if (activity == null) {
                                batch.set(ref, mapOf("id" to operation.entityId, "deleted" to true,
                                    "updatedAtMillis" to System.currentTimeMillis()), SetOptions.merge())
                            } else {
                                batch.set(ref, mapOf(
                                    "id" to activity.id, "enrollmentId" to activity.enrollmentId,
                                    "dayIndex" to activity.dayIndex, "epochDay" to activity.epochDay,
                                    "phaseTitle" to activity.phaseTitle, "title" to activity.title,
                                    "instructions" to activity.instructions, "estimatedMinutes" to activity.estimatedMinutes,
                                    "status" to activity.status,
                                    "updatedAtMillis" to maxOf(activity.updatedAtMillis, System.currentTimeMillis()),
                                    "deleted" to false
                                ), SetOptions.merge())
                            }
                            hasWrites = true
                            operationsToAcknowledge += operation
                        }
                        "CHECK_IN" -> {
                            val parts = operation.entityId.split("_")
                            val epochDay = parts.lastOrNull()?.toLongOrNull()
                            val habitId = if (epochDay == null) null
                                else operation.entityId.removeSuffix("_$epochDay")
                            if (habitId == null || epochDay == null) {
                                operationsToAcknowledge += operation
                            } else {
                                val ref = userRef.collection("habitCheckIns").document(operation.entityId)
                                val local = dao.getCheckIn(habitId, epochDay)
                                if (operation.operationType == "DELETE" || local == null) {
                                    // Keep a tombstone so other devices learn about the deletion.
                                    batch.set(ref, mapOf(
                                        "habitId" to habitId,
                                        "epochDay" to epochDay,
                                        "deleted" to true,
                                        "updatedAtMillis" to System.currentTimeMillis()
                                    ), SetOptions.merge())
                                    hasWrites = true
                                } else {
                                    batch.set(ref, mapOf(
                                        "habitId" to habitId,
                                        "epochDay" to epochDay,
                                        "completed" to local.completed,
                                        "deleted" to false,
                                        "updatedAtMillis" to maxOf(local.updatedAtMillis, System.currentTimeMillis())
                                    ), SetOptions.merge())
                                    hasWrites = true
                                }
                                operationsToAcknowledge += operation
                            }
                        }
                    }
                }

                // Firestore rejects empty batches. Acknowledgement only follows a successful commit.
                if (hasWrites) batch.commit().await()
                operationsToAcknowledge.forEach { op ->
                    dao.deleteSyncOperationIfUnchanged(op.operationKey, op.queuedAtMillis)
                }
            }
        }
    }

    /**
     * Initial sync downloads the account once. Later syncs query only changed documents.
     * The cursor advances only after both collections have been read and merged successfully.
     */
    suspend fun pullChangesSinceLastSync() {
        val uid = userIdOrNull() ?: return
        ensureLocalOwner(uid)
        val preferences = syncPreferences(uid)
        val lastSyncMillis = preferences.getLong("lastSuccessfulPullMillis", 0L)
        // Local edits can enqueue frequent uploads. Avoid paying for two cloud queries on every tap.
        // Cross-device changes are refreshed at most every 15 minutes, plus the initial full pull.
        if (lastSyncMillis != 0L &&
            System.currentTimeMillis() - lastSyncMillis < MIN_PULL_INTERVAL_MILLIS
        ) return
        val userRef = userDocument(uid)

        val habitsQuery = userRef.collection("habits")
        val checkInsQuery = userRef.collection("habitCheckIns")
        val habitsSnapshot = if (lastSyncMillis == 0L) habitsQuery.get().await()
            else habitsQuery.whereGreaterThan("updatedAtMillis", lastSyncMillis).get().await()
        val checkInsSnapshot = if (lastSyncMillis == 0L) checkInsQuery.get().await()
            else checkInsQuery.whereGreaterThan("updatedAtMillis", lastSyncMillis).get().await()

        for (document in habitsSnapshot.documents) {
            val id = document.getString("id") ?: document.id
            if (dao.hasPendingSyncOperation("habit:$id")) continue
            val title = document.getString("title") ?: continue
            val created = document.getLong("createdEpochDay") ?: continue
            dao.upsertHabitFromCloud(HabitEntity(
                id = id,
                title = title,
                description = document.getString("description").orEmpty(),
                createdEpochDay = created,
                inactiveFromEpochDay = document.getLong("inactiveFromEpochDay")
            ))
        }

        for (document in checkInsSnapshot.documents) {
            val habitId = document.getString("habitId") ?: continue
            val epochDay = document.getLong("epochDay") ?: continue
            val key = "checkin:${habitId}_${epochDay}"
            if (dao.hasPendingSyncOperation(key)) continue

            if (document.getBoolean("deleted") == true) {
                dao.deleteCheckIn(habitId, epochDay)
            } else {
                val completed = document.getBoolean("completed") ?: continue
                dao.upsertCheckInFromCloud(HabitCheckInEntity(
                    habitId = habitId,
                    epochDay = epochDay,
                    completed = completed,
                    updatedAtMillis = document.getLong("updatedAtMillis") ?: 0L
                ))
            }
        }

        val enrollmentsQuery = userRef.collection("programEnrollments")
        val activitiesQuery = userRef.collection("programActivities")
        val enrollmentsSnapshot = if (lastSyncMillis == 0L) enrollmentsQuery.get().await()
            else enrollmentsQuery.whereGreaterThan("updatedAtMillis", lastSyncMillis).get().await()
        val activitiesSnapshot = if (lastSyncMillis == 0L) activitiesQuery.get().await()
            else activitiesQuery.whereGreaterThan("updatedAtMillis", lastSyncMillis).get().await()

        for (document in enrollmentsSnapshot.documents) {
            val id = document.getString("id") ?: document.id
            if (dao.hasPendingSyncOperation("program-enrollment:$id")) continue
            if (document.getBoolean("deleted") == true) {
                programDao.deleteActivitiesForEnrollment(id)
                programDao.deleteEnrollmentRow(id)
                continue
            }
            val title = document.getString("titleSnapshot") ?: continue
            val templateId = document.getString("templateId") ?: continue
            val entity = ProgramEnrollmentEntity(
                id = id, templateId = templateId,
                templateVersion = (document.getLong("templateVersion") ?: 1L).toInt(),
                titleSnapshot = title, category = document.getString("category") ?: "FITNESS",
                durationDays = (document.getLong("durationDays") ?: 30L).toInt(),
                startEpochDay = document.getLong("startEpochDay") ?: continue,
                status = document.getString("status") ?: "ACTIVE",
                availableMinutesPerDay = (document.getLong("availableMinutesPerDay") ?: 15L).toInt(),
                availableDaysPerWeek = (document.getLong("availableDaysPerWeek") ?: 5L).toInt(),
                experience = document.getString("experience") ?: "BEGINNER",
                workActivityLevel = document.getString("workActivityLevel") ?: "MIXED",
                equipment = document.getString("equipment") ?: "NONE",
                isPremium = document.getBoolean("isPremium") ?: false,
                createdAtMillis = document.getLong("createdAtMillis") ?: 0L,
                updatedAtMillis = document.getLong("updatedAtMillis") ?: 0L
            )
            programDao.upsertEnrollment(entity)
        }

        for (document in activitiesSnapshot.documents) {
            val id = document.getString("id") ?: document.id
            if (dao.hasPendingSyncOperation("program-activity:$id")) continue
            if (document.getBoolean("deleted") == true) {
                programDao.deleteActivityRow(id)
                continue
            }
            val enrollmentId = document.getString("enrollmentId") ?: continue
            // Ignore orphan activity documents after an enrollment tombstone was applied.
            if (programDao.getEnrollment(enrollmentId) == null) continue
            val dayIndex = (document.getLong("dayIndex") ?: continue).toInt()
            val entity = ProgramActivityEntity(
                id = id, enrollmentId = enrollmentId, dayIndex = dayIndex,
                epochDay = document.getLong("epochDay") ?: continue,
                phaseTitle = document.getString("phaseTitle") ?: "",
                title = document.getString("title") ?: continue,
                instructions = document.getString("instructions") ?: "",
                estimatedMinutes = (document.getLong("estimatedMinutes") ?: 0L).toInt(),
                status = document.getString("status") ?: "PENDING",
                updatedAtMillis = document.getLong("updatedAtMillis") ?: 0L
            )
            programDao.upsertActivity(entity)
        }

        // Persist only after both reads and all Room merges have succeeded.
        preferences.edit().putLong("lastSuccessfulPullMillis", System.currentTimeMillis()).apply()
        if (lastSyncMillis == 0L) {
            // WorkManager's initial full pull is also a valid hydration barrier if listeners lag.
            markInitialSnapshotReady(uid, HABITS_COLLECTION)
            markInitialSnapshotReady(uid, CHECK_INS_COLLECTION)
            markInitialProgramSnapshotReady(uid, PROGRAM_ENROLLMENTS_COLLECTION)
            markInitialProgramSnapshotReady(uid, PROGRAM_ACTIVITIES_COLLECTION)
        }
    }

    /**
     * Starts account-scoped Firestore listeners for habit definitions and daily check-ins.
     *
     * Room remains the UI source of truth: listener events are merged into Room, never rendered
     * directly. The existing WorkManager outbox remains responsible for reliable uploads and
     * periodic catch-up when a device is offline or a listener is unavailable.
     */
    fun startRealtimeSync() {
        if (realtimeAuthListener != null) return

        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid == realtimeUid) return@AuthStateListener

            removeRealtimeRegistrations()
            realtimeUid = uid
            resetInitialSnapshotState(uid)

            if (uid != null) {
                realtimeScope.launch {
                    runCatching { ensureLocalOwner(uid) }
                        .onSuccess {
                            // Do not attach a stale account listener if auth changed during setup.
                            if (auth.currentUser?.uid == uid) attachRealtimeListeners(uid)
                        }
                }
            }
        }
        realtimeAuthListener = listener
        auth.addAuthStateListener(listener)
    }

    @Synchronized
    private fun attachRealtimeListeners(uid: String) {
        removeRealtimeRegistrations()
        if (auth.currentUser?.uid != uid) return

        val userRef = userDocument(uid)
        realtimeRegistrations += userRef.collection("habits")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (auth.currentUser?.uid != uid) return@addSnapshotListener
                if (error != null || snapshot == null) {
                    // A denied/failed listener must not hold the app on its splash indefinitely.
                    markInitialSnapshotReady(uid, HABITS_COLLECTION)
                    return@addSnapshotListener
                }

                realtimeScope.launch {
                    if (auth.currentUser?.uid != uid) return@launch
                    snapshot.documentChanges.forEach { change ->
                        val document = change.document
                        val habitId = document.getString("id") ?: document.id
                        runCatching {
                            if (auth.currentUser?.uid != uid) return@runCatching
                            // An unuploaded local edit always wins over an incoming remote event.
                            if (dao.hasPendingSyncOperation("habit:$habitId")) return@runCatching
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                return@runCatching
                            }
                            val title = document.getString("title") ?: return@runCatching
                            val createdEpochDay = document.getLong("createdEpochDay")
                                ?: return@runCatching

                            dao.upsertHabitFromCloud(
                                HabitEntity(
                                    id = habitId,
                                    title = title,
                                    description = document.getString("description").orEmpty(),
                                    createdEpochDay = createdEpochDay,
                                    inactiveFromEpochDay = document.getLong("inactiveFromEpochDay")
                                )
                            )
                        }
                    }
                    // Online startup waits for the server snapshot, not an empty cache snapshot.
                    if (!snapshot.metadata.isFromCache || !hasValidatedInternet()) {
                        markInitialSnapshotReady(uid, HABITS_COLLECTION)
                    }
                }
            }

        realtimeRegistrations += userRef.collection("habitCheckIns")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (auth.currentUser?.uid != uid) return@addSnapshotListener
                if (error != null || snapshot == null) {
                    markInitialSnapshotReady(uid, CHECK_INS_COLLECTION)
                    return@addSnapshotListener
                }

                realtimeScope.launch {
                    if (auth.currentUser?.uid != uid) return@launch
                    snapshot.documentChanges.forEach { change ->
                        val document = change.document
                        val habitId = document.getString("habitId") ?: return@forEach
                        val epochDay = document.getLong("epochDay") ?: return@forEach
                        val operationKey = "checkin:${habitId}_${epochDay}"

                        runCatching {
                            if (auth.currentUser?.uid != uid) return@runCatching
                            // Do not overwrite a check-in that this device has not uploaded yet.
                            if (dao.hasPendingSyncOperation(operationKey)) return@runCatching

                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED ||
                                document.getBoolean("deleted") == true
                            ) {
                                dao.deleteCheckIn(habitId, epochDay)
                            } else {
                                val completed = document.getBoolean("completed")
                                    ?: return@runCatching
                                dao.upsertCheckInFromCloud(
                                    HabitCheckInEntity(
                                        habitId = habitId,
                                        epochDay = epochDay,
                                        completed = completed,
                                        updatedAtMillis = document.getLong("updatedAtMillis") ?: 0L
                                    )
                                )
                            }
                        }
                    }
                    if (!snapshot.metadata.isFromCache || !hasValidatedInternet()) {
                        markInitialSnapshotReady(uid, CHECK_INS_COLLECTION)
                    }
                }
            }

        // Program state is live-synced too, so a completion made on another device is reflected
        // in Room immediately rather than waiting for the periodic WorkManager pull.
        realtimeRegistrations += userRef.collection(PROGRAM_ENROLLMENTS_COLLECTION)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (auth.currentUser?.uid != uid) return@addSnapshotListener
                if (error != null || snapshot == null) {
                    markInitialProgramSnapshotReady(uid, PROGRAM_ENROLLMENTS_COLLECTION)
                    return@addSnapshotListener
                }

                realtimeScope.launch {
                    if (auth.currentUser?.uid != uid) return@launch
                    snapshot.documentChanges.forEach { change ->
                        val document = change.document
                        val enrollmentId = document.getString("id") ?: document.id
                        runCatching {
                            if (auth.currentUser?.uid != uid) return@runCatching
                            if (dao.hasPendingSyncOperation("program-enrollment:$enrollmentId")) return@runCatching

                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED ||
                                document.getBoolean("deleted") == true
                            ) {
                                programDao.deleteActivitiesForEnrollment(enrollmentId)
                                programDao.deleteEnrollmentRow(enrollmentId)
                            } else {
                                val title = document.getString("titleSnapshot") ?: return@runCatching
                                val templateId = document.getString("templateId") ?: return@runCatching
                                programDao.upsertEnrollment(
                                    ProgramEnrollmentEntity(
                                        id = enrollmentId,
                                        templateId = templateId,
                                        templateVersion = (document.getLong("templateVersion") ?: 1L).toInt(),
                                        titleSnapshot = title,
                                        category = document.getString("category") ?: "FITNESS",
                                        durationDays = (document.getLong("durationDays") ?: 30L).toInt(),
                                        startEpochDay = document.getLong("startEpochDay") ?: return@runCatching,
                                        status = document.getString("status") ?: "ACTIVE",
                                        availableMinutesPerDay = (document.getLong("availableMinutesPerDay") ?: 15L).toInt(),
                                        availableDaysPerWeek = (document.getLong("availableDaysPerWeek") ?: 5L).toInt(),
                                        experience = document.getString("experience") ?: "BEGINNER",
                                        workActivityLevel = document.getString("workActivityLevel") ?: "MIXED",
                                        equipment = document.getString("equipment") ?: "NONE",
                                        isPremium = document.getBoolean("isPremium") ?: false,
                                        createdAtMillis = document.getLong("createdAtMillis") ?: 0L,
                                        updatedAtMillis = document.getLong("updatedAtMillis") ?: 0L
                                    )
                                )
                            }
                        }
                    }
                    if (!snapshot.metadata.isFromCache || !hasValidatedInternet()) {
                        markInitialProgramSnapshotReady(uid, PROGRAM_ENROLLMENTS_COLLECTION)
                    }
                }
            }

        realtimeRegistrations += userRef.collection(PROGRAM_ACTIVITIES_COLLECTION)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (auth.currentUser?.uid != uid) return@addSnapshotListener
                if (error != null || snapshot == null) {
                    markInitialProgramSnapshotReady(uid, PROGRAM_ACTIVITIES_COLLECTION)
                    return@addSnapshotListener
                }

                realtimeScope.launch {
                    if (auth.currentUser?.uid != uid) return@launch
                    snapshot.documentChanges.forEach { change ->
                        val document = change.document
                        val activityId = document.getString("id") ?: document.id
                        runCatching {
                            if (auth.currentUser?.uid != uid) return@runCatching
                            if (dao.hasPendingSyncOperation("program-activity:$activityId")) return@runCatching

                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED ||
                                document.getBoolean("deleted") == true
                            ) {
                                programDao.deleteActivityRow(activityId)
                            } else {
                                val enrollmentId = document.getString("enrollmentId") ?: return@runCatching
                                val dayIndex = (document.getLong("dayIndex") ?: return@runCatching).toInt()
                                programDao.upsertActivity(
                                    ProgramActivityEntity(
                                        id = activityId,
                                        enrollmentId = enrollmentId,
                                        dayIndex = dayIndex,
                                        epochDay = document.getLong("epochDay") ?: return@runCatching,
                                        phaseTitle = document.getString("phaseTitle") ?: "",
                                        title = document.getString("title") ?: return@runCatching,
                                        instructions = document.getString("instructions") ?: "",
                                        estimatedMinutes = (document.getLong("estimatedMinutes") ?: 0L).toInt(),
                                        status = document.getString("status") ?: "PENDING",
                                        updatedAtMillis = document.getLong("updatedAtMillis") ?: 0L
                                    )
                                )
                            }
                        }
                    }
                    if (!snapshot.metadata.isFromCache || !hasValidatedInternet()) {
                        markInitialProgramSnapshotReady(uid, PROGRAM_ACTIVITIES_COLLECTION)
                    }
                }
            }
    }

    @Synchronized
    private fun removeRealtimeRegistrations() {
        realtimeRegistrations.forEach { it.remove() }
        realtimeRegistrations.clear()
    }

    private companion object {
        const val MIN_PULL_INTERVAL_MILLIS = 15 * 60 * 1000L
        const val HABITS_COLLECTION = "habits"
        const val CHECK_INS_COLLECTION = "habitCheckIns"
        const val PROGRAM_ENROLLMENTS_COLLECTION = "programEnrollments"
        const val PROGRAM_ACTIVITIES_COLLECTION = "programActivities"
        const val INITIAL_REALTIME_SYNC_TIMEOUT_MILLIS = 12_000L
    }

    private val realtimeScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO
    )
    private val realtimeRegistrations = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
    @Volatile private var realtimeUid: String? = null
    @Volatile private var realtimeAuthListener: FirebaseAuth.AuthStateListener? = null
}
