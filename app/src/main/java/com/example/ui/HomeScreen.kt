package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.example.data.WeatherData
import com.example.manager.AppLaunchResult
import com.example.manager.AppManager
import com.example.manager.TargetApp
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBackgroundGradientEnd
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisNeonGlow
import com.example.ui.theme.JarvisStatusGreen
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextDim
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningAmber
import com.example.voice.VoiceManager
import com.example.voice.VoiceState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HomeScreen(
    weatherData: WeatherData,
    voiceManager: VoiceManager,
    appManager: AppManager,
    voiceState: VoiceState,
    transcript: String,
    rmsLevel: Float,
    jarvisReply: String,
    onOpenSettings: () -> Unit,
    onOpenChatGPT: () -> Unit,
    onMicClicked: () -> Unit
) {
    val context = LocalContext.current
    var primaryApps by remember { mutableStateOf<List<TargetApp>>(emptyList()) }
    var installedTvApps by remember { mutableStateOf<List<TargetApp>>(emptyList()) }
    var missingAppDialogData by remember { mutableStateOf<AppLaunchResult.AppNotInstalled?>(null) }

    // Live Clock for Bogota / Colombia (America/Bogota UTC-5)
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        primaryApps = appManager.getPrimaryApps()
        installedTvApps = appManager.getAllInstalledTvApps()

        val bogotaZone = TimeZone.getTimeZone("America/Bogota")
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("es", "CO")).apply {
            timeZone = bogotaZone
        }
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale("es", "CO")).apply {
            timeZone = bogotaZone
        }

        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now).replaceFirstChar { it.uppercase() }
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(JarvisBackground, JarvisBackgroundGradientEnd)
                )
            )
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ================= 1. TOP STATUS & HUD BAR =================
            TopHudBar(
                timeString = currentTimeString,
                dateString = currentDateString,
                weatherData = weatherData,
                onOpenSettings = onOpenSettings
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ================= 2. CENTER STAGE (ARC REACTOR & CONTROLS) =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left HUD: Arc Reactor Core & Telemetry
                Box(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxSize()
                        .background(JarvisCardBg, RoundedCornerShape(16.dp))
                        .tvFocusGlow(isFocused = false, cornerRadius = 16.dp)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ArcReactorCore(
                            size = 175.dp,
                            isActive = voiceState is VoiceState.Listening || voiceState is VoiceState.Responding,
                            rmsLevel = rmsLevel
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "J.A.R.V.I.S. MARK VII",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = JarvisCyan
                                )
                                Text(
                                    text = "ONLINE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JarvisStatusGreen
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(vertical = 6.dp)
                                    .background(Color(0xFF031021), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = when (voiceState) {
                                            is VoiceState.Listening -> "ESTADO: ESCUCHANDO COMANDO VOCAL..."
                                            is VoiceState.Processing -> "ESTADO: PROCESANDO INTENCIÓN..."
                                            is VoiceState.Responding -> "ESTADO: TRANSMITIENDO RESPUESTA..."
                                            is VoiceState.Error -> "ESTADO: ADVERTENCIA DEL SISTEMA"
                                            else -> "ESTADO: EN ESPERA DE COMANDOS"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (voiceState is VoiceState.Error) JarvisAlertRed else JarvisNeonGlow
                                    )

                                    Text(
                                        text = jarvisReply,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JarvisTextPrimary,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Waveform feedback
                            AudioWaveformVisualizer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp),
                                isListening = voiceState is VoiceState.Listening,
                                rmsLevel = rmsLevel
                            )
                        }
                    }
                }

                // Right HUD: Voice Button & ChatGPT Card
                Column(
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Big Mic Button (D-Pad Focusable)
                    FocusableActionCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("voice_assistant_button"),
                        title = if (voiceState is VoiceState.Listening) "ESCUCHANDO..." else "ACTIVAR ASISTENTE VOCAL",
                        subtitle = "Di: 'Hola JARVIS' o presione Centro en el control",
                        badge = if (voiceState is VoiceState.Listening) "MIC ACTIVO" else "VOZ ES-CO",
                        icon = Icons.Default.Mic,
                        accentColor = if (voiceState is VoiceState.Listening) JarvisNeonGlow else JarvisCyan,
                        onClick = onMicClicked
                    )

                    // ChatGPT Official Access Card (D-Pad Focusable)
                    FocusableActionCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("chatgpt_official_button"),
                        title = "ACCESO A CHATGPT OFICIAL",
                        subtitle = "chatgpt.com (Sin API • Seguro)",
                        badge = "WEB OFICIAL",
                        icon = Icons.Default.SmartToy,
                        accentColor = JarvisElectricBlue,
                        onClick = onOpenChatGPT
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ================= 3. BOTTOM APPS CAROUSEL (D-PAD OPTIMIZED) =================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APLICACIONES DEL SISTEMA",
                        style = MaterialTheme.typography.labelLarge,
                        color = JarvisCyan
                    )
                    Text(
                        text = "Navegue con las flechas del control remoto",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisTextDim
                    )
                }

                val allAppsList = remember(primaryApps, installedTvApps) {
                    val primaryIds = primaryApps.map { it.id }.toSet()
                    val otherApps = installedTvApps.filter { it.id !in primaryIds }
                    primaryApps + otherApps
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    items(allAppsList, key = { it.id }) { app ->
                        TvAppItemCard(
                            app = app,
                            onClick = {
                                SoundEffects.playActivateSound()
                                val result = appManager.launchApp(app)
                                if (result is AppLaunchResult.AppNotInstalled) {
                                    SoundEffects.playAlertSound()
                                    missingAppDialogData = result
                                    voiceManager.speakCustom("La aplicación ${app.name} no está instalada en este televisor Hyundai, señor.")
                                } else if (result is AppLaunchResult.Success) {
                                    voiceManager.speakCustom("Iniciando ${app.name}, señor.")
                                }
                            }
                        )
                    }
                }
            }
        }

        // ================= 4. MISSING APP DIALOG =================
        missingAppDialogData?.let { missing ->
            Dialog(onDismissRequest = { missingAppDialogData = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = JarvisSurface,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, JarvisWarningAmber),
                    modifier = Modifier
                        .width(460.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = JarvisWarningAmber,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "APLICACIÓN NO ENCONTRADA",
                                style = MaterialTheme.typography.titleLarge,
                                color = JarvisTextPrimary
                            )
                        }

                        Text(
                            text = "La aplicación '${missing.appName}' no está instalada actualmente en su Google TV Hyundai.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = JarvisTextSecondary
                        )

                        Text(
                            text = "¿Desea buscarla y descargarla ahora desde Google Play Store?",
                            style = MaterialTheme.typography.bodySmall,
                            color = JarvisTextDim
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { missingAppDialogData = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF132F4C)
                                )
                            ) {
                                Text("Cancelar", color = JarvisTextSecondary)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = {
                                    missingAppDialogData = null
                                    appManager.openInPlayStore(missing.targetPackage)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JarvisCyan
                                )
                            ) {
                                Text("Abrir Play Store", color = Color(0xFF001F29), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top HUD Bar with J.A.R.V.I.S. brand, Colombia live clock, weather telemetry, and settings trigger
 */
@Composable
private fun TopHudBar(
    timeString: String,
    dateString: String,
    weatherData: WeatherData,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(JarvisCardBg, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & OS Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(JarvisCyan),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF001F29))
                )
            }

            Column {
                Text(
                    text = "J.A.R.V.I.S. HOME",
                    style = MaterialTheme.typography.titleLarge,
                    color = JarvisCyan
                )
                Text(
                    text = "HYUNDAI GOOGLE TV 14 • HYUNDAI AI HUD",
                    style = MaterialTheme.typography.labelSmall,
                    color = JarvisTextDim
                )
            }
        }

        // Center: Bogota Colombia Live Clock
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeString.ifEmpty { "12:00:00" },
                style = MaterialTheme.typography.displayMedium,
                color = JarvisTextPrimary
            )
            Text(
                text = dateString.ifEmpty { "Colombia" },
                style = MaterialTheme.typography.labelSmall,
                color = JarvisNeonGlow
            )
        }

        // Right: Weather & Settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Weather Mini Card
            Row(
                modifier = Modifier
                    .background(Color(0xFF061A2F), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (weatherData.conditionIcon) {
                        "sunny" -> Icons.Default.WbSunny
                        "rain", "rain_light", "thunderstorm" -> Icons.Default.WaterDrop
                        else -> Icons.Default.Cloud
                    },
                    contentDescription = weatherData.conditionText,
                    tint = JarvisCyan,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = "${weatherData.temperatureC}°C • ${weatherData.cityName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = JarvisTextPrimary
                    )
                    Text(
                        text = "${weatherData.conditionText} | Hum: ${weatherData.humidityPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisTextDim
                    )
                }
            }

            // Settings Button (D-Pad Focusable)
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()

            LaunchedEffect(isFocused) {
                if (isFocused) SoundEffects.playFocusSound()
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isFocused) JarvisCyan else Color(0xFF0C2A48))
                    .tvFocusGlow(isFocused, cornerRadius = 8.dp)
                    .focusable(interactionSource = interactionSource)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            SoundEffects.playActivateSound()
                            onOpenSettings()
                        }
                    )
                    .testTag("settings_top_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración",
                    tint = if (isFocused) Color(0xFF001F29) else JarvisCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Focusable Action Card (Voice Assistant button & ChatGPT button)
 */
@Composable
private fun FocusableActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (isFocused) SoundEffects.playFocusSound()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) Color(0xFF0A2949) else JarvisCardBg)
            .tvFocusGlow(isFocused, cornerRadius = 14.dp)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    SoundEffects.playActivateSound()
                    onClick()
                }
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = if (isFocused) 0.35f else 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = JarvisTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * TV App Item Card with D-Pad focus scaling, glowing border, and remote click sound
 */
