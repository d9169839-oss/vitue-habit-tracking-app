package com.virtue.habittracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.model.HabitDayStatus
import com.virtue.habittracker.domain.usecase.ObserveHabitHistoryForDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HistoryRange {
    SEVEN_DAYS,
    THIRTY_DAYS,
    NINETY_DAYS,
    ALL_TIME,
    SPECIFIC_DATE
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val observeHistoryForDay: ObserveHabitHistoryForDayUseCase,
    private val habitRepository: com.virtue.habittracker.domain.repository.HabitRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(HistoryRange.SEVEN_DAYS)
    val selectedRange = _selectedRange.asStateFlow()

    private val _customDate = MutableStateFlow<LocalDate?>(null)
    val customDate = _customDate.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _selectedChartDay = MutableStateFlow(LocalDate.now())
    val selectedChartDay = _selectedChartDay.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val entries = selectedDate
        .flatMapLatest { observeHistoryForDay(it.toEpochDay()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * 7-day progress points for the consistency bar chart.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val weeklyProgress = selectedDate
        .flatMapLatest { selected ->
            val dates = (6 downTo 0).map { selected.minusDays(it.toLong()) }
            combine(dates.map { date -> observeHistoryForDay(date.toEpochDay()) }) { dailyEntries ->
                dates.mapIndexed { index, day ->
                    val dayEntries = dailyEntries[index]
                    val active = dayEntries.filter { it.isActiveOnDate }
                    val completed = active.count { it.status == HabitDayStatus.COMPLETED }
                    val notCompleted = active.count { it.status == HabitDayStatus.NOT_COMPLETED }
                    val unrecorded = active.count { it.status == HabitDayStatus.UNRECORDED }
                    val recorded = completed + notCompleted
                    DailyProgressPoint(
                        date = day,
                        completedCount = completed,
                        notCompletedCount = notCompleted,
                        unrecordedCount = unrecorded,
                        completionRatePercent = if (recorded == 0) 0 else completed * 100 / recorded,
                        totalActiveHabits = active.size
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Multi-day range summary statistics calculated dynamically.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val rangeSummary = combine(selectedRange, customDate, selectedDate) { range, custom, currentSelected ->
        val targetDates: List<LocalDate> = when (range) {
            HistoryRange.SEVEN_DAYS -> (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
            HistoryRange.THIRTY_DAYS -> (29 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
            HistoryRange.NINETY_DAYS -> (89 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
            HistoryRange.ALL_TIME -> (179 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
            HistoryRange.SPECIFIC_DATE -> listOf(custom ?: currentSelected)
        }
        targetDates
    }.flatMapLatest { dates ->
        combine(dates.map { date -> observeHistoryForDay(date.toEpochDay()) }) { dailyLists ->
            var totalCompleted = 0
            var totalMissed = 0
            var totalUnrecorded = 0

            val habitStats = mutableMapOf<String, Pair<Int, Int>>()
            val habitDetails = mutableMapOf<String, HabitDayEntry>()

            dailyLists.forEach { dayEntries ->
                dayEntries.forEach { entry ->
                    val habitId = entry.habit.id
                    habitDetails[habitId] = entry

                    if (entry.isActiveOnDate) {
                        val current = habitStats.getOrDefault(habitId, Pair(0, 0))
                        val isCompleted = entry.status == HabitDayStatus.COMPLETED
                        habitStats[habitId] = Pair(
                            current.first + (if (isCompleted) 1 else 0),
                            current.second + 1
                        )
                        when (entry.status) {
                            HabitDayStatus.COMPLETED -> totalCompleted++
                            HabitDayStatus.NOT_COMPLETED -> totalMissed++
                            HabitDayStatus.UNRECORDED -> totalUnrecorded++
                        }
                    } else if (entry.status == HabitDayStatus.COMPLETED || entry.status == HabitDayStatus.NOT_COMPLETED) {
                        val current = habitStats.getOrDefault(habitId, Pair(0, 0))
                        val isCompleted = entry.status == HabitDayStatus.COMPLETED
                        habitStats[habitId] = Pair(
                            current.first + (if (isCompleted) 1 else 0),
                            current.second + 1
                        )
                        if (isCompleted) totalCompleted++ else totalMissed++
                    }
                }
            }

            val recordedTotal = totalCompleted + totalMissed
            val overallPercentage = if (recordedTotal == 0) 0 else (totalCompleted * 100) / recordedTotal

            RangeSummaryData(
                completedCount = totalCompleted,
                missedCount = totalMissed,
                unrecordedCount = totalUnrecorded,
                overallPercentage = overallPercentage,
                totalRecordedDays = recordedTotal,
                habitPerformanceList = habitDetails.values.map { entry ->
                    val (completed, total) = habitStats[entry.habit.id] ?: Pair(0, 0)
                    val percent = if (total == 0) 0 else (completed * 100) / total
                    HabitPerformanceItem(
                        entry = entry,
                        completedDays = completed,
                        totalDays = total,
                        completionPercentage = percent
                    )
                }.sortedByDescending { it.completionPercentage }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RangeSummaryData())

    fun selectRange(range: HistoryRange) {
        _selectedRange.value = range
        if (range != HistoryRange.SPECIFIC_DATE) {
            _customDate.value = null
        }
    }

    fun selectCustomDate(date: LocalDate) {
        val validDate = if (date.isAfter(LocalDate.now())) LocalDate.now() else date
        _customDate.value = validDate
        _selectedDate.value = validDate
        _selectedChartDay.value = validDate
        _selectedRange.value = HistoryRange.SPECIFIC_DATE
    }

    fun selectChartDay(date: LocalDate) {
        _selectedChartDay.value = date
        _selectedDate.value = date
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = if (date.isAfter(LocalDate.now())) LocalDate.now() else date
    }

    fun previousDay() {
        _selectedDate.update { it.minusDays(1) }
    }

    fun nextDay() {
        _selectedDate.update { current ->
            if (current.isBefore(LocalDate.now())) current.plusDays(1) else current
        }
    }

    fun goToToday() {
        _selectedDate.value = LocalDate.now()
        _selectedChartDay.value = LocalDate.now()
    }

    fun reactivateHabitResume(habitId: String) {
        viewModelScope.launch {
            habitRepository.unarchiveHabit(habitId)
        }
    }

    fun reactivateHabitFreshStart(habitId: String) {
        viewModelScope.launch {
            habitRepository.reactivateHabitFreshStart(habitId, LocalDate.now().toEpochDay())
        }
    }
}

data class DailyProgressPoint(
    val date: LocalDate,
    val completedCount: Int,
    val notCompletedCount: Int,
    val unrecordedCount: Int,
    val completionRatePercent: Int,
    val totalActiveHabits: Int = 0
)

data class RangeSummaryData(
    val completedCount: Int = 0,
    val missedCount: Int = 0,
    val unrecordedCount: Int = 0,
    val overallPercentage: Int = 0,
    val totalRecordedDays: Int = 0,
    val habitPerformanceList: List<HabitPerformanceItem> = emptyList()
)

data class HabitPerformanceItem(
    val entry: HabitDayEntry,
    val completedDays: Int,
    val totalDays: Int,
    val completionPercentage: Int
)
