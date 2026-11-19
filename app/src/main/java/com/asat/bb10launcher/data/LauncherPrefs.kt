package com.asat.bb10launcher.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Pengaturan launcher yang disimpan permanen. */
class LauncherPrefs private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("launcher", Context.MODE_PRIVATE)

    private val _iconPack = MutableStateFlow(prefs.getString(KEY_ICON_PACK, null))
    /** Package icon pack yang dipilih, null = ikon bawaan sistem. */
    val iconPack: StateFlow<String?> = _iconPack.asStateFlow()

    fun setIconPack(pkg: String?) {
        prefs.edit { putString(KEY_ICON_PACK, pkg) }
        _iconPack.value = pkg
    }

    private val _bb10StatusBar = MutableStateFlow(prefs.getBoolean(KEY_BB10_STATUS_BAR, true))
    /** true = sembunyikan status bar Android & tampilkan status bar ala BB10 (jam di tengah). */
    val bb10StatusBar: StateFlow<Boolean> = _bb10StatusBar.asStateFlow()

    fun setBb10StatusBar(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_BB10_STATUS_BAR, enabled) }
        _bb10StatusBar.value = enabled
    }

    companion object {
        private const val KEY_ICON_PACK = "icon_pack"
        private const val KEY_BB10_STATUS_BAR = "bb10_status_bar"

        @Volatile private var instance: LauncherPrefs? = null

        fun get(context: Context): LauncherPrefs =
            instance ?: synchronized(this) {
                instance ?: LauncherPrefs(context.applicationContext).also { instance = it }
            }
    }
}
