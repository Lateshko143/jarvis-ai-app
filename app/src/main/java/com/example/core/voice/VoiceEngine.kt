package com.example.core.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceEngine(
    private val context: Context,
    private val onCommandRecognized: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private val _engineState = MutableStateFlow<VoiceEngineState>(VoiceEngineState.Idle)
    val engineState: StateFlow<VoiceEngineState> = _engineState.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        initSpeechRecognizer()
        tts = TextToSpeech(context.applicationContext, this)
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceEngine)
                }
            } catch (e: Exception) {
                _engineState.value = VoiceEngineState.Error("Speech recognizer error: ${e.message}")
            }
        } else {
            _engineState.value = VoiceEngineState.Error("Speech recognition is not available on this device.")
        }
    }

    fun startListening() {
        stopSpeaking()
        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-US"))
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        try {
            speechRecognizer?.startListening(intent)
            _engineState.value = VoiceEngineState.Listening
        } catch (e: Exception) {
            _engineState.value = VoiceEngineState.Error("Failed to start voice input: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignore if already stopped
        }
        if (_engineState.value is VoiceEngineState.Listening) {
            _engineState.value = VoiceEngineState.Idle
        }
        _audioRms.value = 0f
    }

    fun speak(text: String, onComplete: () -> Unit = {}) {
        if (!isTtsReady) {
            _engineState.value = VoiceEngineState.Error("Speech synthesizer is warming up.")
            return
        }

        stopListening()
        _engineState.value = VoiceEngineState.Speaking(text)

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                _engineState.value = VoiceEngineState.Idle
                _audioRms.value = 0f
                onComplete()
            }
            override fun onError(utteranceId: String?) {
                _engineState.value = VoiceEngineState.Error("TTS playback encounter.")
                _audioRms.value = 0f
            }
        })

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "JARVIS_RESPONSE")
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "JARVIS_RESPONSE")
    }

    fun stopSpeaking() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
            _engineState.value = VoiceEngineState.Idle
            _audioRms.value = 0f
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("en", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            tts?.setPitch(0.95f)
            tts?.setSpeechRate(1.05f)
            isTtsReady = true
        } else {
            _engineState.value = VoiceEngineState.Error("TTS initialization failed with code $status")
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val spokenText = matches[0]
            _engineState.value = VoiceEngineState.Processing
            _audioRms.value = 0f
            onCommandRecognized(spokenText)
        } else {
            _engineState.value = VoiceEngineState.Idle
            _audioRms.value = 0f
        }
    }

    override fun onError(error: Int) {
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No voice match detected. Say 'Jarvis' or tap command."
            SpeechRecognizer.ERROR_NETWORK -> "Network issue for cloud speech recognition."
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording failure."
            SpeechRecognizer.ERROR_CLIENT -> "Speech recognition cancelled."
            else -> "Speech recognition code: $error"
        }
        _audioRms.value = 0f
        _engineState.value = VoiceEngineState.Error(errorMsg)
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {
        // Normalize rmsdB (-2 to 10 typical) to 0.0 - 1.0 range
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _audioRms.value = normalized
    }
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            tts?.shutdown()
        } catch (e: Exception) {
            // Safe teardown
        }
    }
}
