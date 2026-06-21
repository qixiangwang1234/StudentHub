package com.studenthub.app.ai

data class ChatMessage(
    val role: String,  // "user" or "assistant"
    val content: String
)

data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<ChatMessage>,
    val stream: Boolean = false
)

data class DeepSeekResponse(
    val choices: List<Choice> = emptyList()
)

data class Choice(
    val message: ChatMessage? = null
)
