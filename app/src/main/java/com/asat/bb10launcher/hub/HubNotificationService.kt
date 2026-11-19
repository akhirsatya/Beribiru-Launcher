package com.asat.bb10launcher.hub

import android.app.ActivityOptions
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Mengumpulkan notifikasi dari semua app untuk ditampilkan di Hub.
 * Perlu izin "Notification access" dari pengguna.
 */
class HubNotificationService : NotificationListenerService() {

    private val labelCache = HashMap<String, String>()

    override fun onListenerConnected() {
        HubStore.setConnected(true)
        refresh()
    }

    override fun onListenerDisconnected() {
        HubStore.setConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) = refresh()
    override fun onNotificationRemoved(sbn: StatusBarNotification?) = refresh()

    private fun refresh() {
        val active = try { activeNotifications } catch (e: Exception) { null } ?: return
        HubStore.update(
            active.asSequence()
                .filter { it.packageName != packageName }
                .filter { !it.isOngoing && (it.notification.flags and Notification.FLAG_GROUP_SUMMARY) == 0 }
                .mapNotNull(::toItem)
                .sortedByDescending { it.postTime }
                .toList()
        )
    }

    private fun toItem(sbn: StatusBarNotification): HubItem? {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?.takeIf { it.isNotBlank() } ?: return null
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        return HubItem(
            key = sbn.key,
            packageName = sbn.packageName,
            appLabel = appLabel(sbn.packageName),
            title = title,
            text = text,
            postTime = sbn.postTime,
            contentIntent = sbn.notification.contentIntent,
        )
    }

    private fun appLabel(pkg: String): String = labelCache.getOrPut(pkg) {
        runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        }.getOrDefault(pkg)
    }
}

/** Buka pesan: kirim contentIntent milik notifikasi, atau buka app-nya kalau tidak ada. */
fun HubItem.open(context: Context) {
    val launchApp = {
        context.packageManager.getLaunchIntentForPackage(packageName)
            ?.let { context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
    val pi = contentIntent ?: run { launchApp(); return }
    val options = if (Build.VERSION.SDK_INT >= 34) {
        ActivityOptions.makeBasic()
            .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
            .toBundle()
    } else null
    try {
        pi.send(context, 0, null, null, null, null, options)
    } catch (e: PendingIntent.CanceledException) {
        launchApp()
    }
}
