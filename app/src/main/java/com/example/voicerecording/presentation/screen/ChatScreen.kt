package com.example.voicerecording.presentation.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voicerecording.presentation.components.AudioPlayerUI
import com.example.voicerecording.presentation.components.AudioRecorderUI
import com.example.voicerecording.presentation.components.MessageInput
import com.example.voicerecording.presentation.viewmodel.ChatViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val textMessage by viewModel.textMessage.collectAsState()
    val recordingState by viewModel.recordingState.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val isLockRecording by viewModel.isLockRecording.collectAsState()
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isRecording = recordingState is com.example.voicerecording.domain.usecase.AudioRecorderUseCase.RecordingState.Recording

    val permissionState = com.google.accompanist.permissions.rememberPermissionState(
        permission = android.Manifest.permission.RECORD_AUDIO
    )

    // Automatically scroll to the bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                // Audio recorder UI (appears when recording)
                AudioRecorderUI(
                    recordingState = recordingState,
                    amplitude = amplitude,
                    isLockRecording = isLockRecording,
                    onStartRecording = { viewModel.startRecording() },
                    onStopRecording = {
                        viewModel.stopRecording()
                        viewModel.toggleLockRecording() // Reset lock
                    },
                    onCancelRecording = { viewModel.cancelRecording() },
                    onToggleLock = { viewModel.toggleLockRecording() },
                    modifier = Modifier.fillMaxWidth()
                )

                // Message input (text + mic)
                MessageInput(
                    text = textMessage,
                    onTextChange = { viewModel.updateTextMessage(it) },
                    onSendMessage = { viewModel.sendTextMessage() },
                    onStartRecording = {
                        if (permissionState.status.isGranted) {
                            viewModel.startRecording()
                        } else {
                            permissionState.launchPermissionRequest()
                        }
                    },
                    isRecording = isRecording,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false // Standard top-to-bottom layout
        ) {
            items(messages) { message -> // No .reversed() here
                if (message.text.isNotEmpty()) {
                    // Show Text Message
                    com.example.voicerecording.presentation.components.TextMessageUI(
                        text = message.text,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Show Audio Message
                    AudioPlayerUI(
                        audioMessage = message,
                        playbackState = playbackState,
                        onPlay = { viewModel.playAudio(message) },
                        onPause = { viewModel.pauseAudio() },
                        onSeek = { /* Implement seek functionality */ },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (messages.isEmpty()) {
                item {
                    Text(
                        text = "No messages yet\nStart a conversation by sending a voice message!",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}