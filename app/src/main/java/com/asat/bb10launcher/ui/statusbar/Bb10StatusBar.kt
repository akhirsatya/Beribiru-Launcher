package com.asat.bb10launcher.ui.statusbar

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val STATUS_BAR_HEIGHT = 24.dp

private enum class NetType { WIFI, CELL, NONE }
private data class Battery(val level: Int, val charging: Boolean)

/**
 * Status bar ala BB10: indikator di kiri, jam di tengah, jaringan & baterai di kanan.
 * Dipakai saat status bar sistem Android disembunyikan.
 */
@Composable
fun Bb10StatusBar(contentColor: Color, hasUnread: Boolean, modifier: Modifier = Modifier) {
    val time = rememberClockText()
    val battery = rememberBattery()
    val network = rememberNetworkType()
    val ringer = rememberRingerMode()
    val alarmSet = rememberAlarmSet()

    Box(modifier.fillMaxWidth().height(STATUS_BAR_HEIGHT).padding(horizontal = 8.dp)) {
        Row(
            Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasUnread) Box(Modifier.size(7.dp).background(Color(0xFFE53935), CircleShape))
            if (alarmSet) Icon(Icons.Outlined.Alarm, "Alarm", tint = contentColor, modifier = Modifier.size(14.dp))
            when (ringer) {
                AudioManager.RINGER_MODE_SILENT ->
                    Icon(Icons.Outlined.VolumeOff, "Silent", tint = contentColor, modifier = Modifier.size(14.dp))
                AudioManager.RINGER_MODE_VIBRATE ->
                    Icon(Icons.Outlined.Vibration, "Vibrate", tint = contentColor, modifier = Modifier.size(14.dp))
            }
        }

        Text(time, color = contentColor, fontSize = 14.sp, modifier = Modifier.align(Alignment.Center))

        Row(
            Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val netIcon = when (network) {
                NetType.WIFI -> Icons.Filled.Wifi
                NetType.CELL -> Icons.Filled.SignalCellularAlt
                NetType.NONE -> Icons.Filled.SignalCellularOff
            }
            Icon(netIcon, null, tint = contentColor, modifier = Modifier.size(14.dp))
            BatteryIcon(battery, contentColor)
        }
    }
}

@Composable
private fun BatteryIcon(battery: Battery, color: Color) {
    val fill = when {
        battery.charging -> Color(0xFF6CC04A)
        battery.level <= 15 -> Color(0xFFE53935)
        else -> color
    }
    Canvas(Modifier.size(width = 22.dp, height = 11.dp)) {
        val stroke = 1.2.dp.toPx()
        val nub = 2.dp.toPx()
        val bodyW = size.width - nub
        drawRoundRect(
            color = color,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(bodyW - stroke, size.height - stroke),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(stroke),
        )
        drawRect(color, topLeft = Offset(bodyW, size.height * 0.3f), size = Size(nub, size.height * 0.4f))
        val inset = stroke * 2
        val innerW = (bodyW - inset * 2) * (battery.level.coerceIn(0, 100) / 100f)
        drawRect(fill, topLeft = Offset(inset, inset), size = Size(innerW, size.height - inset * 2))
    }
}

@Composable
private fun rememberClockText(): String {
    val context = LocalContext.current
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(60_000L - (System.currentTimeMillis() % 60_000L))
        }
    }
    val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"
    return now.format(DateTimeFormatter.ofPattern(pattern))
}

/** State dari broadcast sistem (termasuk sticky broadcast seperti baterai). */
@Composable
private fun <T> rememberBroadcast(actions: List<String>, read: (Context, Intent?) -> T): T {
    val context = LocalContext.current
    var value by remember { mutableStateOf(read(context, null)) }
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) { value = read(c, intent) }
        }
        val filter = IntentFilter().apply { actions.forEach(::addAction) }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
            ?.let { value = read(context, it) }
        onDispose { context.unregisterReceiver(receiver) }
    }
    return value
}

@Composable
private fun rememberBattery(): Battery = rememberBroadcast(listOf(Intent.ACTION_BATTERY_CHANGED)) { ctx, intent ->
    if (intent != null) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        Battery(
            level = if (level >= 0) level * 100 / scale else 100,
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL,
        )
    } else {
        val bm = ctx.getSystemService(BatteryManager::class.java)
        Battery(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY), bm.isCharging)
    }
}

@Composable
private fun rememberRingerMode(): Int = rememberBroadcast(listOf(AudioManager.RINGER_MODE_CHANGED_ACTION)) { ctx, _ ->
    ctx.getSystemService(AudioManager::class.java).ringerMode
}

@Composable
private fun rememberAlarmSet(): Boolean = rememberBroadcast(listOf(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)) { ctx, _ ->
    ctx.getSystemService(AlarmManager::class.java).nextAlarmClock != null
}

@Composable
private fun rememberNetworkType(): NetType {
    val context = LocalContext.current
    val cm = remember { context.getSystemService(ConnectivityManager::class.java) }
    var type by remember { mutableStateOf(cm.getNetworkCapabilities(cm.activeNetwork).toNetType()) }
    DisposableEffect(cm) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) { type = caps.toNetType() }
            override fun onLost(network: Network) { type = NetType.NONE }
        }
        cm.registerDefaultNetworkCallback(callback)
        onDispose { cm.unregisterNetworkCallback(callback) }
    }
    return type
}

private fun NetworkCapabilities?.toNetType(): NetType = when {
    this == null -> NetType.NONE
    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetType.WIFI
    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetType.CELL
    else -> NetType.NONE
}
