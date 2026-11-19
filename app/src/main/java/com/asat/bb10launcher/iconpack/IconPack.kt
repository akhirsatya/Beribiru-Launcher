package com.asat.bb10launcher.iconpack

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.Xml
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Icon pack format ADW / Nova / Apex (appfilter.xml).
 *
 * Yang didukung:
 *  - <item component="ComponentInfo{pkg/cls}" drawable="nama"/>
 *  - <calendar component="..." prefix="cal_"/>  -> ikon kalender sesuai tanggal hari ini
 *  - <iconback img1=".." img2=".."/>, <iconmask img1=".."/>, <iconupon img1=".."/>, <scale factor=".."/>
 *    -> dipakai untuk "membungkus" app yang tidak punya ikon di pack
 */
class IconPack private constructor(
    val packageName: String,
    private val res: Resources,
    private val components: Map<String, String>,
    private val packageFallback: Map<String, String>,
    private val calendars: Map<String, String>,
    private val backs: List<String>,
    private val mask: String?,
    private val upon: String?,
    private val scale: Float,
) {
    private val drawableCache = HashMap<String, Drawable?>()

    /** Ikon dari pack untuk [component], atau null kalau pack tidak bisa menyediakannya. */
    fun iconFor(component: ComponentName, original: Drawable, sizePx: Int): Bitmap? {
        val key = "${component.packageName}/${component.className}"

        calendars[key]?.let { prefix ->
            loadDrawable(prefix + LocalDate.now().dayOfMonth)?.let { return it.toBitmap(sizePx, sizePx) }
        }
        val name = components[key] ?: packageFallback[component.packageName]
        name?.let { loadDrawable(it) }?.let { return it.toBitmap(sizePx, sizePx) }

        return wrapUnthemed(component, original, sizePx)
    }

    /** iconback + ikon asli (diperkecil & di-mask) + iconupon */
    private fun wrapUnthemed(component: ComponentName, original: Drawable, size: Int): Bitmap? {
        if (backs.isEmpty() && mask == null && upon == null) return null

        val out = createBitmap(size, size)
        val canvas = Canvas(out)

        if (backs.isNotEmpty()) {
            loadDrawable(backs[Math.floorMod(component.hashCode(), backs.size)])
                ?.let { canvas.drawBitmap(it.toBitmap(size, size), 0f, 0f, null) }
        }

        val inner = (size * scale).roundToInt().coerceIn(1, size)
        val offset = (size - inner) / 2f
        val layer = createBitmap(size, size)
        val layerCanvas = Canvas(layer)
        layerCanvas.drawBitmap(original.toBitmap(inner, inner), offset, offset, null)
        mask?.let { loadDrawable(it) }?.let {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT) }
            layerCanvas.drawBitmap(it.toBitmap(size, size), 0f, 0f, paint)
        }
        canvas.drawBitmap(layer, 0f, 0f, null)

        upon?.let { loadDrawable(it) }?.let { canvas.drawBitmap(it.toBitmap(size, size), 0f, 0f, null) }
        return out
    }

    @SuppressLint("DiscouragedApi")
    private fun loadDrawable(name: String): Drawable? = drawableCache.getOrPut(name) {
        val id = res.getIdentifier(name, "drawable", packageName).takeIf { it != 0 }
            ?: res.getIdentifier(name, "mipmap", packageName)
        if (id == 0) null else runCatching { ResourcesCompat.getDrawable(res, id, null) }.getOrNull()
    }

    companion object {
        private const val TAG = "IconPack"

        fun load(context: Context, pkg: String): IconPack? = runCatching {
            val res = context.packageManager.getResourcesForApplication(pkg)
            val parser = openAppFilter(res, pkg) ?: return null

            val components = HashMap<String, String>()
            val packageFallback = HashMap<String, String>()
            val calendars = HashMap<String, String>()
            val backs = mutableListOf<String>()
            var mask: String? = null
            var upon: String? = null
            var scale = 1f

            try {
                while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG) {
                        when (parser.name) {
                            "item" -> {
                                val comp = parser.getAttributeValue(null, "component")?.let(::normalize)
                                val drawable = parser.getAttributeValue(null, "drawable")
                                if (comp != null && !drawable.isNullOrBlank()) {
                                    components.putIfAbsent(comp.first, drawable)
                                    packageFallback.putIfAbsent(comp.second, drawable)
                                }
                            }
                            "calendar" -> {
                                val comp = parser.getAttributeValue(null, "component")?.let(::normalize)
                                val prefix = parser.getAttributeValue(null, "prefix")
                                if (comp != null && !prefix.isNullOrBlank()) calendars[comp.first] = prefix
                            }
                            "iconback" -> for (i in 0 until parser.attributeCount) {
                                if (parser.getAttributeName(i).startsWith("img")) backs += parser.getAttributeValue(i)
                            }
                            "iconmask" -> mask = parser.getAttributeValue(null, "img1")
                            "iconupon" -> upon = parser.getAttributeValue(null, "img1")
                            "scale" -> scale = parser.getAttributeValue(null, "factor")?.toFloatOrNull() ?: 1f
                        }
                    }
                    parser.next()
                }
            } finally {
                (parser as? XmlResourceParser)?.close()
            }

            IconPack(pkg, res, components, packageFallback, calendars, backs, mask, upon, scale)
        }.onFailure { Log.w(TAG, "Gagal memuat icon pack $pkg", it) }.getOrNull()

        /** appfilter.xml bisa ada di res/xml atau di assets. */
        @SuppressLint("DiscouragedApi")
        private fun openAppFilter(res: Resources, pkg: String): XmlPullParser? {
            val id = res.getIdentifier("appfilter", "xml", pkg)
            if (id != 0) return res.getXml(id)
            val text = runCatching {
                res.assets.open("appfilter.xml").bufferedReader().use { it.readText() }
            }.getOrNull() ?: return null
            return Xml.newPullParser().apply { setInput(StringReader(text)) }
        }

        /** "ComponentInfo{com.foo/.Main}" -> ("com.foo/com.foo.Main", "com.foo") */
        private fun normalize(raw: String): Pair<String, String>? {
            val inner = raw.trim().removePrefix("ComponentInfo{").removeSuffix("}").trim()
            val slash = inner.indexOf('/')
            if (slash <= 0 || slash == inner.lastIndex) return null
            val pkg = inner.substring(0, slash)
            var cls = inner.substring(slash + 1)
            if (cls.startsWith(".")) cls = pkg + cls
            return "$pkg/$cls" to pkg
        }
    }
}
