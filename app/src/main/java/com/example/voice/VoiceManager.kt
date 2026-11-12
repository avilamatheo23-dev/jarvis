package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.ui.SoundEffects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    object Processing : VoiceState()
    data class Responding(val speechText: String) : VoiceState()
    data class Error(val message: String) : VoiceState()
}

sealed class JarvisCommandAction {
    data class LaunchApp(val appId: String) : JarvisCommandAction()
    object OpenChatGPT : JarvisCommandAction()
    object OpenSettings : JarvisCommandAction()
    object CheckWeather : JarvisCommandAction()
    object CheckTime : JarvisCommandAction()
    object CheckSystemStatus : JarvisCommandAction()
    object StopSpeaking : JarvisCommandAction()
    data class SpeakOnly(val response: String) : JarvisCommandAction()
}

class VoiceManager(
    private val context: Context,
    private val speechManager: SpeechManager,
    private val onCommandAction: (JarvisCommandAction) -> Unit
) {
    private val TAG = "JarvisVoiceManager"

    private var speechRecognizer: SpeechRecognizer? = null

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _lastJarvisReply = MutableStateFlow("En espera de instrucciones, señor.")
    val lastJarvisReply: StateFlow<String> = _lastJarvisReply.asStateFlow()

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w(TAG, "SpeechRecognizer is not available on this device")
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = VoiceState.Listening
                        SoundEffects.playActivateSound()
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = VoiceState.Listening
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Map rms to 0..10 range for visualizers
                        _rmsLevel.value = ((rmsdB + 2f) / 1.2f).coerceIn(0f, 10f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = VoiceState.Processing
                    }

                    override fun onError(error: Int) {
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No se detectó comando vocal."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Error de conexión de red para voz."
                            SpeechRecognizer.ERROR_AUDIO -> "Error al acceder al micrófono."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permiso de micrófono no concedido."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tiempo de espera agotado."
                            else -> "Reconocimiento no disponible ($error)."
                        }
                        Log.w(TAG, "Speech error: $errorMsg ($error)")
                        _voiceState.value = VoiceState.Error(errorMsg)
                        _rmsLevel.value = 0f
                        SoundEffects.playAlertSound()
                    }

                    override fun onResults(results: Bundle?) {
                        _voiceState.value = VoiceState.Processing
                        _rmsLevel.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _transcript.value = text
                        processSpokenCommand(text)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            _transcript.value = it
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SpeechRecognizer: ${e.message}")
        }
    }

    /**
     * Activates microphone listening
     */
    fun startListening() {
        if (speechRecognizer == null) {
            initRecognizer()
        }

        speechManager.stop()
        _transcript.value = "Escuchando... hable ahora"
        _voiceState.value = VoiceState.Listening

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "startListening error: ${e.message}")
            _voiceState.value = VoiceState.Error("No se pudo iniciar el micrófono.")
            SoundEffects.playAlertSound()
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _voiceState.value = VoiceState.Idle
        _rmsLevel.value = 0f
    }

    /**
     * Parses spoken Colombian Spanish commands and responds with voice + actions
     */
    fun processSpokenCommand(rawText: String) {
        val query = rawText.lowercase(Locale.ROOT).trim()
        Log.i(TAG, "Processing voice query: '$query'")

        val (reply, action) = when {
            query.contains("hola jarvis") || query == "jarvis" || query == "hola" -> {
                "Buenas tardes, señor. Todos los sistemas están en línea y a su completa disposición." to
                        JarvisCommandAction.SpeakOnly("Buenas tardes, señor. Todos los sistemas están en línea y a su completa disposición.")
            }
            query.contains("youtube") -> {
                "Iniciando YouTube en pantalla principal, señor." to
                        JarvisCommandAction.LaunchApp("youtube")
            }
            query.contains("netflix") -> {
                "Accediendo a Netflix. Que disfrute su contenido, señor." to
                        JarvisCommandAction.LaunchApp("netflix")
            }
            query.contains("prime") || query.contains("amazon") -> {
                "Abriendo Prime Video, señor." to
                        JarvisCommandAction.LaunchApp("prime_video")
            }
            query.contains("chatgpt") || query.contains("inteligencia artificial") -> {
                "Abriendo el portal oficial de ChatGPT en pantalla completa, señor." to
                        JarvisCommandAction.OpenChatGPT
            }
            query.contains("play store") || query.contains("tienda") || query.contains("google play") -> {
                "Cargando la tienda Google Play Store, señor." to
                        JarvisCommandAction.LaunchApp("play_store")
            }
            query.contains("clima") || query.contains("tiempo") || query.contains("temperatura") || query.contains("lluvia") -> {
                "Consultando las condiciones meteorológicas actuales, señor." to
                        JarvisCommandAction.CheckWeather
            }
            query.contains("hora") || query.contains("fecha") || query.contains("día") -> {
                "Verificando cronómetro del sistema en horario de Colombia." to
                        JarvisCommandAction.CheckTime
            }
            query.contains("estado") || query.contains("diagnóstico") || query.contains("sistema") || query.contains("reporte") -> {
                "Diagnóstico completo: Núcleo Ark al 100%, temperatura operativa normal y conexión a la red de Hyundai TV establecida." to
                        JarvisCommandAction.CheckSystemStatus
            }
            query.contains("configuración") || query.contains("ajustes") || query.contains("opciones") -> {
                "Accediendo al panel de configuración del sistema." to
                        JarvisCommandAction.OpenSettings
            }
            query.contains("silencio") || query.contains("detener") || query.contains("cancela") -> {
                speechManager.stop()
                "En silencio, señor." to JarvisCommandAction.StopSpeaking
            }
            else -> {
                "He registrado su solicitud: $rawText. Si desea una consulta compleja o redacción, puedo abrir ChatGPT oficial para usted." to
                        JarvisCommandAction.SpeakOnly("He registrado su solicitud. Si requiere una consulta compleja, puedo abrir ChatGPT oficial.")
            }
        }

        _lastJarvisReply.value = reply
        _voiceState.value = VoiceState.Responding(reply)
        speechManager.speak(reply)
        onCommandAction(action)
    }

    fun speakCustom(text: String) {
        _lastJarvisReply.value = text
        _voiceState.value = VoiceState.Responding(text)
        speechManager.speak(text)
    }

    fun dismissError() {
        _voiceState.value = VoiceState.Idle
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
    }
}
