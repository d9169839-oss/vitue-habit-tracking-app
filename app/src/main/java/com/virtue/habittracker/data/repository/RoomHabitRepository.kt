package com.virtue.habittracker.data.repository

import com.virtue.habittracker.data.local.HabitCheckInEntity
import com.virtue.habittracker.data.local.HabitDao
import com.virtue.habittracker.data.local.HabitEntity
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

class RoomHabitRepository @Inject constructor(
    private val dao: HabitDao,
    private val cloud: HabitCloudDataSource
) : HabitRepository {

    override fun observeHabitsForDay(epochDay: Long): Flow<List<HabitDayEntry>> =
        observeEntries(epochDay, includeInactive = false)

    override fun observeHistoryForDay(epochDay: Long): Flow<List<HabitDayEntry>> =
        observeEntries(epochDay, includeInactive = true)

    /**
     * Room remains the fast source for the UI. Cloud restoration runs in parallel, so the
     * screen can render cached history immediately while Firestore fills any missing records.
     */
    private fun observeEntries(epochDay: Long, includeInactive: Boolean): Flow<List<HabitDayEntry>> =
        channelFlow {
            launch { runCatching { cloud.pullMissingRecords() } }

            val habitsFlow = if (includeInactive) {
                dao.observeAllHabitsCreatedByDay(epochDay)
            } else {
                dao.observeHabitsForDay(epochDay)
            }

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

    override suspend fun createHabit(title: String, description: String, createdEpochDay: Long) {
        val habit = HabitEntity(UUID.randomUUID().toString(), title, description, createdEpochDay)
        dao.insertHabit(habit)
        // Save locally first; an offline failure must not lose the user's action.
        runCatching { cloud.pushHabit(habit) }
    }

    override suspend fun setCompletion(habitId: String, epochDay: Long, completed: Boolean) {
        val checkIn = HabitCheckInEntity(habitId, epochDay, completed, System.currentTimeMillis())
        dao.upsertCheckIn(checkIn)
        runCatching { cloud.pushCheckIn(checkIn) }
    }

    override suspend fun clearCompletion(habitId: String, epochDay: Long) {
        dao.deleteCheckIn(habitId, epochDay)
        runCatching { cloud.deleteCheckIn(habitId, epochDay) }
    }

    override suspend fun archiveHabit(habitId: String, inactiveFromEpochDay: Long) {
        dao.archiveHabit(habitId, inactiveFromEpochDay)
        // Archiving changes future active lists but keeps the old habit and check-ins for history.
        dao.getHabitById(habitId)?.let { runCatching { cloud.pushHabit(it) } }
    }

    private fun HabitEntity.toDomain() =
        Habit(id, title, description, createdEpochDay, inactiveFromEpochDay)
}
