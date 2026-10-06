package com.virtue.habittracker.domain.usecase.program

import com.virtue.habittracker.domain.model.program.ProgramActivityStatus
import com.virtue.habittracker.domain.model.program.ProgramStatus
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.repository.ProgramRepository
import javax.inject.Inject

class SetProgramActivityStatusUseCase @Inject constructor(private val repository: ProgramRepository) {
    suspend operator fun invoke(activityId: String, status: ProgramActivityStatus) =
        repository.setActivityStatus(activityId, status)
}

class SetProgramStatusUseCase @Inject constructor(private val repository: ProgramRepository) {
    suspend operator fun invoke(enrollmentId: String, status: ProgramStatus) =
        repository.setEnrollmentStatus(enrollmentId, status)
}

class DeleteProgramUseCase @Inject constructor(private val repository: ProgramRepository) {
    suspend operator fun invoke(enrollmentId: String) = repository.deleteEnrollment(enrollmentId)
}

class ReplanProgramUseCase @Inject constructor(private val repository: ProgramRepository) {
    suspend operator fun invoke(enrollmentId: String, preferences: ProgramPreferences) =
        repository.replanProgram(enrollmentId, preferences)
}
