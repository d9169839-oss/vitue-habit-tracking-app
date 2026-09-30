package com.virtue.habittracker.data.local
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(tableName = "habits", indices = [Index("createdEpochDay"), Index("inactiveFromEpochDay")])
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val createdEpochDay: Long,
    val inactiveFromEpochDay: Long? = null
)
@Entity(tableName = "habit_check_ins", primaryKeys = ["habitId", "epochDay"], indices = [Index("epochDay")])
data class HabitCheckInEntity(val habitId: String, val epochDay: Long, val completed: Boolean, val updatedAtMillis: Long)
