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

class RoomHabitRepository @Inject constructor(private val dao: HabitDao) : HabitRepository {
    override fun observeHabitsForDay(epochDay: Long): Flow<List<HabitDayEntry>> =
        combine(dao.observeHabitsForDay(epochDay), dao.observeCheckInsForDay(epochDay)) { habits, checkIns ->
            val statusByHabit = checkIns.associateBy { it.habitId }
            habits.map { entity ->
                val status = statusByHabit[entity.id]?.let {
                    if (it.completed) HabitDayStatus.COMPLETED else HabitDayStatus.NOT_COMPLETED
                } ?: HabitDayStatus.UNRECORDED
                HabitDayEntry(entity.toDomain(), status)
            }
        }

    override suspend fun createHabit(title: String, description: String, createdEpochDay: Long) {
        dao.insertHabit(HabitEntity(UUID.randomUUID().toString(), title, description, createdEpochDay))
    }

    override suspend fun setCompletion(habitId: String, epochDay: Long, completed: Boolean) {
        dao.upsertCheckIn(HabitCheckInEntity(habitId, epochDay, completed, System.currentTimeMillis()))
    }

    override suspend fun archiveHabit(habitId: String, inactiveFromEpochDay: Long) {
        dao.archiveHabit(habitId, inactiveFromEpochDay)
    }

    private fun HabitEntity.toDomain() = Habit(id, title, description, createdEpochDay, inactiveFromEpochDay)
}
