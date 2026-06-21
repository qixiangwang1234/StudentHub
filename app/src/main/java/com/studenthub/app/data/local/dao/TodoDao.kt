package com.studenthub.app.data.local.dao

import androidx.room.*
import com.studenthub.app.data.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE courseId = :courseId ORDER BY dueDate ASC")
    fun getTodosForCourse(courseId: Long): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?

    @Query("SELECT * FROM todos WHERE courseId IS NULL ORDER BY dueDate ASC")
    fun getStandaloneTodos(): Flow<List<TodoEntity>>

    @Query("SELECT COUNT(*) FROM todos WHERE courseId = :courseId AND isCompleted = 0")
    fun getPendingCountForCourse(courseId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
}
