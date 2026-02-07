package com.example.voicerecording.domain.usecase

import android.media.MediaPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

class AudioPlayerUseCase @Inject constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private var playbackJob: Job? = null
    private var currentPath: String? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState

    sealed class PlaybackState {
        object Idle : PlaybackState()
        data class Loading(val filePath: String) : PlaybackState()
        data class Playing(val filePath: String, val currentPosition: Int, val duration: Int) : PlaybackState()
        data class Paused(val filePath: String, val currentPosition: Int, val duration: Int) : PlaybackState()
        data class Finished(val filePath: String) : PlaybackState()
        data class Error(val filePath: String?, val message: String) : PlaybackState()
    }

    fun playAudio(filePath: String) {
        if (currentPath == filePath && _playbackState.value is PlaybackState.Paused) {
            resumePlayback()
            return
        }

        stopPlayback()
        currentPath = filePath
        _playbackState.value = PlaybackState.Loading(filePath)

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener {
                    start()
                    startPlaybackMonitoring(filePath)
                    _playbackState.value = PlaybackState.Playing(
                        filePath = filePath,
                        currentPosition = 0,
                        duration = it.duration
                    )
                }
                setOnCompletionListener {
                    _playbackState.value = PlaybackState.Finished(filePath)
                    playbackJob?.cancel()
                }
                setOnErrorListener { _, what, extra ->
                    _playbackState.value = PlaybackState.Error(filePath, "Playback error: $what, $extra")
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.Error(filePath, "Failed to play audio: ${e.message}")
        }
    }

    private fun startPlaybackMonitoring(filePath: String) {
        playbackJob?.cancel()
        playbackJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val currentPos = mediaPlayer?.currentPosition ?: 0
                val duration = mediaPlayer?.duration ?: 0

                if (duration > 0) {
                    _playbackState.value = PlaybackState.Playing(filePath, currentPos, duration)
                }

                delay(100)
            }
        }
    }

    fun pausePlayback() {
        val path = currentPath ?: return
        val currentPos = mediaPlayer?.currentPosition ?: 0
        val duration = mediaPlayer?.duration ?: 0
        mediaPlayer?.pause()
        playbackJob?.cancel()
        _playbackState.value = PlaybackState.Paused(path, currentPos, duration)
    }

    fun resumePlayback() {
        val path = currentPath ?: return
        mediaPlayer?.start()
        startPlaybackMonitoring(path)
        _playbackState.value = PlaybackState.Playing(
            filePath = path,
            currentPosition = mediaPlayer?.currentPosition ?: 0,
            duration = mediaPlayer?.duration ?: 0
        )
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (e: Exception) {}
            release()
        }
        mediaPlayer = null
        currentPath = null
        _playbackState.value = PlaybackState.Idle
    }

    fun seekTo(position: Int) {
        val path = currentPath ?: return
        mediaPlayer?.seekTo(position)
        val duration = mediaPlayer?.duration ?: 0
        if (mediaPlayer?.isPlaying == true) {
            _playbackState.value = PlaybackState.Playing(
                filePath = path,
                currentPosition = position,
                duration = duration
            )
        } else {
            _playbackState.value = PlaybackState.Paused(path, position, duration)
        }
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun cleanup() {
        stopPlayback()
    }
}