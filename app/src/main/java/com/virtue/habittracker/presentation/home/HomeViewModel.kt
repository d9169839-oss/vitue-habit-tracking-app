package com.virtue.habittracker.presentation.home
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.usecase.ArchiveHabitUseCase
import com.virtue.habittracker.domain.usecase.CreateHabitUseCase
import com.virtue.habittracker.domain.usecase.ObserveHabitsForDayUseCase
import com.virtue.habittracker.domain.usecase.SetHabitCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeHabitsForDay: ObserveHabitsForDayUseCase,
    private val createHabit: CreateHabitUseCase,
    private val setHabitCompletion: SetHabitCompletionUseCase,
    private val archiveHabitUseCase: ArchiveHabitUseCase,
    private val clearHabitCheckInUseCase: com.virtue.habittracker.domain.usecase.ClearHabitCheckInUseCase
) : ViewModel() {
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()
    @OptIn(ExperimentalCoroutinesApi::class)
    val habits = selectedDate.flatMapLatest { observeHabitsForDay(it.toEpochDay()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDate(date: LocalDate) { _selectedDate.value = if (date.isAfter(LocalDate.now())) LocalDate.now() else date }
    fun previousDay() { _selectedDate.update { it.minusDays(1) } }
    fun nextDay() { _selectedDate.update { current -> if (current.isBefore(LocalDate.now())) current.plusDays(1) else current } }
    fun addHabit(title: String, description: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            createHabit(title, description, LocalDate.now().toEpochDay())
                .onFailure { onError(it.message ?: "Could not create habit.") }
        }
    }
    fun clearCompletion(entry: HabitDayEntry) {
        viewModelScope.launch { clearHabitCheckInUseCase(entry.habit.id, selectedDate.value.toEpochDay()) }
    }

    fun archiveHabit(entry: HabitDayEntry) {
        viewModelScope.launch { archiveHabitUseCase(entry.habit.id, LocalDate.now().toEpochDay()) }
    }

    fun toggleCompletion(entry: HabitDayEntry) {
        viewModelScope.launch {
            val completed = entry.status != com.virtue.habittracker.domain.model.HabitDayStatus.COMPLETED
            setHabitCompletion(entry.habit.id, selectedDate.value.toEpochDay(), completed)
        }
    }

    /** Explicitly record a missed day; this differs from leaving the habit unrecorded. */
    fun markNotCompleted(entry: HabitDayEntry) {
        viewModelScope.launch {
            setHabitCompletion(entry.habit.id, selectedDate.value.toEpochDay(), false)
        }
    }
}
