# StudentHub Phase 2 — Todo/Reminder Module & Red Dot Logic

> **For agentic workers:** REQUIRED SUB-SKILL: Use subagent-driven-development to implement this plan task-by-task.

**Goal:** Implement full Todo CRUD (create, read, update, delete, toggle complete), local notifications with AlarmManager, course-association auto-fill, and red dot indicators on the schedule view.

**Architecture:** New `TodoRepository` in domain layer. Two new screens (TodoList, AddTodo) with ViewModels. Notification helper class. Schedule screen updated to show pending counts. CourseDetail screen updated to list related todos.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, AlarmManager, Navigation Compose

---

## File Structure Changes

```
app/src/main/java/com/studenthub/app/
├── domain/repository/
│   └── TodoRepository.kt                    (NEW)
├── ui/
│   ├── navigation/
│   │   └── AppNavGraph.kt                   (MODIFY — add todo routes)
│   ├── screens/
│   │   └── todo/
│   │       ├── TodoListScreen.kt            (REWRITE — full implementation)
│   │       ├── TodoListViewModel.kt         (NEW)
│   │       ├── AddTodoScreen.kt             (NEW)
│   │       └── AddTodoViewModel.kt          (NEW)
│   └── schedule/
│       ├── ScheduleScreen.kt                (MODIFY — red dot + CourseWithPendingCount)
│       ├── ScheduleViewModel.kt             (MODIFY — use CourseWithPendingCount)
│       └── CourseDetailScreen.kt            (MODIFY — show related todos)
├── notification/
│   └── NotificationHelper.kt               (NEW)
└── di/
    └── AppModule.kt                         (MODIFY — provide TodoRepository)
```

---

### Task 1: TodoRepository

**Files:**
- Create: `app/src/main/java/com/studenthub/app/domain/repository/TodoRepository.kt`
- Modify: `app/src/main/java/com/studenthub/app/di/AppModule.kt` — add TodoRepository provide

- [ ] **Step 1: Create `TodoRepository.kt`**

```kotlin
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
```

- [ ] **Step 2: Add TodoRepository to `AppModule.kt`**

Add these imports and provide method:

```kotlin
import com.studenthub.app.domain.repository.TodoRepository

// Inside AppModule class:
    @Provides
    @Singleton
    fun provideTodoRepository(
        todoDao: TodoDao,
        courseDao: CourseDao
    ): TodoRepository = TodoRepository(todoDao, courseDao)
```

- [ ] **Step 3: Add `getTodoById` to `TodoDao.kt`**

Add this query to the existing `TodoDao.kt`:

```kotlin
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?
```

---

### Task 2: TodoListViewModel

**Files:**
- Create: `app/src/main/java/com/studenthub/app/ui/screens/todo/TodoListViewModel.kt`

- [ ] **Step 1: Create `TodoListViewModel.kt`**

```kotlin
package com.studenthub.app.ui.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthub.app.data.entity.TodoEntity
import com.studenthub.app.domain.repository.TodoRepository
import com.studenthub.app.domain.repository.TodoWithCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TodoFilter { ALL, COURSE, STANDALONE }

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(TodoFilter.ALL)
    val filter: StateFlow<TodoFilter> = _filter.asStateFlow()

    val allTodos: StateFlow<List<TodoWithCourse>> = todoRepository.getAllTodosWithCourse()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTodos: StateFlow<List<TodoWithCourse>> = combine(
        allTodos, _filter
    ) { todos, filter ->
        when (filter) {
            TodoFilter.ALL -> todos
            TodoFilter.COURSE -> todos.filter { it.course != null }
            TodoFilter.STANDALONE -> todos.filter { it.course == null }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> = allTodos.map { todos ->
        todos.count { !it.todo.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setFilter(filter: TodoFilter) {
        _filter.value = filter
    }

    fun toggleTodo(todoWithCourse: TodoWithCourse) {
        viewModelScope.launch {
            todoRepository.toggleTodo(todoWithCourse.todo)
        }
    }

    fun deleteTodo(todoWithCourse: TodoWithCourse) {
        viewModelScope.launch {
            todoRepository.deleteTodo(todoWithCourse.todo)
        }
    }
}
```

---

