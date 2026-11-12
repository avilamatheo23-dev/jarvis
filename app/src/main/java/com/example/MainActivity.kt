package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.WeatherRepository
import com.example.manager.AppLaunchResult
import com.example.manager.AppManager
import com.example.manager.ChatGPTLauncher
import com.example.ui.HomeScreen
import com.example.ui.SettingsScreen
import com.example.ui.SoundEffects
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisTheme
import com.example.voice.JarvisCommandAction
import com.example.voice.SpeechManager
import com.example.voice.VoiceManager
import com.example.voice.VoiceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class CurrentScreen {
    HOME,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var appManager: AppManager
    private lateinit var speechManager: SpeechManager
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var voiceManager: VoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appManager = AppManager(this)
        speechManager = SpeechManager(this)
        weatherRepository = WeatherRepository()

        voiceManager = VoiceManager(
            context = this,
            speechManager = speechManager,
            onCommandAction = { action ->
                handleVoiceCommandAction(action)
            }
        )

        setContent {
            JarvisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = JarvisBackground
                ) {
                    JarvisAppContent(
                        weatherRepo = weatherRepository,
                        voiceManager = voiceManager,
                        speechManager = speechManager,
                        appManager = appManager,
                        onRequestMic = {
                            requestMicAndListen()
                        }
                    )
                }
            }
        }
    }

    private var micPermissionGrantedCallback: (() -> Unit)? = null

    private fun requestMicAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            voiceManager.startListening()
        } else {
            micPermissionGrantedCallback = {
                voiceManager.startListening()
            }
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                micPermissionGrantedCallback?.invoke()
            } else {
                voiceManager.speakCustom("Permiso de micrófono requerido para comandos de voz en su televisor Hyundai.")
            }
            micPermissionGrantedCallback = null
        }
    }

    private fun handleVoiceCommandAction(action: JarvisCommandAction) {
        when (action) {
            is JarvisCommandAction.LaunchApp -> {
                val primaryApps = appManager.getPrimaryApps()
                val target = primaryApps.find { it.id == action.appId }
                if (target != null) {
                    val result = appManager.launchApp(target)
                    if (result is AppLaunchResult.AppNotInstalled) {
                        voiceManager.speakCustom("La aplicación ${target.name} no está instalada en este televisor.")
                    }
                }
            }
            is JarvisCommandAction.OpenChatGPT -> {
                ChatGPTLauncher.launchChatGPT(this)
            }
            is JarvisCommandAction.CheckWeather -> {
                val weather = weatherRepository.weatherState.value
                val text = "En ${weather.cityName}, la temperatura es de ${weather.temperatureC} grados centígrados, con ${weather.conditionText} y humedad del ${weather.humidityPercent}%."
                voiceManager.speakCustom(text)
            }
            is JarvisCommandAction.CheckTime -> {
                val bogotaZone = TimeZone.getTimeZone("America/Bogota")
                val timeFormat = SimpleDateFormat("h y m a", Locale("es", "CO")).apply {
                    timeZone = bogotaZone
                }
                val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "CO")).apply {
                    timeZone = bogotaZone
                }
                val now = Date()
                val timeStr = timeFormat.format(now)
                val dateStr = dateFormat.format(now)
                voiceManager.speakCustom("Son las $timeStr en Colombia, $dateStr.")
            }
            is JarvisCommandAction.CheckSystemStatus -> {
                voiceManager.speakCustom("Todos los subsistemas de Hyundai Google TV 14 responden con normalidad. Interfaz Mark VII operativa.")
            }
            is JarvisCommandAction.OpenSettings -> {
                // Handled in Compose state if triggered
            }
            is JarvisCommandAction.StopSpeaking -> {
                speechManager.stop()
            }
            is JarvisCommandAction.SpeakOnly -> {
                // already spoken by VoiceManager
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Handle physical remote control microphone / voice keys
        if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_VOICE_ASSIST) {
            requestMicAndListen()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.destroy()
        speechManager.shutdown()
    }
}

@Composable
fun JarvisAppContent(
    weatherRepo: WeatherRepository,
    voiceManager: VoiceManager,
    speechManager: SpeechManager,
    appManager: AppManager,
    onRequestMic: () -> Unit
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(CurrentScreen.HOME) }
    val weatherData by weatherRepo.weatherState.collectAsState()
    val voiceState by voiceManager.voiceState.collectAsState()
    val transcript by voiceManager.transcript.collectAsState()
    val rmsLevel by voiceManager.rmsLevel.collectAsState()
    val jarvisReply by voiceManager.lastJarvisReply.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Audio permission launcher inside Compose
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceManager.startListening()
        } else {
            voiceManager.speakCustom("Permiso de micrófono necesario para escuchar comandos.")
        }
    }

    LaunchedEffect(Unit) {
        weatherRepo.fetchWeather()
        // Welcome greeting
        SoundEffects.playActivateSound()
        delay(1200)
        speechManager.speak("Buenas tardes, señor. Sistemas de J.A.R.V.I.S. Home iniciados en su televisor Hyundai.")
    }

    when (currentScreen) {
        CurrentScreen.HOME -> {
            HomeScreen(
                weatherData = weatherData,
                voiceManager = voiceManager,
                appManager = appManager,
                voiceState = voiceState,
                transcript = transcript,
                rmsLevel = rmsLevel,
                jarvisReply = jarvisReply,
                onOpenSettings = {
                    currentScreen = CurrentScreen.SETTINGS
                },
                onOpenChatGPT = {
                    ChatGPTLauncher.launchChatGPT(context)
                },
                onMicClicked = {
                    onRequestMic()
                }
            )
        }
        CurrentScreen.SETTINGS -> {
            SettingsScreen(
                weatherRepo = weatherRepo,
                speechManager = speechManager,
                onBack = {
                    currentScreen = CurrentScreen.HOME
                }
            )
        }
    }
}
