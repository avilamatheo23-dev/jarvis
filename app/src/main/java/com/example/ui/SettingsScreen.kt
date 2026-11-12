package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CityLocation
import com.example.data.ColombianCities
import com.example.data.WeatherRepository
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBackgroundGradientEnd
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisNeonGlow
import com.example.ui.theme.JarvisStatusGreen
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextDim
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.voice.SpeechManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    weatherRepo: WeatherRepository,
    speechManager: SpeechManager,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val coroutineScope = rememberCoroutineScope()
    var selectedCity by remember { mutableStateOf(weatherRepo.getSelectedCity()) }
    var soundEnabled by remember { mutableStateOf(SoundEffects.isEnabled) }
    var currentPitch by remember { mutableFloatStateOf(speechManager.speechPitch) }
    var currentRate by remember { mutableFloatStateOf(speechManager.speechRate) }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JarvisCardBg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val backInteraction = remember { MutableInteractionSource() }
                val isBackFocused by backInteraction.collectIsFocusedAsState()

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBackFocused) JarvisCyan else Color(0xFF0C2A48))
                        .tvFocusGlow(isBackFocused, cornerRadius = 8.dp)
                        .focusable(interactionSource = backInteraction)
                        .clickable(
                            interactionSource = backInteraction,
                            indication = null,
                            onClick = {
                                SoundEffects.playActivateSound()
                                onBack()
                            }
                        )
                        .testTag("settings_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar al inicio",
                        tint = if (isBackFocused) Color(0xFF001F29) else JarvisCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "PANEL DE CONFIGURACIÓN DEL SISTEMA",
                        style = MaterialTheme.typography.titleLarge,
                        color = JarvisCyan
                    )
                    Text(
                        text = "J.A.R.V.I.S. HOME • AJUSTES PARA HYUNDAI GOOGLE TV 14",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisTextDim
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Weather City Config
                item {
                    SettingsCard(
                        title = "1. CIUDAD METEOROLÓGICA (COLOMBIA)",
                        icon = Icons.Default.Cloud
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Seleccione la ciudad para el pronóstico y zona horaria (UTC-5):",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextSecondary
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(ColombianCities.list) { city ->
                                    val isSelected = city.name == selectedCity.name
                                    val interaction = remember { MutableInteractionSource() }
                                    val isFocused by interaction.collectIsFocusedAsState()

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isSelected -> JarvisCyan
                                                    isFocused -> Color(0xFF133F69)
                                                    else -> Color(0xFF061A2F)
                                                }
                                            )
                                            .tvFocusGlow(isFocused, cornerRadius = 8.dp)
                                            .focusable(interactionSource = interaction)
                                            .clickable(
                                                interactionSource = interaction,
                                                indication = null,
                                                onClick = {
                                                    selectedCity = city
                                                    SoundEffects.playActivateSound()
                                                    coroutineScope.launch {
                                                        weatherRepo.setCity(city)
                                                    }
                                                }
                                            )
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "${city.name} (${city.department})",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSelected) Color(0xFF001F29) else JarvisTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Voice & Audio Tuning
                item {
                    SettingsCard(
                        title = "2. PARÁMETROS DE VOZ Y AUDIO (J.A.R.V.I.S.)",
                        icon = Icons.Default.RecordVoiceOver
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Efectos de sonido de la interfaz (Beeps Sci-Fi)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JarvisTextPrimary
                                    )
                                    Text(
                                        text = "Retroalimentación sonora al navegar con el control remoto",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JarvisTextDim
                                    )
                                }
                                Switch(
                                    checked = soundEnabled,
                                    onCheckedChange = {
                                        soundEnabled = it
                                        SoundEffects.isEnabled = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = JarvisCyan,
                                        checkedTrackColor = Color(0xFF004D61)
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        SoundEffects.playActivateSound()
                                        speechManager.speak("Sistemas vocales calibrados, señor. J.A.R.V.I.S. funcionando con voz masculina en español latinoamericano.")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = Color(0xFF001F29),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Probar Voz de J.A.R.V.I.S.", color = Color(0xFF001F29), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Section 3: Google TV 14 Home Launcher Setup
                item {
                    SettingsCard(
                        title = "3. CÓMO CONFIGURAR O REVERTIR COMO LAUNCHER PREDETERMINADO",
                        icon = Icons.Default.Home
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Instrucciones para Hyundai Google TV 14:",
                                style = MaterialTheme.typography.labelLarge,
                                color = JarvisNeonGlow
                            )
                            Text(
                                text = "• Para activar como Launcher principal: Al presionar el botón HOME del control remoto de su Hyundai, seleccione 'J.A.R.V.I.S. HOME' y marque 'Siempre'. O vaya a: Ajustes > Aplicaciones > Acceso especial de apps > App de inicio predeterminada > J.A.R.V.I.S. HOME.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextPrimary
                            )
                            Text(
                                text = "• Para regresar al launcher original de Google TV: Vaya a Ajustes de Android TV > Aplicaciones > Acceso especial > App de inicio predeterminada > Seleccionar 'Google TV Launcher' o 'Android TV Home'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextSecondary
                            )
                            Text(
                                text = "• Seguridad garantizada: J.A.R.V.I.S. HOME no elimina ni modifica los archivos del sistema ni Google TV.",
                                style = MaterialTheme.typography.labelSmall,
                                color = JarvisStatusGreen
                            )
                        }
                    }
                }

                // Section 4: Transfer from Samsung Galaxy A35
                item {
                    SettingsCard(
                        title = "4. TRANSFERENCIA DESDE SAMSUNG GALAXY A35",
                        icon = Icons.Default.PhoneAndroid
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Métodos recomendados para pasar el APK a su televisor Hyundai:",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextPrimary
                            )
                            Text(
                                text = "1. Con la App 'Send Files to TV' (Fácil): Instale 'Send Files to TV' en su Samsung A35 y en la TV Hyundai desde Play Store. Envíe el APK y ábralo en la TV con un gestor como 'File Commander'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextSecondary
                            )
                            Text(
                                text = "2. Vía Memoria USB / OTG: Guarde el APK en una memoria USB desde su Galaxy A35 o PC, conéctela al puerto USB del televisor Hyundai e instálelo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextSecondary
                            )
                            Text(
                                text = "3. Vía ADB Inalámbrico: En Google TV 14 active Opciones de Desarrollador > Depuración inalámbrica, y conecte desde su móvil.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextSecondary
                            )
                        }
                    }
                }

                // Section 5: Direct Download from TV Browser
                item {
                    SettingsCard(
                        title = "5. DESCARGA E INSTALACIÓN DESDE EL NAVEGADOR DE LA TV",
                        icon = Icons.Default.Download
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Cómo descargar e instalar el APK directamente desde el navegador de su Hyundai Google TV:",
                                style = MaterialTheme.typography.labelMedium,
                                color = JarvisNeonGlow
                            )
                            Text(
                                text = "A) Usando la App 'Downloader by AFTVnews' (Método más recomendado en TV):",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "1. Desde la Google Play Store de su TV, instale 'Downloader'.\n2. Abra Downloader e introduzca la URL directa de descarga del APK o el código corto.\n3. Downloader descargará el APK y abrirá el instalador automáticamente.\n4. Si el sistema bloquea la instalación: Vaya a Ajustes > Aplicaciones > Seguridad y restricciones > Instalar aplicaciones desconocidas > Active 'Downloader'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextSecondary
                            )
                            Text(
                                text = "B) Usando un Navegador Web de TV (TV Bro / JioPages / Chrome):",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "1. Abra el navegador en su TV y acceda a la URL donde tenga alojado el APK (Google Drive, enlace directo o servidor local).\n2. Descargue el archivo 'app-debug.apk'.\n3. Abra la lista de descargas del navegador o utilice un gestor como 'File Commander' en la carpeta Download.\n4. Conceda el permiso 'Instalar aplicaciones desconocidas' para el navegador y complete la instalación.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextSecondary
                            )
                        }
                    }
                }

                // Section 6: Telemetry Diagnostics
                item {
                    SettingsCard(
                        title = "6. DIAGNÓSTICO DE TELEMETRÍA DEL SISTEMA",
                        icon = Icons.Default.Info
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "Dispositivo objetivo: Hyundai Google TV 14", style = MaterialTheme.typography.labelSmall, color = JarvisTextPrimary)
                                Text(text = "Dispositivo secundario: Samsung Galaxy A35", style = MaterialTheme.typography.labelSmall, color = JarvisTextPrimary)
                                Text(text = "Núcleo de IA: ChatGPT Oficial (chatgpt.com)", style = MaterialTheme.typography.labelSmall, color = JarvisCyan)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "Reconocimiento de voz: es-CO (Colombia)", style = MaterialTheme.typography.labelSmall, color = JarvisTextPrimary)
                                Text(text = "Zona Horaria: America/Bogota (UTC-5)", style = MaterialTheme.typography.labelSmall, color = JarvisTextPrimary)
                                Text(text = "Estado: Seguro (Sin almacenamiento de credenciales)", style = MaterialTheme.typography.labelSmall, color = JarvisStatusGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisSurface)
            .tvFocusGlow(isFocused = false, cornerRadius = 12.dp)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = JarvisCyan,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}
