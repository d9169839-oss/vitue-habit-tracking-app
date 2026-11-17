package com.virtue.habittracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.usecase.ObserveHabitHistoryForDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeHistoryForDay: ObserveHabitHistoryForDayUseCase
) : ViewModel() {
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    private val _isWeeklyProgressLoading = MutableStateFlow(true)
    val isWeeklyProgressLoading = _isWeeklyProgressLoading.asStateFlow()
    val selectedDate = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val entries = selectedDate
        .flatMapLatest { observeHistoryForDay(it.toEpochDay()) }
        .onEach { _isLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * A Monday-to-Sunday series derived from persisted habit history. Every calendar day is
     * represented, and the percentage uses all habits active that day as its denominator.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val weeklyProgress = selectedDate
        .flatMapLatest { selected ->
            val weekStart = selected.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val dates = (0..6).map { weekStart.plusDays(it.toLong()) }
            combine(dates.map { date -> observeHistoryForDay(date.toEpochDay()) }) { dailyEntries ->
                dates.mapIndexed { index, day ->
                    val dayEntries = dailyEntries[index]
                    val active = dayEntries.filter { it.isActiveOnDate }
                    val completed = active.count { it.status == com.virtue.habittracker.domain.model.HabitDayStatus.COMPLETED }
                    val notCompleted = active.count { it.status == com.virtue.habittracker.domain.model.HabitDayStatus.NOT_COMPLETED }
                    // Use all active habits as the denominator. A day with only one or two
                    // recorded habits must not appear as 100% merely because few were recorded.
                    DailyProgressPoint(
                        date = day,
                        completedCount = completed,
                        notCompletedCount = notCompleted,
                        unrecordedCount = active.count { it.status == com.virtue.habittracker.domain.model.HabitDayStatus.UNRECORDED },
                        completionRatePercent = if (active.isEmpty()) 0 else completed * 100 / active.size
                    )
                }
            }
        }
        .onEach { _isWeeklyProgressLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDate(date: LocalDate) {
        // History is a snapshot of the past or today; future dates have no habit history yet.
        val safeDate = if (date.isAfter(LocalDate.now())) LocalDate.now() else date
        if (safeDate != _selectedDate.value) {
            _isLoading.value = true
            _isWeeklyProgressLoading.value = true
            _selectedDate.value = safeDate
        }
    }

    /** Move through complete Monday-to-Sunday weeks; users cannot land on arbitrary dates. */
    fun previousWeek() {
        // Keep the daily snapshot on the same weekday while the chart moves by a full week.
        selectDate(_selectedDate.value.minusWeeks(1))
    }

    fun nextWeek() {
        val current = _selectedDate.value
        val currentWeekStart = current.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val thisWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        if (currentWeekStart.isBefore(thisWeekStart)) {
            selectDate(minOf(LocalDate.now(), current.plusWeeks(1)))
        }
    }

    fun goToCurrentWeek() {
        selectDate(LocalDate.now())
    }

    fun canMoveToNextWeek(): Boolean {
        val selectedWeekStart = _selectedDate.value.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val thisWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return selectedWeekStart.isBefore(thisWeekStart)
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
