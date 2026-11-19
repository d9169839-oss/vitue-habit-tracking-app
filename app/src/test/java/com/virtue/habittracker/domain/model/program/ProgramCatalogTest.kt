package com.virtue.habittracker.domain.model.program

import com.virtue.habittracker.domain.service.ProgramPersonalizationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Protects the bundled program templates and ensures every program has distinct content. */
class ProgramCatalogTest {

    @Test
    fun `fitness and wellness template has reviewed starter metadata`() {
        val template = ProgramCatalog.find("fitness-wellness-60")

        assertNotNull(template)
        requireNotNull(template)
        assertEquals("Fitness & General Wellness", template.title)
        assertEquals(ProgramCategory.FITNESS, template.category)
        assertEquals(60, template.durationDays)
        assertEquals(15, template.defaultMinutesPerDay)
        assertEquals(ProgramDifficulty.BEGINNER, template.difficulty)
        assertEquals(ProgramEquipment.NONE, template.equipment)
        assertEquals(ProgramArtwork.FITNESS_WELLNESS, template.artwork)
        assertEquals(3, template.phases.size)
        assertEquals(1, template.phases.first().startDay)
        assertEquals(60, template.phases.last().endDay)
        assertTrue(template.safetyNote.contains("not medical advice"))
    }

    @Test
    fun `personal development template includes three structured phases`() {
        val template = ProgramCatalog.find("personal-development-30")

        assertNotNull(template)
        requireNotNull(template)
        assertEquals("Personal Development", template.title)
        assertEquals(ProgramCategory.PERSONAL_DEVELOPMENT, template.category)
        assertEquals(30, template.durationDays)
        assertEquals(10, template.defaultMinutesPerDay)
        assertEquals(ProgramDifficulty.BEGINNER, template.difficulty)
        assertEquals(ProgramEquipment.NONE, template.equipment)
        assertEquals(false, template.isPremium)
        assertEquals(ProgramArtwork.PERSONAL_DEVELOPMENT, template.artwork)
        assertEquals(3, template.phases.size)
        assertEquals(1, template.phases.first().startDay)
        assertEquals(30, template.phases.last().endDay)
    }

    @Test
    fun `every built-in program is free for every user`() {
        assertTrue(ProgramCatalog.all.isNotEmpty())
        assertTrue(ProgramCatalog.all.none { it.isPremium })
    }

    @Test
    fun `all template phases cover their duration without gaps or overlaps`() {
        ProgramCatalog.all.forEach { template ->
            var expectedStartDay = 1

            template.phases.forEach { phase ->
                assertEquals(expectedStartDay, phase.startDay)
                assertTrue(phase.endDay >= phase.startDay)
                expectedStartDay = phase.endDay + 1
            }

            assertEquals(template.durationDays + 1, expectedStartDay)
        }
    }

    @Test
    fun `every catalog program has distinct phase titles and activity content`() {
        val phaseSignatures = ProgramCatalog.all.map { template ->
            template.phases.joinToString("|") { "${it.title}:${it.activityTitle}:${it.instructions}" }
        }
        assertEquals(
            "Every program in ProgramCatalog must have its own unique phase and activity content",
            ProgramCatalog.all.size,
            phaseSignatures.distinct().size
        )
    }

    @Test
    fun `each primary focus program generates varied daily sessions across the week`() {
        val engine = ProgramPersonalizationEngine()
        val focusTemplateIds = listOf("home-strength-60", "weight-habits-90", "stamina-60", "movement-30")

        focusTemplateIds.forEach { templateId ->
            val template = requireNotNull(ProgramCatalog.find(templateId))
            val schedule = engine.buildSchedule(
                template = template,
                enrollmentId = "enrollment-$templateId",
                startEpochDay = 20_000L,
                preferences = ProgramPreferences(
                    availableMinutesPerDay = 30,
                    availableDaysPerWeek = 5,
                    equipment = template.equipment
                )
            )
            val firstWeekActiveTitles = schedule.take(7)
                .filter { it.estimatedMinutes > 0 }
                .map { it.title }

            assertEquals(5, firstWeekActiveTitles.size)
            assertEquals(
                "Active workouts in week 1 of $templateId should have distinct daily focus modules",
                5,
                firstWeekActiveTitles.distinct().size
            )
        }
    }
}
