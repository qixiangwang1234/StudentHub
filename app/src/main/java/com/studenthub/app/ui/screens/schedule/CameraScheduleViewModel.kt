package com.studenthub.app.ui.screens.schedule

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.studenthub.app.ai.DeepSeekApi
import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.data.local.SettingsDataStore
import com.studenthub.app.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

data class CameraScheduleUiState(
    val capturedImage: Bitmap? = null,
    val recognizedText: String? = null,
    val isProcessing: Boolean = false,
    val isAiParsing: Boolean = false,
    val aiParseResult: String = "",
    val error: String? = null
)

data class AiCourseItem(
    val name: String = "",
    val dayOfWeek: Int = 1,
    val startTime: String = "08:00",
    val endTime: String = "09:35",
    val classroom: String = "",
    val teacher: String = ""
)

@HiltViewModel
class CameraScheduleViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraScheduleUiState())
    val uiState: StateFlow<CameraScheduleUiState> = _uiState.asStateFlow()

    private var cameraController: LifecycleCameraController? = null
    private var deepSeekApi: DeepSeekApi? = null
    private val gson = Gson()

    init {
        viewModelScope.launch {
            val settings = settingsDataStore.settings.first()
            if (settings.apiKey.isNotBlank()) {
                deepSeekApi = DeepSeekApi(settings.apiKey)
            }
        }
    }

    fun setCameraController(controller: LifecycleCameraController) {
        cameraController = controller
    }

    fun captureImage(context: Context) {
        _uiState.value = _uiState.value.copy(isProcessing = true, error = null)

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val photoFile = File(
            context.cacheDir,
            "schedule_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    processImage(context, photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "拍照失败: ${exception.message}"
                    )
                }
            }
        )
    }

    private fun processImage(context: Context, photoFile: File) {
        viewModelScope.launch {
            try {
                val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeFile(photoFile.absolutePath)
                }

                if (bitmap == null) {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "无法读取图片"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(capturedImage = bitmap)

                // Run OCR
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val recognizer = TextRecognition.getClient(
                    ChineseTextRecognizerOptions.Builder().build()
                )

                val result = withContext(Dispatchers.IO) {
                    recognizer.process(inputImage)
                }

                val text: String = result.text
                if (text.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        recognizedText = "未识别到文字，请重试",
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        recognizedText = text,
                        error = null
                    )
                    // Auto-trigger AI parsing when text is recognized
                    parseWithAI()
                }

                recognizer.close()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "识别失败: ${e.message}"
                )
            }
        }
    }

    fun updateRecognizedText(text: String) {
        _uiState.value = _uiState.value.copy(recognizedText = text)
    }

    fun reset() {
        _uiState.value = CameraScheduleUiState()
    }

    fun parseWithAI() {
        val text = _uiState.value.recognizedText ?: return
        val api = deepSeekApi

        if (api == null) {
            // Fallback to simple parsing if no AI configured
            parseSimple(text)
            return
        }

        _uiState.value = _uiState.value.copy(isAiParsing = true, error = null, aiParseResult = "")

        viewModelScope.launch {
            val settings = settingsDataStore.settings.first()

            val prompt = """你是一个课表识别助手。下面是一段OCR识别出的课表文字，请解析出所有课程信息，并按以下JSON格式返回（不要加markdown标记，直接返回纯JSON数组）：

早上8点到晚上9点，周一至周日

返回格式：
[
  {"name":"课程名","dayOfWeek":1,"startTime":"08:00","endTime":"09:35","classroom":"教室","teacher":"教师"}
]

dayOfWeek: 1=周一 2=周二 3=周三 4=周四 5=周五 6=周六 7=周日
时间格式: HH:mm
如果某些字段缺失，用空字符串代替。

课表文字：
$text"""

            val result = api.chat(settings.apiModel, listOf(
                com.studenthub.app.ai.ChatMessage("user", prompt)
            ))

            result.fold(
                onSuccess = { reply ->
                    try {
                        val cleanJson = reply.trim()
                            .removePrefix("```json").removePrefix("```")
                            .removeSuffix("```").trim()
                        val type = object : TypeToken<List<AiCourseItem>>() {}.type
                        val courses: List<AiCourseItem> = gson.fromJson(cleanJson, type)
                        var savedCount = 0

                        for (item in courses) {
                            if (item.name.isNotBlank()) {
                                val course = CourseEntity(
                                    name = item.name,
                                    dayOfWeek = item.dayOfWeek,
                                    startTime = item.startTime,
                                    endTime = item.endTime,
                                    classroom = item.classroom.ifBlank { null },
                                    teacher = item.teacher.ifBlank { null },
                                    colorHex = COURSE_COLORS[savedCount % COURSE_COLORS.size]
                                )
                                courseRepository.insertCourse(course)
                                savedCount++
                            }
                        }

                        _uiState.value = _uiState.value.copy(
                            isAiParsing = false,
                            aiParseResult = "成功导入 $savedCount 门课程！"
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isAiParsing = false,
                            error = "AI 解析失败: ${e.message}，请手动修正后重试"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isAiParsing = false,
                        error = "AI 请求失败: ${e.message}，使用基础解析模式"
                    )
                    parseSimple(text)
                }
            )
        }
    }

    private fun parseSimple(text: String) {
        viewModelScope.launch {
            val lines = text.lines().filter { it.isNotBlank() }
            var savedCount = 0

            for (line in lines) {
                val course = tryParseCourse(line)
                if (course != null) {
                    courseRepository.insertCourse(course)
                    savedCount++
                }
            }

            _uiState.value = _uiState.value.copy(
                aiParseResult = "基础解析完成，导入 $savedCount 门课程"
            )
        }
    }

    private fun tryParseCourse(text: String): CourseEntity? {
        val parts = text.trim().split(Regex("[\\s,，\t]+")).filter { it.isNotBlank() }
        if (parts.isEmpty()) return null

        val name = parts.firstOrNull() ?: return null
        val dayMap = mapOf(
            "周一" to 1, "周二" to 2, "周三" to 3, "周四" to 4,
            "周五" to 5, "周六" to 6, "周日" to 7
        )

        var dayOfWeek = 1
        var startTime = "08:00"
        var endTime = "09:35"
        var classroom: String? = null

        for (part in parts) {
            dayMap.forEach { (key, value) ->
                if (part.contains(key)) dayOfWeek = value
            }
            if (part.matches(Regex("\\d{1,2}:\\d{2}"))) {
                if (startTime == "08:00") startTime = part
                else endTime = part
            }
            if (part.contains("教") || part.contains("楼") || part.contains("室") || part.contains("馆")) {
                classroom = part
            }
        }

        return CourseEntity(
            name = name,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            classroom = classroom,
            colorHex = COURSE_COLORS[Random.nextInt(COURSE_COLORS.size)]
        )
    }

    override fun onCleared() {
        super.onCleared()
        cameraController?.unbind()
    }

    companion object {
        val COURSE_COLORS = listOf(
            "#6366f1", "#22c55e", "#f59e0b", "#ef4444",
            "#8b5cf6", "#06b6d4", "#ec4899", "#f97316"
        )
    }
}
