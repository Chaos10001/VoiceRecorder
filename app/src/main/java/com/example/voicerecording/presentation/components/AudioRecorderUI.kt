package com.example.voicerecording.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicerecording.domain.usecase.AudioRecorderUseCase
import java.util.concurrent.TimeUnit

@Composable
fun AudioRecorderUI(
    modifier: Modifier = Modifier,
    recordingState: AudioRecorderUseCase.RecordingState,
    amplitude: Float,
    isLockRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onToggleLock: () -> Unit
) {
    var dragOffset by remember { mutableStateOf(0f) }
    val maxDragOffset = with(LocalDensity.current) { 80.dp.toPx() }
    val isRecording = recordingState is AudioRecorderUseCase.RecordingState.Recording

    // Animate height of the recorder panel
    val height by animateDpAsState(
        targetValue = if (isRecording) 240.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "height"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp
    ) {
        if (isRecording) {
            val recordingDuration = when (recordingState) {
                is AudioRecorderUseCase.RecordingState.Recording -> recordingState.duration
                else -> 0L
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Top Header: Status and Timer
                Column(
                    modifier = Modifier.align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isLockRecording) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            text = if (isLockRecording) "LOCKED" else "RECORDING",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = if (isLockRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = formatDuration(recordingDuration),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Waveform visualization
                RecordingWaveform(
                    amplitude = amplitude,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 8.dp)
                )

                // Lock Slide Indicator (only visible when not locked)
                if (!isLockRecording) {
                    val lockAlpha by animateFloatAsState(
                        targetValue = if (dragOffset < -20) 1f else 0.4f,
                        label = "lockAlpha"
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .offset(y = (dragOffset / 5).dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = lockAlpha)
                        )
                        Text(
                            text = "Slide up",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = lockAlpha)
                        )
                    }
                }

                // Bottom Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button
                    FilledTonalIconButton(
                        onClick = onCancelRecording,
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel recording")
                    }

                    // Main Recording Mic / Lock Icon with Gestures
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { /* Recording logic handled by parent */ },
                                    onDrag = { change, dragAmount ->
                                        dragOffset += dragAmount.y
                                        if (dragOffset < -maxDragOffset && !isLockRecording) {
                                            onToggleLock()
                                        }
                                        if (dragOffset > maxDragOffset && isLockRecording) {
                                            onToggleLock()
                                        }
                                    },
                                    onDragEnd = {
                                        if (!isLockRecording && dragOffset > -50) {
                                            onStopRecording()
                                        }
                                        dragOffset = 0f
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Pulsing background animation for Mic
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseScale"
                        )
                        
                        if (!isLockRecording) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .scale(pulseScale)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), CircleShape)
                            )
                        }

                        val bgColor by animateColorAsState(
                            targetValue = if (isLockRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            label = "bgColor"
                        )

                        Surface(
                            shape = CircleShape,
                            color = bgColor,
                            modifier = Modifier.size(64.dp),
                            shadowElevation = 6.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isLockRecording) Icons.Default.Lock else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Send Button - ALWAYS VISIBLE
                    FilledIconButton(
                        onClick = onStopRecording,
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send recording")
                    }
                }
            }
        }
    }
}

@Composable
fun RecordingWaveform(
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val bars = remember { mutableStateListOf<Float>() }
    val maxBars = 50 // Reduced for a cleaner, modern look

    val barColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(amplitude) {
        bars.add(amplitude)
        if (bars.size > maxBars) {
            bars.removeAt(0)
        }
    }

    Canvas(modifier = modifier) {
        val spacing = 4.dp.toPx()
        val barWidth = (size.width - (maxBars - 1) * spacing) / maxBars
        val centerY = size.height / 2

        bars.forEachIndexed { index, amp ->
            // Scale amplitude to height and ensure a minimum visibility
            val barHeight = (amp / 100f).coerceIn(0.1f, 1f) * size.height
            val x = index * (barWidth + spacing)

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, centerY - barHeight / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
                alpha = if (index == bars.lastIndex) 1f else 0.6f
            )
        }
    }
}

fun formatDuration(milliseconds: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
            TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}