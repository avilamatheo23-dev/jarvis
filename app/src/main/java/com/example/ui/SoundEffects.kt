package com.example.ui

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

/**
 * Provides futuristic auditory feedback for TV remote navigation and actions.
 */
object SoundEffects {
    private const val TAG = "JarvisSounds"
    private var toneGen: ToneGenerator? = null
    var isEnabled: Boolean = true

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_SYSTEM, 65)
        } catch (e: Exception) {
            Log.w(TAG, "ToneGenerator init error: ${e.message}")
        }
    }

    /**
     * Subtle futuristic blip when navigating via D-Pad
     */
    fun playFocusSound() {
        if (!isEnabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
        } catch (_: Exception) {}
    }

    /**
     * Futuristic acknowledgment tone when activating voice or opening an app
     */
    fun playActivateSound() {
        if (!isEnabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 70)
        } catch (_: Exception) {}
    }

    /**
     * Alert tone for errors or non-installed apps
     */
    fun playAlertSound() {
        if (!isEnabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_NACK, 90)
        } catch (_: Exception) {}
    }

    /**
     * Futuristic confirm chime
     */
    fun playConfirmSound() {
        if (!isEnabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 60)
        } catch (_: Exception) {}
    }
}
