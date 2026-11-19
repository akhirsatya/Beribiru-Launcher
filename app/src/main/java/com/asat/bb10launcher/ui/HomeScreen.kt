package com.asat.bb10launcher.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asat.bb10launcher.hub.HubStore
import com.asat.bb10launcher.hub.open
import com.asat.bb10launcher.ui.hub.AccountStrip
import com.asat.bb10launcher.ui.hub.HubScreen
import com.asat.bb10launcher.ui.hub.LocalAppIcons
import com.asat.bb10launcher.ui.hub.rememberHubRevealState
import com.asat.bb10launcher.ui.statusbar.Bb10StatusBar
import com.asat.bb10launcher.ui.statusbar.STATUS_BAR_HEIGHT
import com.asat.bb10launcher.ui.theme.Bb10TextDark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val GRID_COLUMNS = 4
const val GRID_ROWS = 6
const val APPS_PER_PAGE = GRID_COLUMNS * GRID_ROWS

private val STRIP_WIDTH = 56.dp

/**
 * Struktur lapisan (bawah -> atas):
 *  1. Hub (putih, diam di tempat, hanya digambar di area yang sudah terbuka) + scrim gelap saat peek
 *  2. Lapisan home: grid app + indikator + dock, digeser ke kanan saat peek
 *  3. Strip akun, menempel di tepi kiri lapisan home
 *  4. Status bar ala BB10 (global, tidak ikut bergeser)
 */
