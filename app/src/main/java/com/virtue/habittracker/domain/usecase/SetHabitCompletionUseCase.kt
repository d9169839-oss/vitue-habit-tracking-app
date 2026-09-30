package com.virtue.habittracker.domain.usecase
import com.virtue.habittracker.domain.repository.HabitRepository
import javax.inject.Inject
class SetHabitCompletionUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: String, epochDay: Long, completed: Boolean) =
        repository.setCompletion(habitId, epochDay, completed)
}