@Composable
private fun TvAppItemCard(
    app: TargetApp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (isFocused) SoundEffects.playFocusSound()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(115.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) Color(0xFF0F365E) else JarvisCardBg)
            .tvFocusGlow(isFocused, cornerRadius = 12.dp)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .testTag("app_item_${app.id}")
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF031021)),
            contentAlignment = Alignment.Center
        ) {
            if (app.appIcon != null) {
                val bitmap = remember(app.appIcon) {
                    app.appIcon.toBitmap(96, 96).asImageBitmap()
                }
                Image(
                    bitmap = bitmap,
                    contentDescription = app.name,
                    modifier = Modifier.size(44.dp)
                )
            } else {
                Icon(
                    imageVector = when (app.id) {
                        "youtube" -> Icons.Default.PlayArrow
                        "chatgpt" -> Icons.Default.SmartToy
                        else -> Icons.Default.Tv
                    },
                    contentDescription = app.name,
                    tint = JarvisCyan,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = app.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isFocused) JarvisCyan else JarvisTextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = if (app.isInstalled) "INSTALADO" else "DISPONIBLE",
            style = MaterialTheme.typography.labelSmall,
            color = if (app.isInstalled) JarvisStatusGreen else JarvisTextDim,
            fontSize = 8.sp,
            textAlign = TextAlign.Center
        )
    }
}
