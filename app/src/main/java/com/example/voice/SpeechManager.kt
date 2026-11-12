package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "JarvisSpeech"

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtterance = MutableStateFlow("")
    val currentUtterance: StateFlow<String> = _currentUtterance.asStateFlow()

    var speechRate: Float = 1.0f
        set(value) {
            field = value
            tts?.setSpeechRate(value)
        }

    var speechPitch: Float = 0.92f // Slightly deeper Jarvis pitch
        set(value) {
            field = value
            tts?.setPitch(value)
        }

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupVoiceAndLanguage()
            setupProgressListener()
        } else {
            Log.e(TAG, "TTS Initialization failed with status: $status")
        }
    }

    private fun setupVoiceAndLanguage() {
        val ttsInstance = tts ?: return

        // 1. Set language: try Colombia es-CO, then Latin America es-419, then Spanish
        val colombianLocale = Locale("es", "CO")
        val latinLocale = Locale("es", "419")
        val generalSpanish = Locale("es")

        val langResult = when {
            ttsInstance.isLanguageAvailable(colombianLocale) >= TextToSpeech.LANG_AVAILABLE -> {
                ttsInstance.language = colombianLocale
                "es-CO (Colombia)"
            }
            ttsInstance.isLanguageAvailable(latinLocale) >= TextToSpeech.LANG_AVAILABLE -> {
                ttsInstance.language = latinLocale
                "es-419 (Latinoamérica)"
            }
            else -> {
                ttsInstance.language = generalSpanish
                "es (Español)"
            }
        }
        Log.i(TAG, "TTS Language selected: $langResult")

        // 2. Select masculine voice if available in engine
        try {
            val voices = ttsInstance.voices
            if (voices != null) {
                var selectedVoice: Voice? = null
                for (voice in voices) {
                    val lang = voice.locale.language
                    val name = voice.name.lowercase()
                    if (lang == "es") {
                        // Look for masculine traits
                        if (name.contains("male") || name.contains("masc") || name.contains("h-") || name.contains("hombre") || name.contains("sfg")) {
                            selectedVoice = voice
                            break
                        }
                    }
                }
                if (selectedVoice != null) {
                    ttsInstance.voice = selectedVoice
                    Log.i(TAG, "Masculine Spanish voice selected: ${selectedVoice.name}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Voice selection fallback: ${e.message}")
        }

        ttsInstance.setPitch(speechPitch)
        ttsInstance.setSpeechRate(speechRate)
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                Log.w(TAG, "TTS Utterance error $errorCode on id $utteranceId")
            }
        })
    }

    /**
     * Speaks the given text using J.A.R.V.I.S. voice
     */
    fun speak(text: String, utteranceId: String = System.currentTimeMillis().toString()) {
        if (!isInitialized || text.isBlank()) return
        _currentUtterance.value = text
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
