package com.studenthub.app.ui.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoScreen(
    navController: NavController,
    editTodoId: Long? = null,
    preselectedCourseId: Long? = null,
    viewModel: AddTodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(editTodoId) {
        if (editTodoId != null && editTodoId > 0) {
            viewModel.loadTodo(editTodoId)
        }
    }
    LaunchedEffect(preselectedCourseId) {
        if (preselectedCourseId != null && uiState.courses.isNotEmpty()) {
            viewModel.updateCourse(preselectedCourseId)
        }
    }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    // Date picker dialog
    var showDatePicker by remember { mutableStateOf(false) }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dueDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateDueDate(it) }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "编辑待办" else "新建待办", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Title
            Text("标题", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                placeholder = { Text("例如：高数作业 P87") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Course association
            Text("关联课程", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = uiState.selectedCourse?.name ?: "不关联",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("不关联") },
                        onClick = {
                            viewModel.updateCourse(null)
                            expanded = false
                        }
                    )
                    uiState.courses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text(course.name) },
                            onClick = {
                                viewModel.updateCourse(course.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Auto-fill area when a course is selected
            if (uiState.selectedCourse != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val courseColor = Color(android.graphics.Color.parseColor(uiState.selectedCourse!!.colorHex))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(courseColor.copy(alpha = 0.06f))
                        .padding(14.dp)
                ) {
                    Column {
                        Text("🤖 自动填入", style = MaterialTheme.typography.labelSmall, color = courseColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        uiState.selectedCourse?.let { c ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!c.teacher.isNullOrBlank())
                                    Text("👨‍🏫 ${c.teacher}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (!c.classroom.isNullOrBlank())
                                    Text("📍 ${c.classroom}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("⏰ ${c.startTime}—${c.endTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Due date
            Text("截止时间", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = dateFormat.format(Date(uiState.dueDate)),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                trailingIcon = { Text("📅") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            Text("备注（选填）", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                placeholder = { Text("写点备注...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save button
            Button(
                onClick = { viewModel.save(onSuccess = { navController.popBackStack() }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(if (uiState.isEditing) "保存修改" else "添加待办", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
