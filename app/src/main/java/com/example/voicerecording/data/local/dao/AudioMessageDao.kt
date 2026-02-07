package com.example.voicerecording.data.local.dao


import androidx.room.*
import com.example.voicerecording.data.model.AudioMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioMessageDao {

    @Query("SELECT * FROM audio_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<AudioMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AudioMessage)

    @Update
    suspend fun updateMessage(message: AudioMessage)

    @Query("DELETE FROM audio_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("SELECT * FROM audio_messages WHERE id = :id")
    suspend fun getMessageById(id: Long): AudioMessage?
}