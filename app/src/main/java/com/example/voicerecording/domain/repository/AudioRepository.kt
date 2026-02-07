package com.example.voicerecording.domain.repository


import com.example.voicerecording.data.model.AudioMessage
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    fun getAllMessages(): Flow<List<AudioMessage>>
    suspend fun insertMessage(message: AudioMessage)
    suspend fun updateMessage(message: AudioMessage)
    suspend fun deleteMessage(id: Long)
    suspend fun getMessageById(id: Long): AudioMessage?
}