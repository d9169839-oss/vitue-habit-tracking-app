package com.virtue.habittracker.presentation.history

import com.virtue.habittracker.domain.model.Habit
import com.virtue.habittracker.domain.model.HabitDayEntry
import com.virtue.habittracker.domain.model.HabitDayStatus
import com.virtue.habittracker.domain.repository.HabitRepository
import com.virtue.habittracker.domain.usecase.ObserveHabitHistoryForDayUseCase
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun rangeSummaryIncludesArchivedHabits() = runTest {
        val todayEpochDay = LocalDate.now().toEpochDay()

        val activeHabit = Habit(
            id = "habit-1",
            title = "Morning Run",
            description = "Run 5k",
            createdEpochDay = todayEpochDay - 10,
            inactiveFromEpochDay = null
        )

        val archivedHabit = Habit(
            id = "habit-2",
            title = "Old Workout",
            description = "Archived routine",
            createdEpochDay = todayEpochDay - 20,
            inactiveFromEpochDay = todayEpochDay - 2
        )

        val fakeRepository = object : HabitRepository {
            override fun observeHabitsForDay(epochDay: Long): Flow<List<HabitDayEntry>> = flowOf(emptyList())

            override fun observeHistoryForDay(epochDay: Long): Flow<List<HabitDayEntry>> {
                val inactive1 = activeHabit.inactiveFromEpochDay
                val isActiveOnDay1 = inactive1 == null || epochDay < inactive1

                val inactive2 = archivedHabit.inactiveFromEpochDay
                val isActiveOnDay2 = inactive2 == null || epochDay < inactive2

                return flowOf(
                    listOf(
                        HabitDayEntry(activeHabit, HabitDayStatus.COMPLETED, currentStreak = 1, isActiveOnDate = isActiveOnDay1),
                        HabitDayEntry(archivedHabit, HabitDayStatus.COMPLETED, currentStreak = 0, isActiveOnDate = isActiveOnDay2)
                    )
                )
            }

            override suspend fun createHabit(title: String, description: String, createdEpochDay: Long) {}
            override suspend fun setCompletion(habitId: String, epochDay: Long, completed: Boolean) {}
            override suspend fun clearCompletion(habitId: String, epochDay: Long) {}
            override suspend fun archiveHabit(habitId: String, inactiveFromEpochDay: Long) {}
            override suspend fun unarchiveHabit(habitId: String) {}
            override suspend fun reactivateHabitFreshStart(habitId: String, newCreatedEpochDay: Long) {}
        }

        val useCase = ObserveHabitHistoryForDayUseCase(fakeRepository)
        val viewModel = HistoryViewModel(useCase, fakeRepository)

        val job = backgroundScope.launch {
            viewModel.rangeSummary.collect {}
        }
        testScheduler.advanceUntilIdle()

        val performanceList = viewModel.rangeSummary.value.habitPerformanceList

        assertEquals(2, performanceList.size)
        val activeItem = performanceList.find { it.entry.habit.id == "habit-1" }
        val archivedItem = performanceList.find { it.entry.habit.id == "habit-2" }

        assertNotNull(activeItem)
        assertNotNull(archivedItem)
        assertEquals(null, activeItem?.entry?.habit?.inactiveFromEpochDay)
        assertTrue(archivedItem?.entry?.habit?.inactiveFromEpochDay != null)

        job.cancel()
    }

    @Test
    fun reactivateHabitCallsRepositoryMethods() = runTest {
        var unarchivedHabitId: String? = null
        var freshStartHabitId: String? = null
        var freshStartEpochDay: Long? = null

        val fakeRepository = object : HabitRepository {
            override fun observeHabitsForDay(epochDay: Long): Flow<List<HabitDayEntry>> = flowOf(emptyList())
            override fun observeHistoryForDay(epochDay: Long): Flow<List<HabitDayEntry>> = flowOf(emptyList())
            override suspend fun createHabit(title: String, description: String, createdEpochDay: Long) {}
            override suspend fun setCompletion(habitId: String, epochDay: Long, completed: Boolean) {}
            override suspend fun clearCompletion(habitId: String, epochDay: Long) {}
            override suspend fun archiveHabit(habitId: String, inactiveFromEpochDay: Long) {}

            override suspend fun unarchiveHabit(habitId: String) {
                unarchivedHabitId = habitId
            }

            override suspend fun reactivateHabitFreshStart(habitId: String, newCreatedEpochDay: Long) {
                freshStartHabitId = habitId
                freshStartEpochDay = newCreatedEpochDay
            }
        }

        val useCase = ObserveHabitHistoryForDayUseCase(fakeRepository)
        val viewModel = HistoryViewModel(useCase, fakeRepository)

        viewModel.reactivateHabitResume("habit-resume-id")
        testScheduler.advanceUntilIdle()
        assertEquals("habit-resume-id", unarchivedHabitId)

        viewModel.reactivateHabitFreshStart("habit-fresh-id")
        testScheduler.advanceUntilIdle()
        assertEquals("habit-fresh-id", freshStartHabitId)
        assertEquals(LocalDate.now().toEpochDay(), freshStartEpochDay)
    }
}
