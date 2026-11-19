package com.asat.bb10launcher.ui.hub

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

/** Ikon per package dari daftar app launcher (sudah memakai icon pack yang aktif). */
val LocalAppIcons = staticCompositionLocalOf<Map<String, ImageBitmap>> { emptyMap() }

/** Cadangan: ikon sistem untuk package yang tidak punya ikon launcher. */
object AppIconCache {
    private val cache = HashMap<String, ImageBitmap?>()

    fun get(context: Context, pkg: String, sizePx: Int): ImageBitmap? = cache.getOrPut(pkg) {
        runCatching {
            context.packageManager.getApplicationIcon(pkg).toBitmap(sizePx, sizePx).asImageBitmap()
        }.getOrNull()
    }
}

@Composable
fun rememberAppIcon(pkg: String): ImageBitmap? {
    LocalAppIcons.current[pkg]?.let { return it }
    val context = LocalContext.current
    val px = with(LocalDensity.current) { 48.dp.roundToPx() }
    return remember(pkg) { AppIconCache.get(context, pkg, px) }
}
