package com.example.voicerecording.domain.usecase


import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.math.log10

class AudioRecorderUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFilePath: String? = null
    private var recordingStartTime: Long = 0

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude

    private var amplitudeJob: Job? = null

    sealed class RecordingState {
        object Idle : RecordingState()
        object Preparing : RecordingState()
        data class Recording(val duration: Long, val filePath: String) : RecordingState()
        data class Finished(val filePath: String, val duration: Long) : RecordingState()
        data class Error(val message: String) : RecordingState()
    }

    fun startRecording(): String? {
        return try {
            _recordingState.value = RecordingState.Preparing

            // Create unique file name
            val fileName = "audio_${System.currentTimeMillis()}.m4a"
            val outputFile = File(context.getExternalFilesDir(null), fileName)
            recordingFilePath = outputFile.absolutePath

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            // Use 'apply' but don't expect it to return the string
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)

                prepare()
                start()
            }

            recordingStartTime = System.currentTimeMillis()
            _recordingState.value = RecordingState.Recording(
                duration = 0,
                filePath = outputFile.absolutePath
            )

            // Start amplitude monitoring
            startAmplitudeMonitoring()

            // RETURN the path string here, outside of the mediaRecorder block
            recordingFilePath

        } catch (e: IOException) {
            _recordingState.value = RecordingState.Error("Failed to start recording: ${e.message}")
            null
        } catch (e: Exception) {
            _recordingState.value = RecordingState.Error("Error: ${e.message}")
            null
        }
    }
    private fun startAmplitudeMonitoring() {
        amplitudeJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && mediaRecorder != null) {
                val currentAmplitude = mediaRecorder?.maxAmplitude?.toFloat() ?: 0f
                val amplitudeDb = 20 * log10(currentAmplitude.coerceAtLeast(1f))
                _amplitude.value = amplitudeDb.coerceIn(0f, 100f)

                // Update duration
                if (_recordingState.value is RecordingState.Recording) {
                    val currentState = _recordingState.value as RecordingState.Recording
                    val duration = System.currentTimeMillis() - recordingStartTime
                    _recordingState.value = currentState.copy(duration = duration)
                }

                delay(50) // Update every 50ms
            }
        }
    }

    fun stopRecording(): Pair<String?, Long> {
        return try {
            amplitudeJob?.cancel()

            mediaRecorder?.apply {
                stop()
                release()
            }

            mediaRecorder = null

            val duration = System.currentTimeMillis() - recordingStartTime
            val filePath = recordingFilePath

            if (filePath != null && duration > 0) {
                _recordingState.value = RecordingState.Finished(filePath, duration)
            } else {
                _recordingState.value = RecordingState.Idle
            }

            Pair(filePath, duration)
        } catch (e: Exception) {
            _recordingState.value = RecordingState.Error("Failed to stop recording: ${e.message}")
            Pair(null, 0)
        }
    }

    fun cancelRecording() {
        amplitudeJob?.cancel()

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
        }

        mediaRecorder = null

        // Delete the recording file if it exists
        recordingFilePath?.let { path ->
            try {
                File(path).delete()
            } catch (e: Exception) {
            }
        }

        _recordingState.value = RecordingState.Idle
    }

    fun getCurrentDuration(): Long {
        return if (recordingStartTime > 0) {
            System.currentTimeMillis() - recordingStartTime
        } else {
            0
        }
    }

    fun isRecording(): Boolean {
        return _recordingState.value is RecordingState.Recording
    }

    fun cleanup() {
        amplitudeJob?.cancel()
        mediaRecorder?.release()
        mediaRecorder = null
        _recordingState.value = RecordingState.Idle
    }
}