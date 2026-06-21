package com.studenthub.app.ui.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.data.entity.TodoEntity
import com.studenthub.app.domain.repository.CourseRepository
import com.studenthub.app.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTodoUiState(
    val title: String = "",
    val courseId: Long? = null,
    val selectedCourse: CourseEntity? = null,
    val dueDate: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L,
    val remindAt: Long? = null,
    val priority: Int = 0,
    val notes: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val courses: List<CourseEntity> = emptyList()
)

@HiltViewModel
class AddTodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTodoUiState())
    val uiState: StateFlow<AddTodoUiState> = _uiState.asStateFlow()

    private var editingTodoId: Long? = null

    init {
        viewModelScope.launch {
            courseRepository.getAllCourses().collect { courses ->
                _uiState.value = _uiState.value.copy(courses = courses)
            }
        }
    }

    fun loadTodo(todoId: Long) {
        viewModelScope.launch {
            val todo = todoRepository.getTodoById(todoId) ?: return@launch
            editingTodoId = todo.id
            val course = if (todo.courseId != null) {
                courseRepository.getCourseById(todo.courseId)
            } else null
            _uiState.value = AddTodoUiState(
                title = todo.title,
                courseId = todo.courseId,
                selectedCourse = course,
                dueDate = todo.dueDate,
                remindAt = todo.remindAt,
                priority = todo.priority,
                notes = todo.notes ?: "",
                isEditing = true,
                courses = _uiState.value.courses
            )
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title, titleError = null)
    }

    fun updateCourse(courseId: Long?) {
        val course = if (courseId != null) {
            _uiState.value.courses.find { it.id == courseId }
        } else null
        _uiState.value = _uiState.value.copy(
            courseId = courseId,
            selectedCourse = course
        )
    }

    fun updateDueDate(date: Long) {
        _uiState.value = _uiState.value.copy(dueDate = date)
    }

    fun updateRemindAt(time: Long?) {
        _uiState.value = _uiState.value.copy(remindAt = time)
    }

    fun updatePriority(priority: Int) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(titleError = "标题不能为空")
            return
        }
        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            val todo = TodoEntity(
                id = editingTodoId ?: 0,
                title = state.title.trim(),
                courseId = state.courseId,
                dueDate = state.dueDate,
                remindAt = state.remindAt,
                priority = state.priority,
                notes = state.notes.trim().ifBlank { null },
                isCompleted = false,
                createdAt = System.currentTimeMillis()
            )
            if (editingTodoId != null) {
                todoRepository.updateTodo(todo)
            } else {
                todoRepository.insertTodo(todo)
            }
            onSuccess()
        }
    }
}
