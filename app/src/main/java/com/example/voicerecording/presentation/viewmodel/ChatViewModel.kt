package com.example.voicerecording.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicerecording.data.model.AudioMessage
import com.example.voicerecording.domain.repository.AudioRepository
import com.example.voicerecording.domain.usecase.AudioPlayerUseCase
import com.example.voicerecording.domain.usecase.AudioRecorderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    private val audioRecorderUseCase: AudioRecorderUseCase,
    private val audioPlayerUseCase: AudioPlayerUseCase
) : ViewModel() {

    private val _textMessage = MutableStateFlow("")
    val textMessage: StateFlow<String> = _textMessage

    private val _messages = MutableStateFlow<List<AudioMessage>>(emptyList())
    val messages: StateFlow<List<AudioMessage>> = _messages

    val recordingState = audioRecorderUseCase.recordingState
    val amplitude = audioRecorderUseCase.amplitude
    val playbackState = audioPlayerUseCase.playbackState

    private val _isLockRecording = MutableStateFlow(false)
    val isLockRecording: StateFlow<Boolean> = _isLockRecording

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            audioRepository.getAllMessages()
                .collect { messages ->
                    _messages.value = messages
                }
        }
    }

    fun startRecording() {
        audioRecorderUseCase.startRecording()
    }

    fun stopRecording() {
        val (filePath, duration) = audioRecorderUseCase.stopRecording()

        if (filePath != null && duration > 0) {
            viewModelScope.launch {
                val audioMessage = AudioMessage(
                    audioPath = filePath,
                    duration = duration
                )
                audioRepository.insertMessage(audioMessage)
            }
        }
    }

    fun cancelRecording() {
        audioRecorderUseCase.cancelRecording()
        _isLockRecording.value = false
    }

    fun toggleLockRecording() {
        _isLockRecording.value = !_isLockRecording.value
    }

    fun playAudio(audioMessage: AudioMessage) {
        audioPlayerUseCase.playAudio(audioMessage.audioPath)
    }

    fun pauseAudio() {
        audioPlayerUseCase.pausePlayback()
    }

    fun resumeAudio() {
        audioPlayerUseCase.resumePlayback()
    }

    fun stopAudio() {
        audioPlayerUseCase.stopPlayback()
    }

    fun sendTextMessage() {
        val messageText = _textMessage.value.trim()
        if (messageText.isNotEmpty()) {
            viewModelScope.launch {

                val textMessage = AudioMessage(
                    audioPath = "",
                    duration = 0,
                    text = messageText
                )

                audioRepository.insertMessage(textMessage)

                _textMessage.value = ""
            }
        }
    }

    fun updateTextMessage(text: String) {
        _textMessage.value = text
    }

    fun deleteMessage(message: AudioMessage) {
        viewModelScope.launch {
            audioRepository.deleteMessage(message.id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorderUseCase.cleanup()
        audioPlayerUseCase.cleanup()
    }
}