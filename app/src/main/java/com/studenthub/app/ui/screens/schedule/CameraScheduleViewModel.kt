package com.studenthub.app.ui.screens.schedule

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

data class CameraScheduleUiState(
    val capturedImage: Bitmap? = null,
    val recognizedText: String? = null,
    val isProcessing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CameraScheduleViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraScheduleUiState())
    val uiState: StateFlow<CameraScheduleUiState> = _uiState.asStateFlow()

    private var cameraController: LifecycleCameraController? = null
    private var imageCapture: ImageCapture? = null

    fun setCameraController(controller: LifecycleCameraController) {
        cameraController = controller
    }

    fun captureImage(context: Context) {
        _uiState.value = _uiState.value.copy(isProcessing = true)

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
                val bitmap = withContext(Dispatchers.IO) {
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

                val text = result.text
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

    fun parseAndSave() {
        val text = _uiState.value.recognizedText ?: return
        viewModelScope.launch {
            // Simple parsing: try to extract course-like patterns
            // Format: "课程名 周几 时间 地点" or similar
            val lines = text.lines().filter { it.isNotBlank() }
            var savedCount = 0

            for (line in lines) {
                val course = tryParseCourse(line)
                if (course != null) {
                    courseRepository.insertCourse(course)
                    savedCount++
                }
            }
        }
    }

    private fun tryParseCourse(text: String): CourseEntity? {
        // Attempt to parse a line of recognized text into a CourseEntity
        val parts = text.trim().split(Regex("[\\s,，\t]+")).filter { it.isNotBlank() }
        if (parts.isEmpty()) return null

        val name = parts.firstOrNull() ?: return null
        val dayMap = mapOf(
            "周一" to 1, "周二" to 2, "周三" to 3, "周四" to 4,
            "周五" to 5, "周六" to 6, "周日" to 7, "星期" to 1
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
            if (part.contains("教") || part.contains("楼") || part.contains("室")) {
                classroom = part
            }
        }

        return CourseEntity(
            name = name,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            classroom = classroom,
            teacher = null,
            colorHex = listOf("#6366f1", "#22c55e", "#f59e0b", "#ef4444", "#8b5cf6")[Random.nextInt(5)]
        )
    }

    override fun onCleared() {
        super.onCleared()
        cameraController?.unbind()
    }
}
