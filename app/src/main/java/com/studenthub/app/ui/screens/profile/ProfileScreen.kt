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
import com.studenthub.app.data.local.AppSettings
import com.studenthub.app.data.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun saveApiKey(key: String) {
        viewModelScope.launch { settingsDataStore.saveApiKey(key) }
    }

    fun saveApiModel(model: String) {
        viewModelScope.launch { settingsDataStore.saveApiModel(model) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = hiltViewModel()
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
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "我的", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            // API Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🤖 AI 配置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "配置 DeepSeek API 以使用 AI 功能",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // API Key
                    Text("API Key", style = MaterialTheme.typography.labelLarge)
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
                                Text(if (showKey) "隐藏" else "显示")
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Model selection
                    Text("模型", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = showModelDropdown,
                        onExpandedChange = { showModelDropdown = it }
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

                    Spacer(modifier = Modifier.height(12.dp))

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
                            text = "✅ API 已配置",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "关于",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("StudentHub v1.0.0", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "完全离线的学生智能助手",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "数据仅存储在本机，无需注册账号",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
