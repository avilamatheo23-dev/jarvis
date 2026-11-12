# J.A.R.V.I.S. HOME
### Your Personal Intelligent Television Assistant
**Diseñado para Televisores Hyundai con Google TV 14 y compatible con Samsung Galaxy A35**

---

## 1. DESCRIPCIÓN DEL PROYECTO

**J.A.R.V.I.S. HOME** es una aplicación nativa completa para Android TV y Google TV 14 construida en Kotlin y Jetpack Compose, inspirada en los sistemas holográficos de Iron Man y Tony Stark.

Transforma la pantalla de su televisor Hyundai en una estación de mando interactiva con núcleo holográfico Arc Reactor animado, reloj en tiempo real para Colombia (zona horaria `America/Bogota`), reporte meteorológico en vivo con ciudades colombianas, control por voz en español latinoamericano, acceso directo y seguro a ChatGPT oficial (`chatgpt.com`), carrusel de aplicaciones optimizado para control remoto D-Pad, y capacidad para actuar como lanzador principal (Home Launcher).

---

## 2. CARACTERÍSTICAS PRINCIPALES

- **Estética Holográfica Sci-Fi:** Fondo negro azulado (#030712), acentos en cian (#00E5FF) y azul eléctrico (#00B0FF), bordes reactivos y tipografía tecnológica de alta legibilidad.
- **Núcleo Arc Reactor Animado:** Anillos concéntricos giratorios en direcciones opuestas, pulso de energía y reacción visual al nivel de decibelios (RMS) de audio.
- **Asistente de Voz Integrado:**
  - Reconocimiento de voz en español colombiano (`es-CO` / `es-419`).
  - Activación por botón en pantalla o tecla de micrófono del control remoto.
  - Síntesis de voz con Android TextToSpeech en voz masculina latinoamericana (si está instalada en el motor TTS).
  - Comandos vocales directos: *"Hola JARVIS"*, *"Abrir YouTube"*, *"Abrir Netflix"*, *"Abrir Prime Video"*, *"Abrir ChatGPT"*, *"Abrir Play Store"*, *"Clima"*, *"Hora"*, *"Estado del sistema"*, *"Configuración"*, *"Silencio"*.
- **Integración Segura con ChatGPT:**
  - **Requisito fundamental cumplido:** Cero consumo de OpenAI API, sin solicitud de API keys y sin servicios de terceros.
  - Abre el portal web oficial `https://chatgpt.com/` o la app oficial de ChatGPT si está instalada en el dispositivo.
  - Permite inicio de sesión legítimo del usuario sin almacenar contraseñas ni intervenir credenciales.
- **Lanzador de Aplicaciones:**
  - Detección dinámica de YouTube, Netflix, Prime Video, Google Play Store, ChatGPT y todas las apps de TV instaladas.
  - Manejo seguro de apps ausentes: si una aplicación no está instalada, no se producen cierres forzados; se muestra un diálogo holográfico con opción de descargarla desde Google Play Store.
- **Navegación 100% con Control Remoto (D-Pad):**
  - Efectos visuales de enfoque con iluminación neón (`tvFocusGlow`) y escala suave.
  - Efectos sonoros futuristas (clicks y chimes sintetizados).
- **Reloj y Clima de Colombia:**
  - Hora oficial de Colombia (`America/Bogota`, UTC-5) con fecha en español.
  - Clima configurable para ciudades colombianas (Bogotá, Medellín, Cali, Barranquilla, Cartagena, Bucaramanga, Pereira, Manizales) vía API abierta Open-Meteo con respaldo sin conexión.

---

## 3. COMPATIBILIDAD Y ARQUITECTURA

- **Sistema Operativo:** Google TV 14 / Android TV (Android 14+, API 34-36) y Android móvil (API 24+).
- **Dispositivos Probados y Compatibles:**
  - Televisores Hyundai con Google TV.
  - Dispositivos secundarios / móviles: Samsung Galaxy A35 (Android 14/One UI 6+).
- **Relación de Aspecto:** Horizontal 16:9 optimizado para pantallas grandes.
- **Arquitectura de Código:**
  - `MainActivity.kt`: Ciclo de vida, manejo de teclas de control remoto y permisos de micrófono.
  - `HomeScreen.kt`: Pantalla principal holográfica con Arc Reactor, barra de telemetría y carrusel de apps.
  - `SettingsScreen.kt`: Panel de ajuste de ciudad, audio, pruebas de voz y manual de configuración del launcher.
  - `VoiceManager.kt`: Motor de reconocimiento de voz y parser de comandos en español.
  - `SpeechManager.kt`: Motor TextToSpeech con selección de voz masculina en español.
  - `AppManager.kt`: Detección y lanzamiento seguro de aplicaciones de TV.
  - `ChatGPTLauncher.kt`: Acceso directo y seguro al portal oficial de ChatGPT.
  - `WeatherRepository.kt`: Servicio meteorológico con persistencia y fallback.
  - `Animations.kt`: Animaciones de rotación, pulsación del núcleo Arc Reactor y barras de audio.
  - `SoundEffects.kt`: Generador de tonos auditivos para navegación con control remoto.

---

## 4. INSTRUCCIONES DE INSTALACIÓN EN HYUNDAI GOOGLE TV 14

### Método A: Transferencia directa desde Samsung Galaxy A35 con "Send Files to TV" (Recomendado)

1. **En su televisor Hyundai Google TV:**
   - Abra **Google Play Store**.
   - Busque e instale **Send Files to TV** y un gestor de archivos como **File Commander** o **AnExplorer**.
   - Abra la app *Send Files to TV* en el televisor y seleccione **RECEIVE**.

2. **En su Samsung Galaxy A35:**
   - Instale **Send Files to TV** desde Google Play Store.
   - Abra la app, elija **SEND** y seleccione el archivo APK de **J.A.R.V.I.S. HOME** (`app-debug.apk`).
   - Seleccione su televisor Hyundai en la lista de dispositivos conectados a la misma red Wi-Fi.

3. **Completar la instalación en el televisor:**
   - Una vez recibido el archivo en el televisor, use *File Commander* para ir a la carpeta `Download`.
   - Haga clic sobre el APK de J.A.R.V.I.S. HOME.
   - Si Google TV solicita permiso para instalar aplicaciones de fuentes desconocidas, concédalo y pulse **Instalar**.

### Método A: Descarga e Instalación Directa desde el Navegador de la TV (El más rápido sin cables ni PC)

En Android TV y Google TV 14, la forma universal de descargar APKs directamente desde la web es utilizando la aplicación gratuita **Downloader by AFTVnews** (o un navegador como **TV Bro** o **JioPages**):

1. **Instalar Downloader en el televisor:**
   - En su televisor Hyundai, abra **Google Play Store**.
   - Busque **Downloader** (icono naranja de AFTVnews) e instálelo.
   
2. **Habilitar orígenes desconocidos para el navegador en Google TV 14:**
   - Vaya a **Ajustes de Google TV** (rueda dentada) > **Sistema** > **Información**.
   - Pulse 7 veces seguidas sobre **Compilación del SO de Android TV** para activar las Opciones de desarrollador.
   - Regrese a **Ajustes > Aplicaciones > Seguridad y restricciones > Instalar aplicaciones desconocidas**.
   - Active el interruptor para **Downloader** (o para el navegador que vaya a usar).

3. **Descargar e instalar el APK:**
   - Abra la aplicación **Downloader** en su televisor.
   - En la barra de dirección URL, ingrese el enlace de descarga directo del APK (o el enlace de Google Drive / MediaFire / servidor local donde haya subido `app-debug.apk`).
   - Pulse **Go**. La descarga comenzará inmediatamente en pantalla.
   - Al terminar, Downloader abrirá automáticamente la ventana de instalación. Pulse **Instalar**.
   - Al finalizar, pulse **Abrir** para iniciar **J.A.R.V.I.S. HOME**.

---

### Método B: Descargar el APK desde el navegador de su PC o Samsung Galaxy A35

1. Desde el menú de **AI Studio**, haga clic en los tres puntos de la esquina superior o en **Ajustes** y seleccione **Export project as ZIP** o **Generate APK / Download APK**.
2. Guarde el archivo `app-debug.apk` en su dispositivo.
3. Transfiéralo a su televisor Hyundai usando **Send Files to TV**, USB o Google Drive.

---

### Método C: Transferencia directa desde Samsung Galaxy A35 con "Send Files to TV"

1. Copie el archivo `app-debug.apk` a una memoria USB con formato FAT32 o exFAT.
2. Conecte la memoria al puerto USB de su televisor Hyundai.
3. Abra un gestor de archivos en el televisor (como *File Commander*), seleccione la memoria USB y ejecute el instalador.

---

### Método C: Mediante ADB Inalámbrico (Desarrollador)

1. En el televisor Hyundai: Vaya a **Ajustes > Sistema > Información** y pulse 7 veces sobre **Compilación del SO de Android TV** para activar las opciones de desarrollador.
2. Vaya a **Ajustes > Sistema > Opciones para desarrolladores** y active **Depuración USB** y **Depuración inalámbrica**.
3. Anote la dirección IP y puerto que muestra la pantalla.
4. Desde su computadora o consola:
   ```bash
   adb connect <IP_DEL_TELEVISOR>:5555
   adb install -r app-debug.apk
   ```

---

## 5. CÓMO CONFIGURAR J.A.R.V.I.S. COMO PANTALLA PRINCIPAL (HOME LAUNCHER)

El AndroidManifest.xml de J.A.R.V.I.S. HOME incluye las categorías `android.intent.category.HOME` y `android.intent.category.LEANBACK_LAUNCHER`.

### Paso a paso en Google TV 14:
1. Presione el botón **HOME** (icono de casa) en el control remoto de su televisor Hyundai.
2. Si el sistema muestra el selector de inicio, elija **J.A.R.V.I.S. HOME** y seleccione **Siempre**.
3. Si el sistema no muestra el menú automáticamente:
   - Vaya a **Ajustes de Google TV** (icono de engranaje).
   - Ingrese a **Aplicaciones**.
   - Seleccione **Acceso especial de aplicaciones**.
   - Seleccione **Aplicación de inicio predeterminada** (Default Home App).
   - Elija **J.A.R.V.I.S. HOME**.

### Cómo regresar al lanzador original de Google TV en cualquier momento:
- Ingrese a **Ajustes > Aplicaciones > Acceso especial de aplicaciones > Aplicación de inicio predeterminada** y seleccione **Google TV** o **Android TV Home**.
- *Nota de seguridad:* J.A.R.V.I.S. HOME no modifica particiones ni elimina Google TV; es una alternativa 100% reversible y segura.

---

## 6. SOLUCIÓN DE ERRORES FRECUENTES (FAQ)

| Problema | Causa | Solución |
|---|---|---|
| **El micrófono no responde al hablar** | No se concedió el permiso `RECORD_AUDIO` | Haga clic en el botón de voz en pantalla y seleccione "Permitir" cuando aparezca el diálogo del sistema. También verifique que el control remoto esté emparejado por Bluetooth para transmitir voz. |
| **La voz de J.A.R.V.I.S. suena robótica o femenina** | Motor TextToSpeech predeterminado sin voz masculina descargada | Vaya a los Ajustes de Google TV > Sistema > Accesibilidad > Salida de Texto a Voz. Asegúrese de que el motor de voz de Google tenga descargado el paquete de voz en español latinoamericano. |
| **Al pulsar una aplicación (ej. Netflix) aparece un diálogo de advertencia** | La aplicación no está instalada en el televisor | El diálogo le permite pulsar "Abrir Play Store" para instalarla directamente desde la tienda oficial sin que la app se cierre. |
| **ChatGPT no abre directamente en aplicación** | La app nativa de ChatGPT para Android TV aún no existe oficialmente en Play Store | El launcher abre automáticamente la versión web oficial optimizada en pantalla completa en `https://chatgpt.com/`, donde puede iniciar sesión normalmente. |
| **Error 'No se pudo instalar el APK'** | Fuentes desconocidas desactivadas en Google TV | Vaya a Ajustes > Seguridad y restricciones > Fuentes desconocidas y active el permiso para su app de archivos (*File Commander* o *Send Files to TV*). |

---

## 7. COMANDOS DE VOZ SOPORTADOS

Puede hablar tras presionar el botón de micrófono en pantalla o la tecla de voz del control remoto:
- *"Hola JARVIS"* -> Saludo de protocolo y diagnóstico de sistemas.
- *"Abrir YouTube"* -> Inicia YouTube en pantalla completa.
- *"Abrir Netflix"* -> Inicia Netflix.
- *"Abrir Prime Video"* -> Inicia Amazon Prime Video.
- *"Abrir ChatGPT"* -> Abre el portal web oficial https://chatgpt.com/.
- *"Abrir Play Store"* -> Abre la tienda de aplicaciones.
- *"¿Cómo está el clima?"* -> Reporta temperatura y estado del tiempo en la ciudad colombiana configurada.
- *"¿Qué hora es?"* -> Indica la hora y fecha oficial de Colombia.
- *"Estado del sistema"* -> Reporte de telemetría y operatividad.
- *"Configuración"* -> Abre el panel de opciones y ajustes.
- *"Silencio"* -> Detiene la voz en reproducción.
