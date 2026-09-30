package com.virtue.habittracker.domain.service

import com.virtue.habittracker.domain.model.program.ProgramCatalog
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramExperience
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramPersonalizationEngineTest {
    private val engine = ProgramPersonalizationEngine()

    @Test
    fun createsExactlyOneSnapshotActivityPerProgramDay() {
        ProgramCatalog.all.forEach { template ->
            val activities = engine.buildSchedule(
                template = template,
                enrollmentId = "test-enrollment",
                startEpochDay = 20_000L,
                preferences = ProgramPreferences()
            )
            assertEquals(template.durationDays, activities.size)
            assertEquals((1..template.durationDays).toList(), activities.map { it.dayIndex })
            assertTrue(activities.all { it.epochDay == 19_999L + it.dayIndex })
        }
    }

    @Test
    fun respectsTimeBudgetAndWeeklyAvailability() {
        val template = ProgramCatalog.find("movement-30")!!
        val schedule = engine.buildSchedule(
            template, "time-budget", 20_000L,
            ProgramPreferences(
                availableMinutesPerDay = 10,
                availableDaysPerWeek = 3,
                experience = ProgramExperience.BEGINNER,
                equipment = ProgramEquipment.NONE
            )
        )
        assertTrue(schedule.filter { it.estimatedMinutes > 0 }.all { it.estimatedMinutes <= 10 })
        assertEquals(3, schedule.take(7).count { it.estimatedMinutes > 0 })
        assertEquals(0, schedule[3].estimatedMinutes)
    }

    @Test
    fun scheduleIsStableForSameInputsApartFromCreationTimestamp() {
        val template = ProgramCatalog.find("mindful-30")!!
        val first = engine.buildSchedule(template, "stable-id", 20_000L, ProgramPreferences())
        val second = engine.buildSchedule(template, "stable-id", 20_000L, ProgramPreferences())
        assertEquals(first.map { it.id }, second.map { it.id })
        assertEquals(first.map { it.title }, second.map { it.title })
        assertEquals(first.map { it.epochDay }, second.map { it.epochDay })
    }

    @Test
    fun everyCatalogProgramHasReviewedPhaseMetadata() {
        ProgramCatalog.all.forEach { template ->
            assertTrue(template.phases.isNotEmpty())
            assertEquals(1, template.phases.first().startDay)
            assertEquals(template.durationDays, template.phases.last().endDay)
            assertTrue(template.phases.zipWithNext().all { (a, b) -> a.endDay + 1 == b.startDay })
        }
    }
}
