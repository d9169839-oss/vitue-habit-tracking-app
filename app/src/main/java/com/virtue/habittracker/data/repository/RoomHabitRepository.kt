package com.virtue.habittracker.data.repository
import com.virtue.habittracker.data.local.HabitDao
import com.virtue.habittracker.data.local.HabitCheckInEntity
import com.virtue.habittracker.data.local.HabitEntity
import com.virtue.habittracker.domain.model.Habit
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.model.HabitDayStatus
import com.virtue.habittracker.domain.repository.HabitRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart

class RoomHabitRepository @Inject constructor(
    private val dao: HabitDao,
    private val cloud: HabitCloudDataSource
) : HabitRepository {
    override fun observeHabitsForDay(epochDay: Long): Flow<List<HabitDayEntry>> =
        combine(dao.observeHabitsForDay(epochDay), dao.observeCheckInsThroughDay(epochDay)) { habits, checkIns ->
            val recordsByHabit = checkIns.groupBy { it.habitId }
            habits.map { entity ->
                val recordsByDay = recordsByHabit[entity.id].orEmpty().associateBy { it.epochDay }
                val status = recordsByDay[epochDay]?.let {
                    if (it.completed) HabitDayStatus.COMPLETED else HabitDayStatus.NOT_COMPLETED
                } ?: HabitDayStatus.UNRECORDED
                var streak = 0
                var day = epochDay
                while (recordsByDay[day]?.completed == true) { streak++; day-- }
                HabitDayEntry(entity.toDomain(), status, streak)
            }
        }

    override suspend fun createHabit(title: String, description: String, createdEpochDay: Long) {
        val habit = HabitEntity(UUID.randomUUID().toString(), title, description, createdEpochDay)\n        dao.insertHabit(habit)\n        // Persist locally first; best-effort cloud write keeps creation usable offline.\n        runCatching { cloud.pushHabit(habit) }
    }

    override suspend fun setCompletion(habitId: String, epochDay: Long, completed: Boolean) {
        val checkIn = HabitCheckInEntity(habitId, epochDay, completed, System.currentTimeMillis())\n        dao.upsertCheckIn(checkIn)\n        runCatching { cloud.pushCheckIn(checkIn) }
    }

    override suspend fun clearCompletion(habitId: String, epochDay: Long) {
        dao.deleteCheckIn(habitId, epochDay)\n        runCatching { cloud.deleteCheckIn(habitId, epochDay) }
    }

    override suspend fun archiveHabit(habitId: String, inactiveFromEpochDay: Long) {
        dao.archiveHabit(habitId, inactiveFromEpochDay)\n        // The archived habit remains stored so its earlier history remains available.\n        dao.getHabitById(habitId)?.let { runCatching { cloud.pushHabit(it) } }
    }

    private fun HabitEntity.toDomain() = Habit(id, title, description, createdEpochDay, inactiveFromEpochDay)
}
