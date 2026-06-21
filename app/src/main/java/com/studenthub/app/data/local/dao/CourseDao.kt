package com.studenthub.app.data.local.dao

import androidx.room.*
import com.studenthub.app.data.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE dayOfWeek = :day ORDER BY startTime ASC")
    fun getCoursesByDay(day: Int): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("SELECT * FROM courses WHERE id = :id")
    fun getCourseByIdFlow(id: Long): Flow<CourseEntity?>
}
