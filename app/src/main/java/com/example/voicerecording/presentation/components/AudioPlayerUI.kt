package com.example.voicerecording.presentation.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.voicerecording.data.model.AudioMessage
import com.example.voicerecording.domain.usecase.AudioPlayerUseCase
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun AudioPlayerUI(
    audioMessage: AudioMessage,
    playbackState: AudioPlayerUseCase.PlaybackState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeek: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentActive = when (playbackState) {
        is AudioPlayerUseCase.PlaybackState.Loading -> playbackState.filePath == audioMessage.audioPath
        is AudioPlayerUseCase.PlaybackState.Playing -> playbackState.filePath == audioMessage.audioPath
        is AudioPlayerUseCase.PlaybackState.Paused -> playbackState.filePath == audioMessage.audioPath
        is AudioPlayerUseCase.PlaybackState.Finished -> playbackState.filePath == audioMessage.audioPath
        is AudioPlayerUseCase.PlaybackState.Error -> playbackState.filePath == audioMessage.audioPath
        else -> false
    }

    val isPlaying = isCurrentActive && playbackState is AudioPlayerUseCase.PlaybackState.Playing
    val isLoading = isCurrentActive && playbackState is AudioPlayerUseCase.PlaybackState.Loading

    val currentPosition = if (isCurrentActive) {
        when (playbackState) {
            is AudioPlayerUseCase.PlaybackState.Playing -> playbackState.currentPosition
            is AudioPlayerUseCase.PlaybackState.Paused -> playbackState.currentPosition
            else -> 0
        }
    } else {
        0
    }

    val duration = audioMessage.duration.toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Play/Pause button
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                onPause()
                            } else {
                                onPlay()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Waveform/Progress visualization
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .padding(horizontal = 16.dp)
            ) {
                // Progress bar
                LinearProgressIndicator(
                    progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )

                // Time indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(currentPosition.toLong()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = formatDuration(audioMessage.duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}