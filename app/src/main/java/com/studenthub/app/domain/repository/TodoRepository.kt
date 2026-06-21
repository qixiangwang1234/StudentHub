package com.studenthub.app.domain.repository

import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.data.entity.TodoEntity
import com.studenthub.app.data.local.dao.CourseDao
import com.studenthub.app.data.local.dao.TodoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class TodoWithCourse(
    val todo: TodoEntity,
    val course: CourseEntity?
)

@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao,
    private val courseDao: CourseDao
) {
    fun getAllTodosWithCourse(): Flow<List<TodoWithCourse>> {
        return combine(todoDao.getAllTodos(), courseDao.getAllCourses()) { todos, courses ->
            todos.map { todo ->
                TodoWithCourse(
                    todo = todo,
                    course = courses.find { it.id == todo.courseId }
                )
            }
        }
    }

    fun getTodosForCourse(courseId: Long): Flow<List<TodoEntity>> =
        todoDao.getTodosForCourse(courseId)

    fun getStandaloneTodosWithCourse(): Flow<List<TodoWithCourse>> {
        return todoDao.getStandaloneTodos().map { todos ->
            todos.map { TodoWithCourse(it, null) }
        }
    }

    fun getPendingCountForCourse(courseId: Long): Flow<Int> =
        todoDao.getPendingCountForCourse(courseId)

    suspend fun getTodoById(id: Long): TodoEntity? = todoDao.getTodoById(id)

    suspend fun insertTodo(todo: TodoEntity): Long = todoDao.insertTodo(todo)

    suspend fun updateTodo(todo: TodoEntity) = todoDao.updateTodo(todo)

    suspend fun deleteTodo(todo: TodoEntity) = todoDao.deleteTodo(todo)

    suspend fun toggleTodo(todo: TodoEntity) {
        todoDao.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
    }
}
