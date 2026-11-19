package com.asat.bb10launcher.hub

import android.app.PendingIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HubItem(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val postTime: Long,
    val contentIntent: PendingIntent?,
) {
    /** Pesan baru pada notifikasi yang sama (postTime berubah) dianggap belum dibaca lagi. */
    val readKey: String get() = "$key@$postTime"
}

/** Penampung bersama antara NotificationListenerService dan UI launcher. */
object HubStore {
    private val _items = MutableStateFlow<List<HubItem>>(emptyList())
    val items: StateFlow<List<HubItem>> = _items.asStateFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _readKeys = MutableStateFlow<Set<String>>(emptySet())
    val readKeys: StateFlow<Set<String>> = _readKeys.asStateFlow()

    internal fun update(items: List<HubItem>) {
        _items.value = items
        val alive = items.mapTo(HashSet()) { it.readKey }
        _readKeys.update { keys -> keys.filterTo(HashSet()) { it in alive } }
    }

    internal fun setConnected(value: Boolean) { _connected.value = value }

    fun markRead(item: HubItem) = _readKeys.update { it + item.readKey }

    fun markAllRead() = _readKeys.update { keys -> keys + _items.value.map { it.readKey } }
}
