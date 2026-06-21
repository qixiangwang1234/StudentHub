package com.studenthub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val courseId: Long? = null,  // null = standalone todo
    val dueDate: Long,           // epoch millis
    val remindAt: Long? = null,  // epoch millis, null = no reminder
    val priority: Int = 0,       // 0=low, 1=medium, 2=high
    val isCompleted: Boolean = false,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = false
)
