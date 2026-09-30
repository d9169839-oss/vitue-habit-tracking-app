package com.virtue.habittracker.domain.model.program

enum class ProgramCategory { FITNESS, SELF_GROOMING, MINDFULNESS }
enum class ProgramDifficulty { BEGINNER, INTERMEDIATE }
enum class ProgramStatus { ACTIVE, PAUSED, COMPLETED, ENDED }
enum class ProgramActivityStatus { PENDING, COMPLETED, SKIPPED }
enum class ProgramExperience { BEGINNER, SOME_EXPERIENCE, EXPERIENCED }
enum class WorkActivityLevel { MOSTLY_SEATED, MIXED, PHYSICALLY_ACTIVE }
enum class ProgramEquipment { NONE, HOME_BASIC, GYM }

data class ProgramPhaseTemplate(
    val number: Int,
    val title: String,
    val startDay: Int,
    val endDay: Int,
    val focus: String,
    val activityTitle: String,
    val instructions: String,
    val minutes: Int
)

data class ProgramTemplate(
    val id: String,
    val version: Int,
    val title: String,
    val category: ProgramCategory,
    val description: String,
    val durationDays: Int,
    val difficulty: ProgramDifficulty,
    val defaultMinutesPerDay: Int,
    val isPremium: Boolean,
    val equipment: ProgramEquipment,
    val safetyNote: String,
    val phases: List<ProgramPhaseTemplate>
)

data class ProgramPreferences(
    val availableMinutesPerDay: Int = 20,
    val availableDaysPerWeek: Int = 5,
    val experience: ProgramExperience = ProgramExperience.BEGINNER,
    val workActivityLevel: WorkActivityLevel = WorkActivityLevel.MIXED,
    val equipment: ProgramEquipment = ProgramEquipment.NONE
)

data class ProgramEnrollment(
    val id: String,
    val templateId: String,
    val templateVersion: Int,
    val titleSnapshot: String,
    val category: ProgramCategory,
    val durationDays: Int,
    val startEpochDay: Long,
    val status: ProgramStatus,
    val preferences: ProgramPreferences,
    val isPremium: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

data class ScheduledProgramActivity(
    val id: String,
    val enrollmentId: String,
    val dayIndex: Int,
    val epochDay: Long,
    val phaseTitle: String,
    val title: String,
    val instructions: String,
    val estimatedMinutes: Int,
    val status: ProgramActivityStatus,
    val updatedAtMillis: Long
)

data class ProgramProgress(
    val enrollment: ProgramEnrollment,
    val activities: List<ScheduledProgramActivity>
) {
    val completedCount: Int get() = activities.count { it.status == ProgramActivityStatus.COMPLETED }
    val skippedCount: Int get() = activities.count { it.status == ProgramActivityStatus.SKIPPED }
    val totalCount: Int get() = activities.size
    val progressPercent: Int get() = if (totalCount == 0) 0 else completedCount * 100 / totalCount
    val todayActivities: List<ScheduledProgramActivity>
        get() = activities.filter { it.epochDay == java.time.LocalDate.now().toEpochDay() }
}
