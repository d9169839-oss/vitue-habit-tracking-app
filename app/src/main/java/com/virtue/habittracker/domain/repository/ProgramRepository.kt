package com.virtue.habittracker.domain.repository

import com.virtue.habittracker.domain.model.program.ProgramEnrollment
import com.virtue.habittracker.domain.model.program.ProgramProgress
import com.virtue.habittracker.domain.model.program.ProgramStatus
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import kotlinx.coroutines.flow.Flow

interface ProgramRepository {
    fun observePrograms(): Flow<List<ProgramProgress>>
    suspend fun startProgram(templateId: String, startEpochDay: Long, preferences: ProgramPreferences): String
    suspend fun setActivityStatus(activityId: String, status: com.virtue.habittracker.domain.model.program.ProgramActivityStatus)
    suspend fun setEnrollmentStatus(enrollmentId: String, status: ProgramStatus)
    suspend fun deleteEnrollment(enrollmentId: String)
}
