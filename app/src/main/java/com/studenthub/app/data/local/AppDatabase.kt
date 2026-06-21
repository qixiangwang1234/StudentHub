package com.studenthub.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.data.entity.GradeItemEntity
import com.studenthub.app.data.entity.TodoEntity
import com.studenthub.app.data.local.dao.CourseDao
import com.studenthub.app.data.local.dao.GradeItemDao
import com.studenthub.app.data.local.dao.TodoDao

@Database(
    entities = [CourseEntity::class, GradeItemEntity::class, TodoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun gradeItemDao(): GradeItemDao
    abstract fun todoDao(): TodoDao
}
