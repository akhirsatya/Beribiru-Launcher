package com.asat.bb10launcher.data

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.asat.bb10launcher.iconpack.IconPack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

/**
 * Sumber daftar aplikasi. Memakai LauncherApps (API resmi untuk launcher),
 * jadi app di Work Profile juga ikut muncul dan otomatis ter-update saat
 * ada app di-install / di-uninstall atau icon pack diganti.
 */
class AppRepository(
    private val context: Context,
    private val prefs: LauncherPrefs,
) {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager = context.getSystemService(UserManager::class.java)
    private val iconSizePx = (64 * context.resources.displayMetrics.density).roundToInt()

    private var cachedPack: IconPack? = null
    private var cachedPackUpdateTime = 0L

    fun observeApps(): Flow<List<AppInfo>> =
        combine(packageChanges(), prefs.iconPack) { _, pack -> pack }
            .conflate()
            .map { pack -> loadApps(pack) }
            .flowOn(Dispatchers.Default)

    private fun packageChanges(): Flow<Unit> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String, user: UserHandle) { trySend(Unit) }
            override fun onPackageRemoved(packageName: String, user: UserHandle) { trySend(Unit) }
            override fun onPackageChanged(packageName: String, user: UserHandle) { trySend(Unit) }
            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) { trySend(Unit) }
            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) { trySend(Unit) }
        }
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        trySend(Unit) // muat pertama kali
        awaitClose { launcherApps.unregisterCallback(callback) }
    }

    private fun loadApps(iconPackPkg: String?): List<AppInfo> {
        val pack = iconPackFor(iconPackPkg)
        return userManager.userProfiles
            .flatMap { user ->
                launcherApps.getActivityList(null, user).map { info ->
                    AppInfo(
                        label = info.label.toString(),
                        component = info.componentName,
                        user = user,
                        icon = loadIcon(info, user, pack),
                    )
                }
            }
            .filter { it.component.packageName != context.packageName }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    private fun loadIcon(info: LauncherActivityInfo, user: UserHandle, pack: IconPack?): ImageBitmap {
        val themed = pack?.let {
            runCatching { it.iconFor(info.componentName, info.getIcon(0), iconSizePx) }.getOrNull()
        }
        val drawable = when {
            themed == null -> info.getBadgedIcon(0)
            user == Process.myUserHandle() -> BitmapDrawable(context.resources, themed)
            // tetap beri badge koper untuk app Work Profile
            else -> context.packageManager.getUserBadgedIcon(BitmapDrawable(context.resources, themed), user)
        }
        return drawable.toBitmap(iconSizePx, iconSizePx).asImageBitmap()
    }

    /** Icon pack di-cache, dimuat ulang hanya kalau pack-nya ganti atau di-update. */
    private fun iconPackFor(pkg: String?): IconPack? {
        if (pkg == null) { cachedPack = null; return null }
        val updated = runCatching { context.packageManager.getPackageInfo(pkg, 0).lastUpdateTime }
            .getOrNull() ?: return null // pack sudah di-uninstall -> ikon sistem
        cachedPack?.let { if (it.packageName == pkg && cachedPackUpdateTime == updated) return it }
        return IconPack.load(context, pkg).also {
            cachedPack = it
            cachedPackUpdateTime = updated
        }
    }

    fun launch(app: AppInfo) = runCatching {
        launcherApps.startMainActivity(app.component, app.user, null, null)
    }.onFailure { Log.w(TAG, "Gagal membuka ${app.component}", it) }

    fun openAppInfo(app: AppInfo) = runCatching {
        launcherApps.startAppDetailsActivity(app.component, app.user, null, null)
    }.onFailure { Log.w(TAG, "Gagal membuka info ${app.component}", it) }

    private companion object { const val TAG = "AppRepository" }
}
