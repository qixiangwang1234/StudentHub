package com.studenthub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grade_items")
data class GradeItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long,
    val name: String,            // "作业", "期中", "期末"
    val weight: Int,             // 20, 30, 50
    val score: Float? = null,    // actual score, filled after exam
    val totalScore: Float = 100f // full marks, default 100
)
