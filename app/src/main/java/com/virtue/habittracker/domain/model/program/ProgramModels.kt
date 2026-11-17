package com.virtue.habittracker.domain.model.program

enum class ProgramCategory { FITNESS, SELF_GROOMING, MINDFULNESS, PERSONAL_DEVELOPMENT }

/** Bundled artwork identifiers; drawable resources stay in the presentation layer. */
enum class ProgramArtwork { DEFAULT, FITNESS_WELLNESS, PERSONAL_DEVELOPMENT }
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
    val phases: List<ProgramPhaseTemplate>,
    val artwork: ProgramArtwork = ProgramArtwork.DEFAULT
)

enum class BiologicalSex { MALE, FEMALE }
enum class DailyOccupation { DESK, LIGHT, ON_FEET, PHYSICAL }

data class Biometrics(
    val sex: BiologicalSex = BiologicalSex.MALE,
    val age: Int = 25,
    val heightCm: Double = 178.0,
    val weightKg: Double = 72.0,
    val occupation: DailyOccupation = DailyOccupation.DESK
)

data class ProgramPreferences(
    val biometrics: Biometrics = Biometrics(),
    val availableMinutesPerDay: Int = 30,
    val availableDaysPerWeek: Int = 4,
    val experience: ProgramExperience = ProgramExperience.BEGINNER,
    val workActivityLevel: WorkActivityLevel = WorkActivityLevel.MOSTLY_SEATED,
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
    // Rest/recovery days are part of the schedule, but are not workout tasks and must not
    // reduce the user's completion percentage.
    val measurableActivities: List<ScheduledProgramActivity>
        get() = activities.filter { it.estimatedMinutes > 0 }
    val completedCount: Int get() = measurableActivities.count { it.status == ProgramActivityStatus.COMPLETED }
    val skippedCount: Int get() = measurableActivities.count { it.status == ProgramActivityStatus.SKIPPED }
    val totalCount: Int get() = measurableActivities.size
    val progressPercent: Int get() = if (totalCount == 0) 0 else completedCount * 100 / totalCount
    val todayActivities: List<ScheduledProgramActivity>
        get() = activities.filter { it.epochDay == java.time.LocalDate.now().toEpochDay() }
}
