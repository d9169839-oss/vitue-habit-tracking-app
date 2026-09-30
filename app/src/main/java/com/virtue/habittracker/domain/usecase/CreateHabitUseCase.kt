package com.virtue.habittracker.domain.usecase
import com.virtue.habittracker.domain.repository.HabitRepository
import javax.inject.Inject
class CreateHabitUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(title: String, description: String, createdEpochDay: Long): Result<Unit> {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return Result.failure(IllegalArgumentException("Give your habit a name."))
        if (cleanTitle.length > 60) return Result.failure(IllegalArgumentException("Habit names must be 60 characters or fewer."))
        if (description.length > 240) return Result.failure(IllegalArgumentException("Descriptions must be 240 characters or fewer."))
        return runCatching { repository.createHabit(cleanTitle, description.trim(), createdEpochDay) }
    }
}
