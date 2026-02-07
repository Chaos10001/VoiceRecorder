package com.example.voicerecording.di

import android.content.Context
import com.example.voicerecording.data.local.AppDatabase
import com.example.voicerecording.data.local.dao.AudioMessageDao
import com.example.voicerecording.data.repository.AudioRepositoryImpl
import com.example.voicerecording.domain.repository.AudioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideAudioMessageDao(database: AppDatabase): AudioMessageDao {
        return database.audioMessageDao()
    }

    @Provides
    @Singleton
    fun provideAudioRepository(audioMessageDao: AudioMessageDao): AudioRepository {
        return AudioRepositoryImpl(audioMessageDao)
    }
}