package com.virtue.habittracker.domain.service

import com.virtue.habittracker.domain.model.program.ProgramActivityStatus
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.ScheduledProgramActivity
import com.virtue.habittracker.domain.model.program.WorkActivityLevel
import java.util.UUID

/** Pure deterministic schedule builder: no Android, Room, Firebase, or network dependency. */
class ProgramPersonalizationEngine {
    private data class DailyWorkoutModule(
        val titleSuffix: String,
        val focusNote: String
    )

    private fun dailyVariation(templateId: String, activeDayOffset: Int): DailyWorkoutModule? {
        val modules: List<DailyWorkoutModule> = when (templateId) {
            "home-strength-60" -> listOf(
                DailyWorkoutModule(
                    "Upper Body Push & Anterior Core",
                    "Primary focus: chest, anterior deltoids, triceps, and anti-extension core control."
                ),
                DailyWorkoutModule(
                    "Lower Body Quad & Glute Drive",
                    "Primary focus: squat mechanics, split-stance stability, and knee-over-toe control."
                ),
                DailyWorkoutModule(
                    "Upper Body Pull & Scapular Density",
                    "Primary focus: lats, upper-back retraction, rear delts, and elbow flexor tension."
                ),
                DailyWorkoutModule(
                    "Posterior Chain Hinge & Hamstrings",
                    "Primary focus: hip-hinge mechanics, glute bridge isometric pauses, and lower-back endurance."
                ),
                DailyWorkoutModule(
                    "Full-Body Hypertrophy & Metabolic Pump",
                    "Primary focus: multi-joint supersets, controlled 2-second peak contractions, and thoracic decompression."
                ),
                DailyWorkoutModule(
                    "Structural Symmetry & Unilateral Strength",
                    "Primary focus: single-arm/single-leg balance, core anti-rotation, and joint integrity."
                )
            )
            "weight-habits-90" -> listOf(
                DailyWorkoutModule(
                    "Full-Body Muscle Retention & Protein Anchor",
                    "Primary focus: compound muscle-preservation sets + hitting your first 35g+ protein meal anchor."
                ),
                DailyWorkoutModule(
                    "NEAT Step Surge & Core Stability",
                    "Primary focus: 20-minute post-meal brisk walk + trunk endurance and hydration check."
                ),
                DailyWorkoutModule(
                    "Lower Body Metabolic Resistance",
                    "Primary focus: high-recruitment leg and glute movements with 60-second active rest intervals."
                ),
                DailyWorkoutModule(
                    "Upper Body Density & Satiety Check-In",
                    "Primary focus: push/pull superset pairing + whole-food fiber and evening meal timing."
                ),
                DailyWorkoutModule(
                    "Low-Impact Conditioning & Mobility Flow",
                    "Primary focus: steady fat-oxidation zone movement + hip/thoracic opening."
                ),
                DailyWorkoutModule(
                    "Metabolic Reset & Sleep Hygiene Audit",
                    "Primary focus: light functional movement + 8-hour recovery window preparation."
                )
            )
            "stamina-60" -> listOf(
                DailyWorkoutModule(
                    "Zone 2 Nasal Aerobic Base",
                    "Primary focus: steady conversational pace keeping heart rate in your aerobic fat-burning zone."
                ),
                DailyWorkoutModule(
                    "Aerobic Threshold Tempo Intervals",
                    "Primary focus: 3–4 sustained moderate-hard surges with controlled 2-minute active recovery."
                ),
                DailyWorkoutModule(
                    "Cardio-Respiratory Endurance & Cadence",
                    "Primary focus: smooth, rhythmic stride/pedal cadence and diaphragmatic breathing efficiency."
                ),
                DailyWorkoutModule(
                    "VO2 Max Conditioning Bouts",
                    "Primary focus: structured higher-output work bouts followed by full heart-rate recovery."
                ),
                DailyWorkoutModule(
                    "Long Steady-State Vitality Session",
                    "Primary focus: extended low-stress aerobic output to build mitochondrial density."
                ),
                DailyWorkoutModule(
                    "Active Flush & Parasympathetic Cooldown",
                    "Primary focus: low-intensity circulation work and box-breathing heart-rate down-regulation."
                )
            )
            "movement-30" -> listOf(
                DailyWorkoutModule(
                    "Cervical, Shoulder & Thoracic CARs",
                    "Primary focus: overhead scapular glide, ribcage rotation, and neck decompression."
                ),
                DailyWorkoutModule(
                    "Hip Capsule, 90/90 & Ankle Dorsiflexion",
                    "Primary focus: internal/external hip rotation, deep squat comfort, and Achilles elasticity."
                ),
                DailyWorkoutModule(
                    "Spinal Segmentation & Posterior Chain",
                    "Primary focus: segmental cat-cow waves, hamstring nerve glides, and lumbar relief."
                ),
                DailyWorkoutModule(
                    "Single-Leg Balance & Proprioception",
                    "Primary focus: foot tripod stability, knee tracking, and controlled weight transfers."
                ),
                DailyWorkoutModule(
                    "Full-Body Fascial Unwinding Flow",
                    "Primary focus: continuous multi-planar transitions synchronized with slow nasal exhalations."
                ),
                DailyWorkoutModule(
                    "Deep Tissue Recovery & Vagal Reset",
                    "Primary focus: passive floor opener holds and extended-exhale parasympathetic breathing."
                )
            )
            else -> emptyList()
        }

        if (modules.isEmpty()) return null
        return modules[activeDayOffset % modules.size]
    }

