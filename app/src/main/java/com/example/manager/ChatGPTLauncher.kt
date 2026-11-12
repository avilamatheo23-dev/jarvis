package com.example.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object ChatGPTLauncher {
    private const val TAG = "ChatGPTLauncher"
    const val CHATGPT_OFFICIAL_URL = "https://chatgpt.com/"
    const val CHATGPT_PACKAGE = "com.openai.chatgpt"

    /**
     * Checks if official ChatGPT native app is installed
     */
    fun isNativeAppInstalled(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(CHATGPT_PACKAGE, 0)
            info.enabled
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Launches official ChatGPT.
     * Prefers native app if installed, otherwise opens the official web site
     * in the system browser so the user can log in safely.
     */
    fun launchChatGPT(context: Context) {
        val pm = context.packageManager
        if (isNativeAppInstalled(context)) {
            val intent = pm.getLeanbackLaunchIntentForPackage(CHATGPT_PACKAGE)
                ?: pm.getLaunchIntentForPackage(CHATGPT_PACKAGE)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
        }

        // Open official website in external browser
        openOfficialWeb(context)
    }

    /**
     * Opens the official website https://chatgpt.com/ in the device browser.
     * Respects security: does not handle or store passwords.
     */
    fun openOfficialWeb(context: Context) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(CHATGPT_OFFICIAL_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening browser: ${e.message}")
        }
    }
}
