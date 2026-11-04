package com.virtue.habittracker.domain.model.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Protects the two bundled starter templates from accidental catalog regressions. */
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
        assertEquals(false, template.isPremium)
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
    fun `all template phases cover their duration without gaps or overlaps`() {
        listOf("fitness-wellness-60", "personal-development-30").forEach { id ->
            val template = requireNotNull(ProgramCatalog.find(id))
            var expectedStartDay = 1

            template.phases.forEach { phase ->
                assertEquals(expectedStartDay, phase.startDay)
                assertTrue(phase.endDay >= phase.startDay)
                expectedStartDay = phase.endDay + 1
            }

            assertEquals(template.durationDays + 1, expectedStartDay)
        }
    }
}
