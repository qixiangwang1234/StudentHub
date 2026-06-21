package com.studenthub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val teacher: String? = null,
    val classroom: String? = null,
    val dayOfWeek: Int,          // 1=Monday ... 7=Sunday
    val startTime: String,       // "08:00"
    val endTime: String,         // "09:35"
    val weekStart: Int = 1,
    val weekEnd: Int = 20,
    val colorHex: String = "#6366f1",
    val notes: String? = null
)
