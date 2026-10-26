package com.example.core.voice

sealed class VoiceEngineState {
    data object Idle : VoiceEngineState()
    data object Listening : VoiceEngineState()
    data object Processing : VoiceEngineState()
    data class Speaking(val text: String) : VoiceEngineState()
    data class Error(val message: String) : VoiceEngineState()
}
