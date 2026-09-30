package com.virtue.habittracker.domain.usecase
import com.virtue.habittracker.domain.repository.HabitRepository
import javax.inject.Inject
class ArchiveHabitUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: String, inactiveFromEpochDay: Long) =
        repository.archiveHabit(habitId, inactiveFromEpochDay)
}
