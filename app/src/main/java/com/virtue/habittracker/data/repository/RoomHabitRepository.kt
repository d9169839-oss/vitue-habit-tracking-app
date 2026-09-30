package com.virtue.habittracker.data.repository

import com.virtue.habittracker.data.local.HabitCheckInEntity
import com.virtue.habittracker.data.local.HabitDao
import com.virtue.habittracker.data.local.HabitEntity
import com.virtue.habittracker.data.sync.HabitSyncScheduler
import com.virtue.habittracker.domain.model.Habit
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.model.HabitDayStatus
import com.virtue.habittracker.domain.repository.HabitRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Local-first repository. User actions write only to Room and its durable outbox.
 * Firestore is handled separately by a network-constrained WorkManager worker.
 */
class RoomHabitRepository @Inject constructor(
    private val dao: HabitDao,
    private val cloud: HabitCloudDataSource,
    private val syncScheduler: HabitSyncScheduler
) : HabitRepository {

    override fun observeHabitsForDay(epochDay: Long): Flow<List<HabitDayEntry>> =
        observeEntries(epochDay, includeInactive = false)

    override fun observeHistoryForDay(epochDay: Long): Flow<List<HabitDayEntry>> =
        observeEntries(epochDay, includeInactive = true)

    /**
     * Account ownership is checked locally before exposing Room data. No Firestore request is
     * required to render Home or History; a background sync is merely scheduled afterward.
     */
    private fun observeEntries(epochDay: Long, includeInactive: Boolean): Flow<List<HabitDayEntry>> =
        channelFlow {
            runCatching { cloud.prepareLocalCacheForCurrentUser() }
            syncScheduler.enqueueSync()

            val habitsFlow = if (includeInactive) {
                dao.observeAllHabitsCreatedByDay(epochDay)
            } else {
                dao.observeHabitsForDay(epochDay)
            }

            launch {
                combine(habitsFlow, dao.observeCheckInsThroughDay(epochDay)) { habits, checkIns ->
                    val recordsByHabit = checkIns.groupBy { it.habitId }
                    habits.map { entity ->
                        val recordsByDay = recordsByHabit[entity.id].orEmpty().associateBy { it.epochDay }
                        val status = recordsByDay[epochDay]?.let {
                            if (it.completed) HabitDayStatus.COMPLETED else HabitDayStatus.NOT_COMPLETED
                        } ?: HabitDayStatus.UNRECORDED

                        // A streak is derived from consecutive completed dates, not a stored counter.
                        var streak = 0
                        var day = epochDay
                        while (recordsByDay[day]?.completed == true) {
                            streak++
                            day--
                        }

                        val activeOnDate = entity.inactiveFromEpochDay == null ||
                            epochDay < entity.inactiveFromEpochDay
                        HabitDayEntry(
                            habit = entity.toDomain(),
                            status = status,
                            currentStreak = streak,
                            isActiveOnDate = activeOnDate
                        )
                    }
                }.collect { send(it) }
            }
        }

    override suspend fun createHabit(title: String, description: String, createdEpochDay: Long) {
        val habit = HabitEntity(UUID.randomUUID().toString(), title, description, createdEpochDay)
        // Habit + outbox row are one Room transaction. Network is not involved in this action.
        dao.saveHabitAndQueue(habit)
        syncScheduler.enqueueSync()
    }

    override suspend fun setCompletion(habitId: String, epochDay: Long, completed: Boolean) {
        val checkIn = HabitCheckInEntity(habitId, epochDay, completed, System.currentTimeMillis())
        dao.saveCheckInAndQueue(checkIn)
        syncScheduler.enqueueSync()
    }

    override suspend fun clearCompletion(habitId: String, epochDay: Long) {
        // The durable DELETE operation becomes a cloud tombstone during sync.
        dao.clearCheckInAndQueue(habitId, epochDay)
        syncScheduler.enqueueSync()
    }

    override suspend fun archiveHabit(habitId: String, inactiveFromEpochDay: Long) {
        dao.archiveHabitAndQueue(habitId, inactiveFromEpochDay)
        syncScheduler.enqueueSync()
    }

    private fun HabitEntity.toDomain() =
        Habit(id, title, description, createdEpochDay, inactiveFromEpochDay)
}
