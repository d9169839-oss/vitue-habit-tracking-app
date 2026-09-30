package com.virtue.habittracker.data.repository

import com.virtue.habittracker.data.local.ProgramActivityEntity
import com.virtue.habittracker.data.local.ProgramDao
import com.virtue.habittracker.data.local.ProgramEnrollmentEntity
import com.virtue.habittracker.data.sync.HabitSyncScheduler
import com.virtue.habittracker.domain.model.program.ProgramActivityStatus
import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramEnrollment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramProgress
import com.virtue.habittracker.domain.model.program.ProgramStatus
import com.virtue.habittracker.domain.model.program.WorkActivityLevel
import com.virtue.habittracker.domain.model.program.ProgramCatalog
import com.virtue.habittracker.domain.model.program.ScheduledProgramActivity
import com.virtue.habittracker.domain.repository.ProgramRepository
import com.virtue.habittracker.domain.service.ProgramPersonalizationEngine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

@Singleton
class RoomProgramRepository @Inject constructor(
    private val dao: ProgramDao,
    private val cloud: HabitCloudDataSource,
    private val syncScheduler: HabitSyncScheduler,
    private val personalizationEngine: ProgramPersonalizationEngine
) : ProgramRepository {

    override fun observePrograms(): Flow<List<ProgramProgress>> = channelFlow {
        // Clear another account's local cache before exposing program data, then sync in background.
        runCatching { cloud.prepareLocalCacheForCurrentUser() }
        syncScheduler.enqueueSync()
        launch {
            combine(dao.observeEnrollments(), dao.observeActivities()) { enrollments, activities ->
                val activitiesByEnrollment = activities.groupBy { it.enrollmentId }
                enrollments.map { entity ->
                    ProgramProgress(entity.toDomain(), activitiesByEnrollment[entity.id].orEmpty().map { it.toDomain() })
                }
            }.collect { send(it) }
        }
    }

    override suspend fun startProgram(templateId: String, startEpochDay: Long, preferences: ProgramPreferences): String {
        val template = ProgramCatalog.find(templateId) ?: error("Program template not found.")
        val enrollmentId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val enrollment = ProgramEnrollmentEntity(
            id = enrollmentId,
            templateId = template.id,
            templateVersion = template.version,
            titleSnapshot = template.title,
            category = template.category.name,
            durationDays = template.durationDays,
            startEpochDay = startEpochDay,
            status = ProgramStatus.ACTIVE.name,
            availableMinutesPerDay = preferences.availableMinutesPerDay.coerceIn(5, 90),
            availableDaysPerWeek = preferences.availableDaysPerWeek.coerceIn(1, 7),
            experience = preferences.experience.name,
            workActivityLevel = preferences.workActivityLevel.name,
            equipment = preferences.equipment.name,
            isPremium = template.isPremium,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        val schedule = personalizationEngine.buildSchedule(template, enrollmentId, startEpochDay, preferences)
        dao.saveEnrollmentAndSchedule(enrollment, schedule.map { it.toEntity() })
        syncScheduler.enqueueSync()
        return enrollmentId
    }

    override suspend fun setActivityStatus(activityId: String, status: ProgramActivityStatus) {
        val current = dao.getActivity(activityId) ?: return
        dao.updateActivityAndQueue(current.copy(status = status.name, updatedAtMillis = System.currentTimeMillis()))
        syncScheduler.enqueueSync()
    }

    override suspend fun setEnrollmentStatus(enrollmentId: String, status: ProgramStatus) {
        val current = dao.getEnrollment(enrollmentId) ?: return
        dao.updateEnrollmentAndQueue(current.copy(status = status.name, updatedAtMillis = System.currentTimeMillis()))
        syncScheduler.enqueueSync()
    }

    override suspend fun deleteEnrollment(enrollmentId: String) {
        dao.deleteEnrollmentAndQueue(enrollmentId)
        syncScheduler.enqueueSync()
    }

    private fun ProgramEnrollmentEntity.toDomain() = ProgramEnrollment(
        id, templateId, templateVersion, titleSnapshot,
        runCatching { ProgramCategory.valueOf(category) }.getOrDefault(ProgramCategory.FITNESS),
        durationDays, startEpochDay,
        runCatching { ProgramStatus.valueOf(status) }.getOrDefault(ProgramStatus.ACTIVE),
        ProgramPreferences(
            availableMinutesPerDay, availableDaysPerWeek,
            runCatching { ProgramExperience.valueOf(experience) }.getOrDefault(ProgramExperience.BEGINNER),
            runCatching { WorkActivityLevel.valueOf(workActivityLevel) }.getOrDefault(WorkActivityLevel.MIXED),
            runCatching { ProgramEquipment.valueOf(equipment) }.getOrDefault(ProgramEquipment.NONE)
        ),
        isPremium, createdAtMillis, updatedAtMillis
    )

    private fun ProgramActivityEntity.toDomain() = ScheduledProgramActivity(
        id, enrollmentId, dayIndex, epochDay, phaseTitle, title, instructions, estimatedMinutes,
        runCatching { ProgramActivityStatus.valueOf(status) }.getOrDefault(ProgramActivityStatus.PENDING),
        updatedAtMillis
    )

    private fun ScheduledProgramActivity.toEntity() = ProgramActivityEntity(
        id, enrollmentId, dayIndex, epochDay, phaseTitle, title, instructions,
        estimatedMinutes, status.name, updatedAtMillis
    )
}
