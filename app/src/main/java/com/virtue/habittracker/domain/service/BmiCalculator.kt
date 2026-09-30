package com.virtue.habittracker.domain.service

/**
 * Optional adult BMI screening calculation. It deliberately does not assign a diagnosis,
 * category, calorie target, or exercise intensity. Children and teens require different methods.
 */
object BmiCalculator {
    fun calculateAdultBmi(ageYears: Int?, heightCm: Double?, weightKg: Double?): Double? {
        if (ageYears == null || ageYears < 18) return null
        if (heightCm == null || weightKg == null) return null
        if (!heightCm.isFinite() || !weightKg.isFinite() || heightCm <= 0.0 || weightKg <= 0.0) return null
        val heightMetres = heightCm / 100.0
        val bmi = weightKg / (heightMetres * heightMetres)
        return bmi.takeIf { it.isFinite() && it > 0.0 }
    }
}
