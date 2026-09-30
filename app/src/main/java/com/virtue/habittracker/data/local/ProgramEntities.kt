package com.virtue.habittracker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "program_enrollments", indices = [Index("startEpochDay"), Index("status")])
data class ProgramEnrollmentEntity(
    @PrimaryKey val id: String,
    val templateId: String,
    val templateVersion: Int,
    val titleSnapshot: String,
    val category: String,
    val durationDays: Int,
    val startEpochDay: Long,
    val status: String,
    val availableMinutesPerDay: Int,
    val availableDaysPerWeek: Int,
    val experience: String,
    val equipment: String,
    val isPremium: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(tableName = "program_activities", indices = [Index("enrollmentId"), Index("epochDay")])
data class ProgramActivityEntity(
    @PrimaryKey val id: String,
    val enrollmentId: String,
    val dayIndex: Int,
    val epochDay: Long,
    val phaseTitle: String,
    val title: String,
    val instructions: String,
    val estimatedMinutes: Int,
    val status: String,
    val updatedAtMillis: Long
)