### Task 3: Full TodoListScreen (replace placeholder)

**Files:**
- Rewrite: `app/src/main/java/com/studenthub/app/ui/screens/todo/TodoListScreen.kt`

- [ ] **Step 1: Rewrite `TodoListScreen.kt`**

```kotlin
package com.studenthub.app.ui.screens.todo

import androidx.compose.animation.*
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

            Spacer(modifier = Modifier.height(12.dp))

            // Filter tabs
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
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(label, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Todo list
            if (filteredTodos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无待办",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
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
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (todo.isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

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

                    // Due date
                    Text(
                        text = "截止 ${dateFormat.format(Date(todo.dueDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            todo.isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            isOverdue -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
```

---

### Task 4: AddTodoViewModel

**Files:**
- Create: `app/src/main/java/com/studenthub/app/ui/screens/todo/AddTodoViewModel.kt`

- [ ] **Step 1: Create `AddTodoViewModel.kt`**

```kotlin
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
    val dueDate: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L, // 7 days later
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
```

---

### Task 5: AddTodoScreen

**Files:**
- Create: `app/src/main/java/com/studenthub/app/ui/screens/todo/AddTodoScreen.kt`

- [ ] **Step 1: Create `AddTodoScreen.kt`**

```kotlin
package com.studenthub.app.ui.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
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
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Course dropdown
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = uiState.selectedCourse?.name ?: "不关联",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(Icons.Default.Notifications, null) },
                        singleLine = true
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
                modifier = Modifier.fillMaxWidth().height(50.dp),
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
```

---

### Task 6: NotificationHelper

**Files:**
- Create: `app/src/main/java/com/studenthub/app/notification/NotificationHelper.kt`

- [ ] **Step 1: Create `NotificationHelper.kt`**

```kotlin
package com.studenthub.app.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.studenthub.app.MainActivity
import com.studenthub.app.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val CHANNEL_ID = "studenthub_reminders"
const val CHANNEL_NAME = "待办提醒"
const val NOTIFICATION_ID_BASE = 1000

object NotificationHelper {

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "课程待办截止提醒"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(context: Context, todoId: Long, title: String, remindAtMillis: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("todo_id", todoId)
            putExtra("todo_title", title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todoId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            remindAtMillis,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context, todoId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todoId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getLongExtra("todo_id", -1)
        val todoTitle = intent.getStringExtra("todo_title") ?: "待办提醒"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            todoId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("待办提醒")
            .setContentText(todoTitle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_BASE + todoId.toInt(), notification)
    }
}
```

Also register the receiver in AndroidManifest.xml:

```xml
<receiver
    android:name=".notification.ReminderReceiver"
    android:exported="false" />
```

And call `NotificationHelper.createNotificationChannel(this)` in `StudentHubApp.onCreate()`:

```kotlin
override fun onCreate() {
    super.onCreate()
    NotificationHelper.createNotificationChannel(this)
}
```

---

### Task 7: Red dot on schedule screen

**Files:**
- Modify: `app/src/main/java/com/studenthub/app/ui/screens/schedule/ScheduleViewModel.kt`
- Modify: `app/src/main/java/com/studenthub/app/ui/screens/schedule/ScheduleScreen.kt`

- [ ] **Step 1: Update `ScheduleViewModel.kt` — change `courses` to use `CourseWithPendingCount`**

Add import for `CourseWithPendingCount` and change the `courses` StateFlow:

```kotlin
import com.studenthub.app.domain.repository.CourseWithPendingCount

// Change this:
// val courses: StateFlow<List<CourseEntity>> = ...
// To:
val coursesWithPending: StateFlow<List<CourseWithPendingCount>> = _selectedDay
    .flatMapLatest { day -> courseRepository.getCoursesByDayWithPendingCount(day) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

- [ ] **Step 2: Update `ScheduleScreen.kt` — add red dot to CourseBlock**

Change the `courses` to `coursesWithPending` collection, and update `CourseBlock` to accept and display pending count:

```kotlin
// Change val courses by viewModel.courses.collectAsState()
// To:
val coursesWithPending by viewModel.coursesWithPending.collectAsState()