    fun buildSchedule(
        template: ProgramTemplate,
        enrollmentId: String,
        startEpochDay: Long,
        preferences: ProgramPreferences,
        createdAtMillis: Long = 0L
    ): List<ScheduledProgramActivity> {
        val availableMinutes = preferences.availableMinutesPerDay.coerceIn(5, 90)
        // Physically demanding work already adds load; preserve recovery by capping planned sessions.
        val requestedDays = preferences.availableDaysPerWeek.coerceIn(1, 7)
        val daysPerWeek = if (preferences.workActivityLevel == WorkActivityLevel.PHYSICALLY_ACTIVE) {
            minOf(requestedDays, 4)
        } else {
            requestedDays
        }

        var activeSessionCounter = 0

        return (1..template.durationDays).map { day ->
            val phase = template.phases.lastOrNull { day in it.startDay..it.endDay }
                ?: template.phases.last()
            val dayOfWeek = (day - 1) % 7
            val isRestDay = dayOfWeek >= daysPerWeek
            val baseMinutes = minOf(phase.minutes, availableMinutes)
            val equipmentMismatch = template.equipment == ProgramEquipment.GYM &&
                preferences.equipment != ProgramEquipment.GYM

            val variation = if (!isRestDay && !equipmentMismatch) {
                dailyVariation(template.id, activeSessionCounter++)
            } else {
                null
            }

            val title = when {
                isRestDay -> "Rest and reset"
                equipmentMismatch -> "Choose a no-equipment alternative"
                variation != null -> "${phase.activityTitle} • ${variation.titleSuffix}"
                else -> phase.activityTitle
            }
            val instructions = when {
                isRestDay -> "A planned easier day. Rest, stretch gently if comfortable, or simply resume tomorrow."
                equipmentMismatch -> "This template expects gym equipment. For now, choose comfortable walking or mobility instead; do not attempt unfamiliar weighted movements."
                variation != null -> "${variation.focusNote} ${phase.instructions}"
                else -> phase.instructions
            }
            ScheduledProgramActivity(
                id = UUID.nameUUIDFromBytes("$enrollmentId:$day".toByteArray()).toString(),
                enrollmentId = enrollmentId,
                dayIndex = day,
                epochDay = startEpochDay + day - 1L,
                phaseTitle = phase.title,
                title = title,
                instructions = instructions,
                estimatedMinutes = if (isRestDay) 0 else baseMinutes,
                status = ProgramActivityStatus.PENDING,
                updatedAtMillis = createdAtMillis
            )
        }
    }
}
