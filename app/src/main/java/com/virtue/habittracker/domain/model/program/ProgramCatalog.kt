package com.virtue.habittracker.domain.model.program

import com.virtue.habittracker.domain.model.program.catalog.AerobicEngineProgram
import com.virtue.habittracker.domain.model.program.catalog.BuildLeanMuscleProgram
import com.virtue.habittracker.domain.model.program.catalog.CalmRecompositionProgram
import com.virtue.habittracker.domain.model.program.catalog.CircadianSleepRestorationProgram
import com.virtue.habittracker.domain.model.program.catalog.DailyProgramModule
import com.virtue.habittracker.domain.model.program.catalog.DopamineDetoxImpulseProgram
import com.virtue.habittracker.domain.model.program.catalog.EverydayGroomingProgram
import com.virtue.habittracker.domain.model.program.catalog.FitnessGeneralWellnessProgram
import com.virtue.habittracker.domain.model.program.catalog.GratitudeEmotionalResilienceProgram
import com.virtue.habittracker.domain.model.program.catalog.LongevityMovementProgram
import com.virtue.habittracker.domain.model.program.catalog.MindfulnessAnxietyReliefProgram
import com.virtue.habittracker.domain.model.program.catalog.PersonalDevelopmentProgram
import com.virtue.habittracker.domain.model.program.catalog.ProgramDefinition
import com.virtue.habittracker.domain.model.program.catalog.SimpleSkincareProgram

/**
 * Central registry aggregating all modular [`ProgramDefinition`] implementations.
 * Each program lives in its own dedicated source file under `domain/model/program/catalog/`
 * with its own phases, metadata, and rotating daily curriculum modules.
 */
object ProgramCatalog {
    val definitions: List<ProgramDefinition> = listOf(
        // Physical & Metabolic Programs
        BuildLeanMuscleProgram,
        CalmRecompositionProgram,
        AerobicEngineProgram,
        LongevityMovementProgram,
        FitnessGeneralWellnessProgram,
        // Psychiatry, CBT, Sleep & Mindfulness Programs
        MindfulnessAnxietyReliefProgram,
        CircadianSleepRestorationProgram,
        GratitudeEmotionalResilienceProgram,
        // Executive Function, Dopamine Detox & Self-Mastery Programs
        DopamineDetoxImpulseProgram,
        PersonalDevelopmentProgram,
        // Self-Grooming & Dermatological Consistency Programs
        EverydayGroomingProgram,
        SimpleSkincareProgram
    )

    val all: List<ProgramTemplate> = definitions.map { it.template }

    fun find(id: String): ProgramTemplate? = all.firstOrNull { it.id == id }

    fun findDefinition(id: String): ProgramDefinition? =
        definitions.firstOrNull { it.template.id == id }

    fun dailyModuleFor(templateId: String, activeDayOffset: Int): DailyProgramModule? {
        val modules = findDefinition(templateId)?.dailyModules.orEmpty()
        if (modules.isEmpty()) return null
        return modules[activeDayOffset % modules.size]
    }
}
