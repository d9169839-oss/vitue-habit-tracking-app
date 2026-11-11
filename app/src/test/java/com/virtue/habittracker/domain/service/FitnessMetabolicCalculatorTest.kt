package com.virtue.habittracker.domain.service

import com.virtue.habittracker.domain.model.program.BiologicalSex
import com.virtue.habittracker.domain.model.program.Biometrics
import com.virtue.habittracker.domain.model.program.DailyOccupation
import com.virtue.habittracker.domain.model.program.ProgramExperience
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FitnessMetabolicCalculatorTest {

    @Test
    fun `calculates male BMR and TDEE correctly using Mifflin-St Jeor equation`() {
        val biometrics = Biometrics(
            sex = BiologicalSex.MALE,
            age = 25,
            heightCm = 178.0,
            weightKg = 72.0,
            occupation = DailyOccupation.DESK
        )

        val profile = FitnessMetabolicCalculator.calculate(
            biometrics = biometrics,
            experience = ProgramExperience.BEGINNER,
            focusProgramId = "build-lean-muscle"
        )

        // BMR = 10(72) + 6.25(178) - 5(25) + 5 = 720 + 1112.5 - 125 + 5 = 1712.5 -> 1712
        assertEquals(1712, profile.bmr)
        // TDEE = 1712 * 1.2 = 2054
        assertEquals(2054, profile.tdee)
        // Target (Hypertrophy +10%) = 2054 * 1.10 = 2259
        assertEquals(2259, profile.targetCalories)
        // Protein = 72 * 2.0 = 144g
        assertEquals(144, profile.proteinGrams)
        // Weekly sets = 12
        assertEquals(12, profile.weeklySetsPerMuscleGroup)
    }

    @Test
    fun `calculates female BMR and TDEE correctly using Mifflin-St Jeor equation`() {
        val biometrics = Biometrics(
            sex = BiologicalSex.FEMALE,
            age = 28,
            heightCm = 165.0,
            weightKg = 60.0,
            occupation = DailyOccupation.ON_FEET
        )

        val profile = FitnessMetabolicCalculator.calculate(
            biometrics = biometrics,
            experience = ProgramExperience.EXPERIENCED,
            focusProgramId = "calm-recomposition"
        )

        // BMR = 10(60) + 6.25(165) - 5(28) - 161 = 600 + 1031.25 - 140 - 161 = 1330.25 -> 1330
        assertEquals(1330, profile.bmr)
        // TDEE = 1330 * 1.55 = 2061
        assertEquals(2061, profile.tdee)
        // Target (Recomp -15%) = 2061 * 0.85 = 1751
        assertEquals(1751, profile.targetCalories)
        // Protein = 60 * 2.2 = 132g
        assertEquals(132, profile.proteinGrams)
        // Experienced Weekly sets = 20
        assertEquals(20, profile.weeklySetsPerMuscleGroup)
    }
}
