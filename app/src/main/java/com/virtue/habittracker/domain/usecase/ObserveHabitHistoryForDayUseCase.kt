package com.virtue.habittracker.domain.usecase

import com.virtue.habittracker.domain.repository.HabitRepository
import javax.inject.Inject

/** Separate use case keeps the History screen independent from today's active-habit list. */
class ObserveHabitHistoryForDayUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(epochDay: Long) = repository.observeHistoryForDay(epochDay)
}