@Composable
fun HomeScreen(
    homeEvents: Flow<Unit>,
    vm: HomeViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val apps by vm.apps.collectAsStateWithLifecycle()
    val hubItems by HubStore.items.collectAsStateWithLifecycle()
    val readKeys by HubStore.readKeys.collectAsStateWithLifecycle()
    val hubConnected by HubStore.connected.collectAsStateWithLifecycle()
    val bb10StatusBar by vm.bb10StatusBar.collectAsStateWithLifecycle()

    val appPages = remember(apps) { apps.chunked(APPS_PER_PAGE).ifEmpty { listOf(emptyList()) } }
    val pagerState = rememberPagerState { appPages.size }
    val reveal = rememberHubRevealState()

    val iconPacks by vm.iconPacks.collectAsStateWithLifecycle()
    val selectedIconPack by vm.selectedIconPack.collectAsStateWithLifecycle()
    var iconSheetOpen by rememberSaveable { mutableStateOf(false) }

    // Ikon per package (sudah ter-theme icon pack) untuk Hub & strip akun
    val iconsByPackage = remember(apps) {
        apps.groupBy { it.component.packageName }.mapValues { it.value.first().icon }
    }

    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var hubFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var accountsOpen by rememberSaveable { mutableStateOf(false) }

    val hubVisible by remember { derivedStateOf { reveal.offset > 0f } }
    val stripVisible by remember { derivedStateOf { reveal.offset > 0f && reveal.progress < 0.98f } }
    val hubMostlyOpen by remember { derivedStateOf { reveal.progress > 0.5f } }
    val hasUnread = remember(hubItems, readKeys) { hubItems.any { it.readKey !in readKeys } }

    // Tombol Home -> tutup Hub & search, kembali ke halaman app pertama
    LaunchedEffect(Unit) {
        homeEvents.collect {
            searchOpen = false
            accountsOpen = false
            reveal.close()
            pagerState.animateScrollToPage(0)
        }
    }
    // Back: tutup daftar akun dulu, lalu Hub
    BackHandler(enabled = hubVisible) { reveal.close() }
    BackHandler(enabled = accountsOpen) { accountsOpen = false }

    // Filter & panel akun di-reset setiap Hub tertutup penuh
    LaunchedEffect(hubVisible) { if (!hubVisible) { hubFilter = null; accountsOpen = false } }

    // Status bar: sembunyikan milik Android bila status bar BB10 aktif
    val view = LocalView.current
    LaunchedEffect(bb10StatusBar, hubMostlyOpen) {
        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, view)
        if (bb10StatusBar) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
        controller.isAppearanceLightStatusBars = hubMostlyOpen
    }

    // Jarak atas konten: cutout kamera (+ status bar BB10) atau status bar Android
    val safeTop = WindowInsets.safeDrawing.only(WindowInsetsSides.Top).asPaddingValues().calculateTopPadding()
    val topInset = if (bb10StatusBar) safeTop + STATUS_BAR_HEIGHT else safeTop

    CompositionLocalProvider(LocalAppIcons provides iconsByPackage) {
    Box(Modifier.fillMaxSize().onSizeChanged { reveal.width = it.width.toFloat() }) {

        // 1. Hub
        if (hubVisible) {
            HubScreen(
                notifications = hubItems,
                readKeys = readKeys,
                connected = hubConnected,
                filterPackage = hubFilter,
                onFilterChange = { hubFilter = it },
                onOpen = { item -> HubStore.markRead(item); item.open(context) },
                onMarkAllRead = HubStore::markAllRead,
                onEnableAccess = {
                    context.startSafely(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
                onCompose = {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain")
                    context.startSafely(Intent.createChooser(send, "Compose"))
                },
                accountsOpen = accountsOpen,
                onAccountsOpenChange = { accountsOpen = it },
                topInset = topInset,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent { clipRect(right = reveal.offset) { this@drawWithContent.drawContent() } }
                    // swipe kiri -> kembali ke grid; swipe kanan saat Hub penuh -> daftar akun
                    .draggable(
                        state = rememberDraggableState { reveal.dragBy(it) },
                        orientation = Orientation.Horizontal,
                        onDragStopped = { velocity ->
                            if (reveal.progress >= 0.99f && velocity > 600f) accountsOpen = true
                            else reveal.settle(velocity)
                        },
                    ),
            )
            // Hub agak gelap selama peek, terang penuh saat terbuka
            Box(
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val alpha = ((1f - reveal.progress) * 0.45f).coerceIn(0f, 1f)
                        clipRect(right = reveal.offset) { drawRect(Color.Black.copy(alpha = alpha)) }
                    }
            )
        }

        // 2. Lapisan home
        Box(
            Modifier
                .fillMaxSize()
                .offset { IntOffset(reveal.offset.roundToInt(), 0) }
                // scrim tipis supaya label tetap terbaca di wallpaper terang
                .background(Brush.verticalGradient(listOf(Color(0x44000000), Color(0x77000000))))
        ) {
            Column(Modifier.fillMaxSize().padding(top = topInset).navigationBarsPadding()) {
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .nestedScroll(reveal.nestedScrollConnection),
                ) { page ->
                    AppGrid(
                        apps = appPages.getOrNull(page).orEmpty(),
                        onClick = vm::launch,
                        onLongClick = vm::openAppInfo,
                        onEmptyLongClick = {
                            vm.refreshIconPacks()
                            iconSheetOpen = true
                        },
                    )
                }

                PageIndicator(
                    pageCount = pagerState.pageCount,
                    currentPage = pagerState.currentPage,
                    onHubClick = { reveal.open() },
                    onPageClick = { scope.launch { pagerState.animateScrollToPage(it) } },
                )

                BottomDock(
                    onPhone = { context.startSafely(Intent(Intent.ACTION_DIAL)) },
                    onSearch = { searchOpen = true },
                    onCamera = { context.startSafely(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)) },
                )
            }
        }

        // 3. Strip akun (peek)
        if (stripVisible) {
            AccountStrip(
                notifications = hubItems,
                readKeys = readKeys,
                onAccountClick = { pkg -> hubFilter = pkg; reveal.open() },
                topInset = topInset,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(STRIP_WIDTH)
                    .offset { IntOffset((reveal.offset - STRIP_WIDTH.toPx()).roundToInt(), 0) }
                    // memudar saat Hub hampir terbuka penuh
                    .graphicsLayer { alpha = ((1f - reveal.progress) / 0.35f).coerceIn(0f, 1f) },
            )
        }

        if (searchOpen) {
            SearchOverlay(
                apps = apps,
                topInset = topInset,
                onDismiss = { searchOpen = false },
                onLaunch = { searchOpen = false; vm.launch(it) },
            )
        }

        // 4. Status bar BB10 (global)
        if (bb10StatusBar) {
            Bb10StatusBar(
                contentColor = if (hubMostlyOpen && !searchOpen) Bb10TextDark else Color.White,
                hasUnread = hasUnread,
                modifier = Modifier.padding(top = safeTop),
            )
        }

        if (iconSheetOpen) {
            IconPackSheet(
                packs = iconPacks,
                selected = selectedIconPack,
                onSelect = { pkg -> vm.selectIconPack(pkg); iconSheetOpen = false },
                bb10StatusBar = bb10StatusBar,
                onBb10StatusBarChange = vm::setBb10StatusBar,
                onDismiss = { iconSheetOpen = false },
            )
        }
    }
    }
}

private fun Context.startSafely(intent: Intent) {
    try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "Aplikasi tidak ditemukan", Toast.LENGTH_SHORT).show()
    }
}
