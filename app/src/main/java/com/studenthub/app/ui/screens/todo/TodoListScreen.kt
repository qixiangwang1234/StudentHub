package com.studenthub.app.ui.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studenthub.app.domain.repository.TodoWithCourse
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    navController: NavController,
    viewModel: TodoListViewModel = hiltViewModel()
) {
    val filter by viewModel.filter.collectAsState()
    val filteredTodos by viewModel.filteredTodos.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "待办",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (pendingCount > 0) {
                        Text(
                            text = "${pendingCount}项待完成",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { navController.navigate("add_todo") },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("新建")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter chips with emoji icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodoFilter.entries.forEach { f ->
                    val isSelected = filter == f
                    val label = when (f) {
                        TodoFilter.ALL -> "全部"
                        TodoFilter.COURSE -> "课程"
                        TodoFilter.STANDALONE -> "其他"
                    }
                    val icon = when (f) {
                        TodoFilter.ALL -> "📋"
                        TodoFilter.COURSE -> "📚"
                        TodoFilter.STANDALONE -> "📌"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                        leadingIcon = {
                            Text(
                                text = icon,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Todo list or empty state
            if (filteredTodos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🎉 全部完成！",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "太棒了，没有待办事项了",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTodos, key = { it.todo.id }) { todoWithCourse ->
                        TodoItem(
                            todoWithCourse = todoWithCourse,
                            onToggle = { viewModel.toggleTodo(todoWithCourse) },
                            onDelete = { viewModel.deleteTodo(todoWithCourse) },
                            onClick = { navController.navigate("edit_todo/${todoWithCourse.todo.id}") }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TodoItem(
    todoWithCourse: TodoWithCourse,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val todo = todoWithCourse.todo
    val course = todoWithCourse.course
    val isOverdue = !todo.isCompleted && todo.dueDate < System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(IntrinsicSize.Min),
        shape = RoundedCornerShape(14.dp),
        elevation = if (todo.isCompleted) CardDefaults.cardElevation(defaultElevation = 0.dp)
                    else CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Colored accent bar (red for overdue, green for completed, transparent for normal)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        when {
                            isOverdue -> Color(0xFFEF4444)
                            todo.isCompleted -> Color(0xFF22C55E)
                            else -> Color.Transparent
                        }
                    )
            )

            Spacer(modifier = Modifier.width(4.dp))

            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                // Title - smaller and faded for completed items
                Text(
                    text = todo.title,
                    style = if (todo.isCompleted)
                        MaterialTheme.typography.bodyMedium
                    else
                        MaterialTheme.typography.bodyLarge,
                    fontWeight = if (todo.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    color = if (todo.isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Course tag
                    if (course != null) {
                        val courseColor = Color(android.graphics.Color.parseColor(course.colorHex))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(courseColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = courseColor
                            )
                        }
                    }

                    // Due date with overdue warning
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isOverdue) {
                            Text(
                                text = "⚠ ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = "截止 ${dateFormat.format(Date(todo.dueDate))}",
                            style = if (todo.isCompleted) MaterialTheme.typography.labelSmall
                                    else MaterialTheme.typography.bodySmall,
                            color = when {
                                todo.isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                isOverdue -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}