// Then where items(courses) is used, change to items(coursesWithPending):
items(coursesWithPending, key = { it.course.id }) { cwp ->
    CourseBlock(
        course = cwp.course,
        pendingCount = cwp.pendingTodoCount,
        onClick = { navController.navigate("course_detail/${cwp.course.id}") }
    )
}

// Also update empty state check:
if (coursesWithPending.isEmpty()) { ... }

// Update CourseBlock signature to accept pendingCount:
@Composable
fun CourseBlock(
    course: CourseEntity,
    pendingCount: Int = 0,
    onClick: () -> Unit
) {
    // ... (same existing code, but add red dot in the top-right of the Card)
    // Add this inside the Card's Row, in the course info Column area:
    if (pendingCount > 0) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(0xFFEF4444), CircleShape)
                .align(Alignment.TopEnd)
        )
    }
}

// Wrap the Card content in a Box to position the red dot:
Box(modifier = Modifier.fillMaxWidth()) {
    // ... existing Row content ...
    if (pendingCount > 0) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 4.dp)
                .size(8.dp)
                .background(Color(0xFFEF4444), CircleShape)
        )
    }
}
```

---

### Task 8: Update CourseDetailScreen — show related todos

**Files:**
- Modify: `app/src/main/java/com/studenthub/app/ui/screens/schedule/CourseDetailScreen.kt`

- [ ] **Step 1: Add a "关联待办" section to CourseDetailScreen**

Add a new Card below the Notes section showing todos for this course. The ViewModel needs to be updated too:

Add to `CourseDetailViewModel.kt`:
```kotlin
import com.studenthub.app.domain.repository.TodoRepository
import com.studenthub.app.data.entity.TodoEntity

// In the constructor:
class CourseDetailViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val todoRepository: TodoRepository  // ADD THIS
) : ViewModel() {

    val courseTodos: StateFlow<List<TodoEntity>> = _course.flatMapLatest { course ->
        if (course != null) todoRepository.getTodosForCourse(course.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```

Add to `CourseDetailScreen.kt` UI (after the Notes card):
```kotlin
// Related todos section
if (courseTodos.isNotEmpty()) {
    Spacer(modifier = Modifier.height(24.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "📋 关联待办",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            courseTodos.forEach { todo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (todo.isCompleted) "✅" else "⬜",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
        }
    }
}
```

Add import: `import androidx.compose.ui.text.style.TextDecoration`

---

### Task 9: Update navigation

**Files:**
- Modify: `app/src/main/java/com/studenthub/app/ui/navigation/AppNavGraph.kt`

- [ ] **Step 1: Add todo routes**

Add imports and routes:
```kotlin
import com.studenthub.app.ui.screens.todo.AddTodoScreen

// In NavHost, add after the todo route:
composable("add_todo") { AddTodoScreen(navController = navController) }
composable(
    route = "edit_todo/{todoId}",
    arguments = listOf(navArgument("todoId") { type = NavType.LongType })
) { backStackEntry ->
    val todoId = backStackEntry.arguments?.getLong("todoId") ?: return@composable
    AddTodoScreen(navController = navController, editTodoId = todoId)
}
```

- [ ] **Step 2: Pass navController to TodoListScreen**

Update the existing `composable("todo")` route:
```kotlin
composable("todo") { TodoListScreen(navController = navController) }
```

---

## Spec Coverage Check

- ✅ **Todo CRUD** — Tasks 1-5 (Repo, VM, Screen, AddScreen)
- ✅ **Todo filtering (全部/课程/其他)** — Task 2 (TodoFilter enum) + Task 3 (FilterChip row)
- ✅ **Course association** — Task 4 (AddTodo VM course selector) + Task 5 (dropdown + auto-fill)
- ✅ **Auto-fill teacher/classroom/time** — Task 5 (auto-fill box in AddTodoScreen)
- ✅ **Due date picker** — Task 5 (Material3 DatePickerDialog)
- ✅ **Reminder/notification** — Task 6 (AlarmManager + NotificationChannel)
- ✅ **Red dot on schedule** — Task 7 (CourseWithPendingCount + red CircleShape)
- ✅ **Course detail shows related todos** — Task 8
- ✅ **Navigation routes** — Task 9 (add_todo / edit_todo/{todoId})
