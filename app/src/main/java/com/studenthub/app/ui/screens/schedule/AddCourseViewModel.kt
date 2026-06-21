package com.studenthub.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.domain.repository.CourseRepository
import com.studenthub.app.ui.theme.CourseColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddCourseUiState(
    val name: String = "",
    val teacher: String = "",
    val classroom: String = "",
    val dayOfWeek: Int = 1,
    val startHour: String = "08",
    val startMinute: String = "00",
    val endHour: String = "09",
    val endMinute: String = "35",
    val weekStart: String = "1",
    val weekEnd: String = "20",
    val colorHex: String = "#6366f1",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null
)

@HiltViewModel
class AddCourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCourseUiState())
    val uiState: StateFlow<AddCourseUiState> = _uiState.asStateFlow()

    private var editingCourseId: Long? = null

    fun loadCourse(courseId: Long) {
        viewModelScope.launch {
            val course = courseRepository.getCourseById(courseId) ?: return@launch
            editingCourseId = course.id
            _uiState.value = AddCourseUiState(
                name = course.name,
                teacher = course.teacher ?: "",
                classroom = course.classroom ?: "",
                dayOfWeek = course.dayOfWeek,
                startHour = course.startTime.substringBefore(":"),
                startMinute = course.startTime.substringAfter(":"),
                endHour = course.endTime.substringBefore(":"),
                endMinute = course.endTime.substringAfter(":"),
                weekStart = course.weekStart.toString(),
                weekEnd = course.weekEnd.toString(),
                colorHex = course.colorHex,
                isEditing = true
            )
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = null)
    }

    fun updateTeacher(teacher: String) {
        _uiState.value = _uiState.value.copy(teacher = teacher)
    }

    fun updateClassroom(classroom: String) {
        _uiState.value = _uiState.value.copy(classroom = classroom)
    }

    fun updateDayOfWeek(day: Int) {
        _uiState.value = _uiState.value.copy(dayOfWeek = day)
    }

    fun updateStartTime(hour: String, minute: String) {
        _uiState.value = _uiState.value.copy(startHour = hour, startMinute = minute)
    }

    fun updateEndTime(hour: String, minute: String) {
        _uiState.value = _uiState.value.copy(endHour = hour, endMinute = minute)
    }

    fun updateWeekRange(start: String, end: String) {
        _uiState.value = _uiState.value.copy(weekStart = start, weekEnd = end)
    }

    fun updateColorHex(color: String) {
        _uiState.value = _uiState.value.copy(colorHex = color)
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(nameError = "课程名称不能为空")
            return
        }

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val course = CourseEntity(
                id = editingCourseId ?: 0,
                name = state.name.trim(),
                teacher = state.teacher.trim().ifBlank { null },
                classroom = state.classroom.trim().ifBlank { null },
                dayOfWeek = state.dayOfWeek,
                startTime = "${state.startHour}:${state.startMinute}",
                endTime = "${state.endHour}:${state.endMinute}",
                weekStart = state.weekStart.toIntOrNull() ?: 1,
                weekEnd = state.weekEnd.toIntOrNull() ?: 20,
                colorHex = state.colorHex
            )

            if (editingCourseId != null) {
                courseRepository.updateCourse(course)
            } else {
                courseRepository.insertCourse(course)
            }

            onSuccess()
        }
    }
}
