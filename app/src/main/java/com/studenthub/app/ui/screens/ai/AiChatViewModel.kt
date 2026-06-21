package com.studenthub.app.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthub.app.ai.ChatMessage
import com.studenthub.app.ai.DeepSeekApi
import com.studenthub.app.data.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage("assistant", "你好！我是 StudentHub AI 助手。我可以帮你：\n\n📝 创建待办事项\n📅 查询课表信息\n✏️ 写备注或润色文字\n💡 解答学习问题\n\n有什么可以帮你的？")
    ),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val apiKey: String = "",
    val apiModel: String = "deepseek-chat"
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private var deepSeekApi: DeepSeekApi? = null

    init {
        viewModelScope.launch {
            settingsDataStore.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    apiKey = settings.apiKey,
                    apiModel = settings.apiModel
                )
                if (settings.apiKey.isNotBlank()) {
                    deepSeekApi = DeepSeekApi(settings.apiKey)
                }
            }
        }
    }

    fun updateInput(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text, error = null)
    }

    fun sendMessage() {
        val state = _uiState.value
        val text = state.inputText.trim()
        if (text.isEmpty()) return

        if (state.apiKey.isBlank()) {
            _uiState.value = state.copy(
                error = "请先在「我的」页面配置 DeepSeek API Key",
                inputText = ""
            )
            return
        }

        val userMessage = ChatMessage("user", text)
        val updatedMessages = state.messages + userMessage

        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            inputText = "",
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val api = deepSeekApi
            if (api == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "API 未配置，请检查设置"
                )
                return@launch
            }

            val result = api.chat(state.apiModel, updatedMessages)
            result.fold(
                onSuccess = { reply ->
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + ChatMessage("assistant", reply),
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "请求失败：${e.message}"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
