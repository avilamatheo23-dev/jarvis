package com.example.manager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log

data class TargetApp(
    val id: String,
    val name: String,
    val category: String,
    val packageCandidates: List<String>,
    val fallbackUrl: String? = null,
    val iconResId: Int? = null,
    val isInstalled: Boolean = false,
    val installedPackage: String? = null,
    val appIcon: Drawable? = null
)

sealed class AppLaunchResult {
    data class Success(val appName: String) : AppLaunchResult()
    data class AppNotInstalled(
        val appName: String,
        val targetPackage: String,
        val fallbackUrl: String? = null
    ) : AppLaunchResult()
    data class Error(val message: String) : AppLaunchResult()
}

class AppManager(private val context: Context) {
    private val TAG = "JarvisAppManager"
    private val packageManager: PackageManager = context.packageManager

    // Target major apps requested by user
    private val primaryAppsConfig = listOf(
        TargetApp(
            id = "youtube",
            name = "YouTube",
            category = "Streaming",
            packageCandidates = listOf(
                "com.google.android.youtube.tv",
                "com.google.android.youtube"
            ),
            fallbackUrl = "https://www.youtube.com"
        ),
        TargetApp(
            id = "netflix",
            name = "Netflix",
            category = "Streaming",
            packageCandidates = listOf(
                "com.netflix.ninja",
                "com.netflix.mediaclient"
            ),
            fallbackUrl = "https://www.netflix.com"
        ),
        TargetApp(
            id = "prime_video",
            name = "Prime Video",
            category = "Streaming",
            packageCandidates = listOf(
                "com.amazon.amazonvideo.livingroom",
                "com.amazon.avod.thirdpartyclient"
            ),
            fallbackUrl = "https://www.primevideo.com"
        ),
        TargetApp(
            id = "play_store",
            name = "Google Play",
            category = "Sistema",
            packageCandidates = listOf(
                "com.android.vending"
            )
        ),
        TargetApp(
            id = "chatgpt",
            name = "ChatGPT",
            category = "Inteligencia Artificial",
            packageCandidates = listOf(
                "com.openai.chatgpt"
            ),
            fallbackUrl = "https://chatgpt.com/"
        )
    )

    /**
     * Resolves installed status and drawables for target primary apps
     */
    fun getPrimaryApps(): List<TargetApp> {
        return primaryAppsConfig.map { app ->
            var installedPkg: String? = null
            var drawable: Drawable? = null

            for (pkg in app.packageCandidates) {
                try {
                    val info = packageManager.getApplicationInfo(pkg, 0)
                    if (info.enabled) {
                        installedPkg = pkg
                        drawable = packageManager.getApplicationIcon(info)
                        break
                    }
                } catch (_: PackageManager.NameNotFoundException) {
                    // Try next candidate
                }
            }

            app.copy(
                isInstalled = installedPkg != null,
                installedPackage = installedPkg,
                appIcon = drawable
            )
        }
    }

    /**
     * Queries all installed Leanback TV and launcher applications
     */
    fun getAllInstalledTvApps(): List<TargetApp> {
        val apps = mutableListOf<TargetApp>()
        val selfPackage = context.packageName

        // 1. Check Leanback TV Launcher intents
        val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val tvResolveList = packageManager.queryIntentActivities(leanbackIntent, 0)

        // 2. Check standard Launcher intents as fallback
        val standardIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val standardResolveList = packageManager.queryIntentActivities(standardIntent, 0)

        val combinedResolves = (tvResolveList + standardResolveList).distinctBy { it.activityInfo.packageName }

        for (resolve in combinedResolves) {
            val pkg = resolve.activityInfo.packageName
            if (pkg == selfPackage) continue // Skip self

            try {
                val appName = resolve.loadLabel(packageManager).toString()
                val icon = resolve.loadIcon(packageManager)
                apps.add(
                    TargetApp(
                        id = pkg,
                        name = appName,
                        category = "Aplicación TV",
                        packageCandidates = listOf(pkg),
                        isInstalled = true,
                        installedPackage = pkg,
                        appIcon = icon
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error loading app info for $pkg: ${e.message}")
            }
        }

        return apps
    }

    /**
     * Safely launches an application or reports missing app
     */
    fun launchApp(app: TargetApp): AppLaunchResult {
        // Special case for ChatGPT: if not installed, opens official website https://chatgpt.com/ directly
        if (app.id == "chatgpt") {
            val installedPkg = app.installedPackage ?: findInstalledPackage(app.packageCandidates)
            if (installedPkg != null) {
                val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(installedPkg)
                    ?: packageManager.getLaunchIntentForPackage(installedPkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return AppLaunchResult.Success(app.name)
                }
            }
            // Open official web directly
            openWebUrl("https://chatgpt.com/")
            return AppLaunchResult.Success("ChatGPT (Sitio Web Oficial)")
        }

        // Generic apps: find installed candidate
        val installedPkg = app.installedPackage ?: findInstalledPackage(app.packageCandidates)

        if (installedPkg != null) {
            try {
                val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(installedPkg)
                    ?: packageManager.getLaunchIntentForPackage(installedPkg)

                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return AppLaunchResult.Success(app.name)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error launching $installedPkg: ${e.message}")
                return AppLaunchResult.Error("No fue posible abrir ${app.name}: ${e.localizedMessage}")
            }
        }

        // App not found
        val targetPkg = app.packageCandidates.firstOrNull() ?: ""
        return AppLaunchResult.AppNotInstalled(
            appName = app.name,
            targetPackage = targetPkg,
            fallbackUrl = app.fallbackUrl
        )
    }

    /**
     * Opens Play Store to install missing app
     */
    fun openInPlayStore(packageName: String) {
        try {
            val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(playIntent)
        } catch (_: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Could not open store: ${e.message}")
            }
        }
    }

    fun openWebUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not open url $url: ${e.message}")
        }
    }

    private fun findInstalledPackage(candidates: List<String>): String? {
        for (pkg in candidates) {
            try {
                val info = packageManager.getApplicationInfo(pkg, 0)
                if (info.enabled) return pkg
            } catch (_: PackageManager.NameNotFoundException) {}
        }
        return null
    }
}
