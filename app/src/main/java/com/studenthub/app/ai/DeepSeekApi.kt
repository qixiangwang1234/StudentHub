package com.studenthub.app.ai

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class DeepSeekApi(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json".toMediaType()
    private val url = "https://api.deepseek.com/chat/completions"

    suspend fun chat(model: String, messages: List<ChatMessage>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = DeepSeekRequest(
                    model = model,
                    messages = messages,
                    stream = false
                )
                val jsonBody = gson.toJson(requestBody)
                val body = jsonBody.toRequestBody(jsonMediaType)

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("API error ${response.code}: $responseBody")
                    )
                }

                val deepSeekResponse = gson.fromJson(responseBody, DeepSeekResponse::class.java)
                val content = deepSeekResponse.choices.firstOrNull()?.message?.content
                    ?: "抱歉，我没有理解你的问题。"

                Result.success(content)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
