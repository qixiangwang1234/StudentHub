package com.studenthub.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.domain.repository.CourseRepository
import com.studenthub.app.domain.repository.TodoRepository
import com.studenthub.app.domain.repository.TodoWithCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.text.SimpleDateFormat
import java.util.*

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val todoRepository: TodoRepository
) : ViewModel() {
    val todayCourses: StateFlow<List<CourseEntity>> = courseRepository.getCoursesByDay(getCurrentDayOfWeek())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTodos: StateFlow<List<TodoWithCourse>> = todoRepository.getAllTodosWithCourse()
        .map { list -> list.filter { !it.todo.isCompleted }.take(3) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> = todoRepository.getAllTodosWithCourse()
        .map { list -> list.count { !it.todo.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun getCurrentDayOfWeek(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1; Calendar.TUESDAY -> 2; Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4; Calendar.FRIDAY -> 5; Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7; else -> 1
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val todayCourses by viewModel.todayCourses.collectAsStateWithLifecycle()
    val pendingTodos by viewModel.pendingTodos.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()

    val dateFormat = SimpleDateFormat("M月d日", Locale.getDefault())
    val dayNames = listOf("日", "一", "二", "三", "四", "五", "六")
    val calendar = Calendar.getInstance()
    val dayOfWeek = dayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..5 -> "夜深了 🌙"
        in 6..11 -> "早上好 👋"
        in 12..13 -> "中午好 ☀️"
        in 14..17 -> "下午好 🌤️"
        else -> "晚上好 🌆"
    }

    // Compute next class and sorted courses
    val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    val sortedCourses = remember(todayCourses) {
        todayCourses.sortedBy { course ->
            val parts = course.startTime.split(":")
            (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: 0)
        }
    }
    val nextClass = remember(sortedCourses, currentMinutes) {
        sortedCourses.firstOrNull { course ->
            val parts = course.startTime.split(":")
            val startMinutes = (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: 0)
            startMinutes >= currentMinutes
        }
    }
    val otherClasses = remember(sortedCourses, nextClass) {
        sortedCourses.filter { it.id != nextClass?.id }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(bottom = 64.dp),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Greeting with avatar placeholder
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = greeting, style = MaterialTheme.typography.headlineMedium)
                            Text(
                                text = "${dateFormat.format(Date())} 星期$dayOfWeek",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Circular avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "U",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Today's courses - redesigned with next class highlight
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(0.dp)) {
                            Text(
                                text = "📚 今日课程",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (todayCourses.isEmpty()) {
                                Text(
                                    text = "今天没有课，享受自由时光吧 🎉",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            } else {
                                // Next class - prominently displayed with accent background
                                if (nextClass != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "下一节: ${nextClass.name}",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${nextClass.startTime}—${nextClass.endTime}${if (!nextClass.classroom.isNullOrBlank()) " · ${nextClass.classroom}" else ""}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                // Other classes listed below
                                otherClasses.forEach { course ->
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 6.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = course.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "${course.startTime}—${course.endTime}${if (!course.classroom.isNullOrBlank()) " · ${course.classroom}" else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Pending todos with badge
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📋 待办速览",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (pendingCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            text = "${pendingCount}项待完成",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (pendingTodos.isEmpty()) {
                                Text(
                                    text = "暂无待办，轻松一下 🎉",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                pendingTodos.forEach { twc ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⬜", style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = twc.todo.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Grades card removed (was always showing "暂无数据", adds clutter)

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }

        // AI Input bar - unified gradient bar
        if (navController != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                tonalElevation = 0.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .clickable { navController.navigate("ai_chat") }
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "✨ AI 帮我创建待办、写备注...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
