package com.example.voicerecording.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "audio_messages")
data class AudioMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val audioPath: String,
    val duration: Long,
    val timestamp: Date = Date(),
    val isSent: Boolean = false,
    val isPlaying: Boolean = false,
    val text: String = ""
)