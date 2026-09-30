package com.virtue.habittracker.domain.usecase
import com.virtue.habittracker.domain.repository.HabitRepository
import javax.inject.Inject
class ObserveHabitsForDayUseCase @Inject constructor(private val repository: HabitRepository) {
    operator fun invoke(epochDay: Long) = repository.observeHabitsForDay(epochDay)
}
