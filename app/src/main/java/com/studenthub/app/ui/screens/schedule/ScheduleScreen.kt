package com.studenthub.app.ui.screens.schedule

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.domain.repository.CourseWithPendingCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val selectedDay by viewModel.selectedDay.collectAsState()
    val coursesWithPending by viewModel.coursesWithPending.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()

    val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("add_course") },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加课程",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)) {
                // Header with camera icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "课表",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2026年 春季学期",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Camera button as secondary action
                    IconButton(
                        onClick = { navController.navigate("camera_schedule") }
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "拍课表",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day tabs with animated indicator
                DayTabs(
                    selectedDay = selectedDay,
                    dayLabels = dayLabels,
                    allCourses = allCourses,
                    onDaySelected = { viewModel.selectDay(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Course list or empty state
                if (coursesWithPending.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📅 这天还没有课程",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "点击右下角 ➕ 添加新课程吧~",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(coursesWithPending, key = { it.course.id }) { cwp ->
                            CourseBlock(
                                course = cwp.course,
                                pendingCount = cwp.pendingTodoCount,
                                onClick = { navController.navigate("course_detail/${cwp.course.id}") }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayTabs(
    selectedDay: Int,
    dayLabels: List<String>,
    allCourses: List<CourseEntity>,
    onDaySelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        dayLabels.forEachIndexed { index, label ->
            val dayNumber = index + 1
            val isSelected = selectedDay == dayNumber
            val hasCourse = allCourses.any { it.dayOfWeek == dayNumber }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDaySelected(dayNumber) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Animated underline indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isSelected) 0.5f else 0f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .animateContentSize(tween(300))
                )
                // Red dot for unselected tabs that have courses
                if (hasCourse && !isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun CourseBlock(
    course: CourseEntity,
    pendingCount: Int = 0,
    onClick: () -> Unit
) {
    val courseColor = Color(android.graphics.Color.parseColor(course.colorHex))

    // Pulsing animation for red dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(tween(300)),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = courseColor.copy(alpha = 0.08f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp)) {
                // Time column
                Column(
                    modifier = Modifier.width(52.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = course.startTime.substringBefore(":"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = courseColor
                    )
                    Text(
                        text = course.startTime.substringAfter(":"),
                        style = MaterialTheme.typography.labelSmall,
                        color = courseColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(24.dp)
                            .background(courseColor.copy(alpha = 0.2f), RoundedCornerShape(1.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = course.endTime.substringBefore(":"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = courseColor.copy(alpha = 0.6f)
                    )
                    Text(
                        text = course.endTime.substringAfter(":"),
                        style = MaterialTheme.typography.labelSmall,
                        color = courseColor.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Vertical color bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(60.dp)
                        .background(courseColor, RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Course info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (!course.classroom.isNullOrBlank()) {
                        Text(
                            text = "📍 ${course.classroom}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    if (!course.teacher.isNullOrBlank()) {
                        Text(
                            text = "👨‍🏫 ${course.teacher}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Red dot indicator with pulse animation
            if (pendingCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = 6.dp)
                        .scale(pulseScale)
                        .size(10.dp)
                        .background(Color(0xFFEF4444), CircleShape)
                )
            }
        }
    }
}
