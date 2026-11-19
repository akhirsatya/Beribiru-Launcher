package com.asat.bb10launcher.iconpack

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class IconPackInfo(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
)

/** Mencari icon pack yang ter-install (format ADW / Nova / Apex / GO / dll). */
object IconPackDiscovery {

    private val ACTIONS = listOf(
        "org.adw.launcher.THEMES",
        "com.novalauncher.THEME",
        "com.gau.go.launcherex.theme",
        "com.dlto.atom.launcher.THEME",
        "com.fede.launcher.THEME_ICONPACK",
    )
    private val CATEGORIES = listOf(
        "com.anddoes.launcher.THEME",
        "com.teslacoilsw.launcher.THEME",
    )

    fun find(context: Context): List<IconPackInfo> {
        val pm = context.packageManager
        val intents = ACTIONS.map { Intent(it) } +
            CATEGORIES.map { Intent(Intent.ACTION_MAIN).addCategory(it) }

        return intents
            .flatMap { pm.queryIntentActivities(it, 0) }
            .map { it.activityInfo.packageName }
            .distinct()
            .mapNotNull { pkg ->
                runCatching {
                    val info = pm.getApplicationInfo(pkg, 0)
                    IconPackInfo(
                        packageName = pkg,
                        label = pm.getApplicationLabel(info).toString(),
                        icon = pm.getApplicationIcon(info).toBitmap(96, 96).asImageBitmap(),
                    )
                }.getOrNull()
            }
            .sortedBy { it.label.lowercase() }
    }
}
