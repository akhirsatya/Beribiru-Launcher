package com.asat.bb10launcher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.asat.bb10launcher.data.AppInfo
import com.asat.bb10launcher.data.AppRepository
import com.asat.bb10launcher.data.LauncherPrefs
import com.asat.bb10launcher.iconpack.IconPackDiscovery
import com.asat.bb10launcher.iconpack.IconPackInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = LauncherPrefs.get(application)
    private val repo = AppRepository(application, prefs)

    val apps: StateFlow<List<AppInfo>> = repo.observeApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedIconPack: StateFlow<String?> = prefs.iconPack

    private val _iconPacks = MutableStateFlow<List<IconPackInfo>>(emptyList())
    val iconPacks: StateFlow<List<IconPackInfo>> = _iconPacks.asStateFlow()

    fun refreshIconPacks() {
        viewModelScope.launch(Dispatchers.Default) {
            _iconPacks.value = IconPackDiscovery.find(getApplication())
        }
    }

    fun selectIconPack(pkg: String?) = prefs.setIconPack(pkg)

    val bb10StatusBar: StateFlow<Boolean> = prefs.bb10StatusBar
    fun setBb10StatusBar(enabled: Boolean) = prefs.setBb10StatusBar(enabled)

    fun launch(app: AppInfo) { repo.launch(app) }
    fun openAppInfo(app: AppInfo) { repo.openAppInfo(app) }
}
