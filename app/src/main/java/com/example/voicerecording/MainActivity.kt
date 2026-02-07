package com.example.voicerecording

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.voicerecording.presentation.screen.ChatScreen
import com.example.voicerecording.presentation.theme.VoiceRecordingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoiceRecordingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ChatScreen()
                }
            }
        }
    }
}