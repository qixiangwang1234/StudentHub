package com.studenthub.app.ui.screens.profile

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.studenthub.app.data.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    val settings = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.studenthub.app.data.local.AppSettings())

    fun saveApiKey(key: String) {
        viewModelScope.launch { settingsDataStore.saveApiKey(key) }
    }

    fun saveApiModel(model: String) {
        viewModelScope.launch { settingsDataStore.saveApiModel(model) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.saveDarkMode(enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.saveNotificationsEnabled(enabled) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var apiKey by remember(settings) { mutableStateOf(settings.apiKey) }
    var showKey by remember { mutableStateOf(false) }
    var showModelDropdown by remember { mutableStateOf(false) }
    val models = listOf("deepseek-chat", "deepseek-reasoner")

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // ─── Section: AI 配置 ───
                SectionLabel(text = "AI 配置")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        // API Key
                        Text("DeepSeek API Key", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            placeholder = { Text("sk-xxxxxxxxxxxxxxxx") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (showKey) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            trailingIcon = {
                                TextButton(onClick = { showKey = !showKey }) {
                                    Text(if (showKey) "隐藏" else "显示", fontWeight = FontWeight.Medium)
                                }
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Model
                        Text("AI 模型", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(6.dp))
                        ExposedDropdownMenuBox(
                            expanded = showModelDropdown,
                            onExpandedChange = { showModelDropdown = !showModelDropdown }
                        ) {
                            OutlinedTextField(
                                value = settings.apiModel,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showModelDropdown) },
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = showModelDropdown,
                                onDismissRequest = { showModelDropdown = false }
                            ) {
                                models.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model) },
                                        onClick = {
                                            viewModel.saveApiModel(model)
                                            showModelDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Save + status
                        Button(
                            onClick = { viewModel.saveApiKey(apiKey) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = apiKey != settings.apiKey
                        ) {
                            Text("保存配置")
                        }

                        if (settings.apiKey.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✅ API 已配置 — ${settings.apiModel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ─── Section: 外观 ───
                SectionLabel(text = "外观")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Dark mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("深色模式", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = if (settings.darkMode) "已开启" else "已关闭",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.darkMode,
                                onCheckedChange = viewModel::toggleDarkMode
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("待办提醒", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = if (settings.notificationsEnabled) "已开启" else "已关闭",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.notificationsEnabled,
                                onCheckedChange = viewModel::toggleNotifications
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ─── Section: 关于 ───
                SectionLabel(text = "关于")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("StudentHub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("v1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "完全离线的学生智能助手。数据仅存储在本机，无需注册账号。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}
