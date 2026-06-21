package com.studenthub.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthub.app.domain.repository.CourseRepository
import com.studenthub.app.domain.repository.CourseWithPendingCount
import com.studenthub.app.data.entity.CourseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(getCurrentDayOfWeek())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    val courses: StateFlow<List<CourseEntity>> = _selectedDay
        .flatMapLatest { day -> courseRepository.getCoursesByDay(day) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coursesWithPending: StateFlow<List<CourseWithPendingCount>> = _selectedDay
        .flatMapLatest { day -> courseRepository.getCoursesByDayWithPendingCount(day) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCourses: StateFlow<List<CourseEntity>> = courseRepository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    suspend fun deleteCourse(course: CourseEntity) {
        courseRepository.deleteCourse(course)
    }

    private fun getCurrentDayOfWeek(): Int {
        val calendar = java.util.Calendar.getInstance()
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            java.util.Calendar.MONDAY -> 1
            java.util.Calendar.TUESDAY -> 2
            java.util.Calendar.WEDNESDAY -> 3
            java.util.Calendar.THURSDAY -> 4
            java.util.Calendar.FRIDAY -> 5
            java.util.Calendar.SATURDAY -> 6
            java.util.Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
