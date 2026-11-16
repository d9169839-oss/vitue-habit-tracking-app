package com.virtue.habittracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.usecase.ObserveHabitHistoryForDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeHistoryForDay: ObserveHabitHistoryForDayUseCase
) : ViewModel() {
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    val selectedDate = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val entries = selectedDate
        .flatMapLatest { observeHistoryForDay(it.toEpochDay()) }
        .onEach { _isLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * A real seven-day series derived from the same persisted history as the daily History screen.
     * Each point excludes unrecorded entries from its completion-rate denominator.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val weeklyProgress = selectedDate
        .flatMapLatest { selected ->
            val dates = (6 downTo 0).map { selected.minusDays(it.toLong()) }
            combine(dates.map { date -> observeHistoryForDay(date.toEpochDay()) }) { dailyEntries ->
                dates.mapIndexed { index, day ->
                    val dayEntries = dailyEntries[index]
                    val active = dayEntries.filter { it.isActiveOnDate }
                    val completed = active.count { it.status == com.virtue.habittracker.domain.model.HabitDayStatus.COMPLETED }
                    val notCompleted = active.count { it.status == com.virtue.habittracker.domain.model.HabitDayStatus.NOT_COMPLETED }
                    val recorded = completed + notCompleted
                    DailyProgressPoint(
                        date = day,
                        completedCount = completed,
                        notCompletedCount = notCompleted,
                        unrecordedCount = active.count { it.status == com.virtue.habittracker.domain.model.HabitDayStatus.UNRECORDED },
                        completionRatePercent = if (recorded == 0) 0 else completed * 100 / recorded
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDate(date: LocalDate) {
        // History is a snapshot of the past or today; future dates have no habit history yet.
        val safeDate = if (date.isAfter(LocalDate.now())) LocalDate.now() else date
        if (safeDate != _selectedDate.value) {
            _isLoading.value = true
            _selectedDate.value = safeDate
        }
    }

    fun previousDay() {
        selectDate(_selectedDate.value.minusDays(1))
    }

    fun nextDay() {
        val current = _selectedDate.value
        if (current.isBefore(LocalDate.now())) selectDate(current.plusDays(1))
    }

    fun goToToday() {
        selectDate(LocalDate.now())
    }
}


/** One truthful, date-specific point for the recent consistency chart. */
data class DailyProgressPoint(
    val date: LocalDate,
    val completedCount: Int,
    val notCompletedCount: Int,
    val unrecordedCount: Int,
    val completionRatePercent: Int
)
