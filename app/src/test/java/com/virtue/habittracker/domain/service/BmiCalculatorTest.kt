package com.virtue.habittracker.domain.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BmiCalculatorTest {
    @Test
    fun calculatesAdultScreeningValue() {
        assertEquals(22.86, BmiCalculator.calculateAdultBmi(30, 170.0, 66.0)!!, 0.02)
    }

    @Test
    fun doesNotCalculateForMinorsOrInvalidMeasurements() {
        assertNull(BmiCalculator.calculateAdultBmi(17, 170.0, 60.0))
        assertNull(BmiCalculator.calculateAdultBmi(30, 0.0, 60.0))
        assertNull(BmiCalculator.calculateAdultBmi(30, 170.0, Double.NaN))
        assertNull(BmiCalculator.calculateAdultBmi(null, 170.0, 60.0))
    }
}
