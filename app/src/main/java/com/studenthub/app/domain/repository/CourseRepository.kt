package com.studenthub.app.domain.repository

import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.data.local.dao.CourseDao
import com.studenthub.app.data.local.dao.TodoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

data class CourseWithPendingCount(
    val course: CourseEntity,
    val pendingTodoCount: Int
)

@Singleton
class CourseRepository @Inject constructor(
    private val courseDao: CourseDao,
    private val todoDao: TodoDao
) {
    fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()

    fun getCoursesByDay(day: Int): Flow<List<CourseEntity>> = courseDao.getCoursesByDay(day)

    fun getCoursesByDayWithPendingCount(day: Int): Flow<List<CourseWithPendingCount>> {
        return courseDao.getCoursesByDay(day).combine(todoDao.getAllTodos()) { courses, todos ->
            courses.map { course ->
                val pendingCount = todos.count {
                    it.courseId == course.id && !it.isCompleted
                }
                CourseWithPendingCount(course, pendingCount)
            }
        }
    }

    suspend fun getCourseById(id: Long): CourseEntity? = courseDao.getCourseById(id)

    fun getCourseByIdFlow(id: Long): Flow<CourseEntity?> = courseDao.getCourseByIdFlow(id)

    suspend fun insertCourse(course: CourseEntity): Long = courseDao.insertCourse(course)

    suspend fun updateCourse(course: CourseEntity) = courseDao.updateCourse(course)

    suspend fun deleteCourse(course: CourseEntity) = courseDao.deleteCourse(course)
}
