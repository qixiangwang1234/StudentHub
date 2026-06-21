package com.studenthub.app.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.studenthub.app.domain.repository.CourseRepository
import com.studenthub.app.domain.repository.GradeItemRepository
import com.studenthub.app.domain.repository.TodoRepository
import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.data.entity.GradeItemEntity
import com.studenthub.app.data.entity.TodoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val todoRepository: TodoRepository,
    private val gradeItemRepository: GradeItemRepository
) : ViewModel() {

    private val _course = MutableStateFlow<CourseEntity?>(null)
    val course: StateFlow<CourseEntity?> = _course.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    val courseTodos: StateFlow<List<TodoEntity>> = _course.flatMapLatest { course ->
        if (course != null) todoRepository.getTodosForCourse(course.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gradeItems: StateFlow<List<GradeItemEntity>> = _course.flatMapLatest { course ->
        if (course != null) gradeItemRepository.getGradeItemsForCourse(course.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Grade dialog state
    private var _showGradeDialog = MutableStateFlow(false)
    val showGradeDialog: StateFlow<Boolean> = _showGradeDialog.asStateFlow()
    private var _editingGradeItem = MutableStateFlow<GradeItemEntity?>(null)
    val editingGradeItem: StateFlow<GradeItemEntity?> = _editingGradeItem.asStateFlow()

    fun showAddGradeDialog() {
        _editingGradeItem.value = null
        _showGradeDialog.value = true
    }

    fun showEditGradeDialog(item: GradeItemEntity) {
        _editingGradeItem.value = item
        _showGradeDialog.value = true
    }

    fun dismissGradeDialog() {
        _showGradeDialog.value = false
        _editingGradeItem.value = null
    }

    fun saveGradeItem(name: String, weight: Int, score: Float?, totalScore: Float) {
        val courseId = _course.value?.id ?: return
        viewModelScope.launch {
            val item = _editingGradeItem.value?.copy(
                name = name, weight = weight, score = score, totalScore = totalScore
            ) ?: GradeItemEntity(
                courseId = courseId, name = name, weight = weight, score = score, totalScore = totalScore
            )
            if (_editingGradeItem.value != null) {
                gradeItemRepository.updateGradeItem(item)
            } else {
                gradeItemRepository.insertGradeItem(item)
            }
            dismissGradeDialog()
        }
    }

    fun deleteGradeItem(item: GradeItemEntity) {
        viewModelScope.launch {
            gradeItemRepository.deleteGradeItem(item)
        }
    }

    fun updateGradeScore(item: GradeItemEntity, score: Float) {
        viewModelScope.launch {
            gradeItemRepository.updateGradeItem(item.copy(score = score))
        }
    }

    fun loadCourse(courseId: Long) {
        viewModelScope.launch {
            courseRepository.getCourseByIdFlow(courseId).collect { entity ->
                _course.value = entity
            }
        }
    }

    fun showDeleteDialog() {
        _showDeleteConfirm.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteConfirm.value = false
    }

    fun deleteCourse(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _course.value?.let { courseRepository.deleteCourse(it) }
            onSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: Long,
    navController: NavController,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val course by viewModel.course.collectAsStateWithLifecycle()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsStateWithLifecycle()

    LaunchedEffect(courseId) {
        viewModel.loadCourse(courseId)
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("删除课程") },
            text = { Text("确定要删除「${course?.name}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteCourse(onSuccess = { navController.popBackStack() }) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text("取消")
                }
            }
        )
    }

    val dayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.name ?: "课程详情", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (course != null) {
                        IconButton(onClick = { navController.navigate("edit_course/${courseId}") }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = viewModel::showDeleteDialog) {
                            Icon(Icons.Default.Delete, contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        course?.let { c ->
            val courseColor = Color(android.graphics.Color.parseColor(c.colorHex))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Course info banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = courseColor.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = c.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = courseColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (!c.teacher.isNullOrBlank() || !c.classroom.isNullOrBlank()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = courseColor.copy(alpha = 0.15f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Info grid
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!c.teacher.isNullOrBlank()) {
                                InfoRow(label = "👨‍🏫 教师", value = c.teacher)
                            }
                            if (!c.classroom.isNullOrBlank()) {
                                InfoRow(label = "🏫 教室", value = c.classroom)
                            }
                            InfoRow(label = "⏰ 时间", value = "${dayNames[c.dayOfWeek - 1]} ${c.startTime}—${c.endTime}")
                            InfoRow(label = "📅 周数", value = "第${c.weekStart}—${c.weekEnd}周")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Grade Section
                val gradeItems by viewModel.gradeItems.collectAsStateWithLifecycle()
                val showGradeDialog by viewModel.showGradeDialog.collectAsStateWithLifecycle()
                val editingGradeItem by viewModel.editingGradeItem.collectAsStateWithLifecycle()

                // Grade add/edit dialog
                if (showGradeDialog) {
                    GradeItemDialog(
                        item = editingGradeItem,
                        onDismiss = viewModel::dismissGradeDialog,
                        onSave = viewModel::saveGradeItem
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📊 成绩构成",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = viewModel::showAddGradeDialog) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "添加",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("添加")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (gradeItems.isEmpty()) {
                            Text(
                                text = "暂未设置成绩项目",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            gradeItems.forEach { item ->
                                GradeItemRow(
                                    item = item,
                                    onEdit = { viewModel.showEditGradeDialog(it) },
                                    onDelete = { viewModel.deleteGradeItem(it) }
                                )
                            }
                        }

                        // Weighted total calculation
                        val weightedTotal = remember(gradeItems) {
                            val scored = gradeItems.filter { it.score != null }
                            if (scored.isEmpty()) null
                            else {
                                val totalWeight = scored.sumOf { it.weight.toDouble() }.toFloat()
                                if (totalWeight == 0f) null
                                else {
                                    val weightedSum = scored.sumOf {
                                        (it.score!! / it.totalScore * it.weight).toDouble()
                                    }.toFloat()
                                    (weightedSum / totalWeight) * 100
                                }
                            }
                        }

                        if (weightedTotal != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "当前加权总分",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = String.format("%.1f%%", weightedTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (weightedTotal >= 60f) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (weightedTotal / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (weightedTotal >= 60f) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Notes Section (placeholder for Phase 3)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "📝 备注",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!c.notes.isNullOrBlank()) {
                            Text(
                                text = c.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "暂无备注",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Related todos section
                val courseTodos by viewModel.courseTodos.collectAsState()
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

                Spacer(modifier = Modifier.height(32.dp))
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GradeItemDialog(
    item: GradeItemEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, weight: Int, score: Float?, totalScore: Float) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var weightText by remember { mutableStateOf(item?.weight?.toString() ?: "") }
    var scoreText by remember { mutableStateOf(item?.score?.toString() ?: "") }
    var totalScoreText by remember { mutableStateOf(item?.totalScore?.toString() ?: "100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (item != null) "编辑成绩项目" else "添加成绩项目",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    placeholder = { Text("例如：作业、期中考试") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it.filter { c -> c.isDigit() } },
                    label = { Text("权重 (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = scoreText,
                    onValueChange = { scoreText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("得分（可选）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = totalScoreText,
                    onValueChange = { totalScoreText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("满分") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weight = weightText.toIntOrNull() ?: return@TextButton
                    val totalScore = totalScoreText.toFloatOrNull() ?: return@TextButton
                    val score = scoreText.toFloatOrNull()
                    if (name.isNotBlank() && weight > 0 && totalScore > 0) {
                        onSave(name.trim(), weight, score, totalScore)
                    }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun GradeItemRow(
    item: GradeItemEntity,
    onEdit: (GradeItemEntity) -> Unit,
    onDelete: (GradeItemEntity) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onEdit(item) }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.name}  ${item.weight}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (item.score != null) "${item.score}/${item.totalScore}"
                           else "待录入",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.score != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(
                    onClick = { onEdit(item) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onDelete(item) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
