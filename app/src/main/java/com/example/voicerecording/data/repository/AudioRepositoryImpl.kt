package com.example.voicerecording.data.repository


import com.example.voicerecording.data.local.dao.AudioMessageDao
import com.example.voicerecording.data.model.AudioMessage
import com.example.voicerecording.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(
    private val audioMessageDao: AudioMessageDao
) : AudioRepository {

    override fun getAllMessages(): Flow<List<AudioMessage>> {
        return audioMessageDao.getAllMessages()
    }

    override suspend fun insertMessage(message: AudioMessage) {
        audioMessageDao.insertMessage(message)
    }

    override suspend fun updateMessage(message: AudioMessage) {
        audioMessageDao.updateMessage(message)
    }

    override suspend fun deleteMessage(id: Long) {
        audioMessageDao.deleteMessage(id)
    }

    override suspend fun getMessageById(id: Long): AudioMessage? {
        return audioMessageDao.getMessageById(id)
    }
}