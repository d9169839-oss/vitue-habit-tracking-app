package com.virtue.habittracker.domain.service

import com.virtue.habittracker.domain.model.program.BiologicalSex
import com.virtue.habittracker.domain.model.program.Biometrics
import com.virtue.habittracker.domain.model.program.DailyOccupation
import com.virtue.habittracker.domain.model.program.ProgramExperience

data class MetabolicProfile(
    val bmr: Int,
    val tdee: Int,
    val targetCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val weeklySetsPerMuscleGroup: Int,
    val neatMultiplier: Double
)

/**
 * Deterministic clinical fitness & metabolic equations (Mifflin-St Jeor BMR, NEAT multipliers,
 * TDEE, macronutrient partitioning, and hypertrophy set-volume scaling).
 */
object FitnessMetabolicCalculator {
    fun calculate(
        biometrics: Biometrics,
        experience: ProgramExperience,
        focusProgramId: String = "build-lean-muscle"
    ): MetabolicProfile {
        val (sex, age, heightCm, weightKg, occupation) = biometrics

        // Mifflin-St Jeor BMR Formula
        val bmrRaw = if (sex == BiologicalSex.MALE) {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) + 5.0
        } else {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) - 161.0
        }
        val bmr = bmrRaw.toInt().coerceAtLeast(800)

        // NEAT Activity Multiplier
        val neatMultiplier = when (occupation) {
            DailyOccupation.DESK -> 1.20
            DailyOccupation.LIGHT -> 1.375
            DailyOccupation.ON_FEET -> 1.55
            DailyOccupation.PHYSICAL -> 1.725
        }

        val tdee = (bmr * neatMultiplier).toInt()

        // Caloric Target Surplus/Deficit Multiplier
        val surplusDeficitMultiplier = when (focusProgramId) {
            "build-lean-muscle" -> 1.10 // 10% Surplus
            "calm-recomposition" -> 0.85 // 15% Deficit
            "aerobic-engine" -> 1.00 // Maintenance
            "longevity-movement" -> 1.00 // Maintenance
            else -> 1.05
        }

        val targetCalories = (tdee * surplusDeficitMultiplier).toInt()

        // Protein (g / kg body weight)
        val proteinPerKg = when (focusProgramId) {
            "build-lean-muscle" -> 2.0
            "calm-recomposition" -> 2.2
            "aerobic-engine" -> 1.8
            "longevity-movement" -> 1.6
            else -> 1.8
        }
        val proteinGrams = (weightKg * proteinPerKg).toInt()
        val proteinCalories = proteinGrams * 4

        // Fat (25% of target calories)
        val fatCalories = (targetCalories * 0.25).toInt()
        val fatGrams = (fatCalories / 9).coerceAtLeast(20)

        // Carbs (Remaining calories)
        val carbCalories = (targetCalories - proteinCalories - fatCalories).coerceAtLeast(0)
        val carbsGrams = carbCalories / 4

        // Weekly Sets per Muscle Group based on Gym Experience
        val weeklySets = when (experience) {
            ProgramExperience.BEGINNER -> 12
            ProgramExperience.SOME_EXPERIENCE -> 16
            ProgramExperience.EXPERIENCED -> 20
        }

        return MetabolicProfile(
            bmr = bmr,
            tdee = tdee,
            targetCalories = targetCalories,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            weeklySetsPerMuscleGroup = weeklySets,
            neatMultiplier = neatMultiplier
        )
    }
}
