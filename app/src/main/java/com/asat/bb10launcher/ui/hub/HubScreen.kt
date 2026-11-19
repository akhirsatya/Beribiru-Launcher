package com.asat.bb10launcher.ui.hub

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asat.bb10launcher.hub.HubItem
import com.asat.bb10launcher.ui.theme.Bb10Blue
import com.asat.bb10launcher.ui.theme.Bb10DateBar
import com.asat.bb10launcher.ui.theme.Bb10Divider
import com.asat.bb10launcher.ui.theme.Bb10MenuBg
import com.asat.bb10launcher.ui.theme.Bb10TextDark
import com.asat.bb10launcher.ui.theme.Bb10TextGray
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val HubBlue = Bb10Blue

private val dayFormat = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val Light = FontFamily.SansSerif
private val GrayscaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

/**
 * Hub ala BB10.3 (tema terang):
 *  - title bar putih dengan judul biru + panah dropdown (buka daftar akun)
 *  - date bar abu untuk setiap hari
 *  - ikon berwarna = belum dibaca, ikon monokrom = sudah dibaca
 *  - action bar putih: Accounts | Search | (Compose) | Mark read | More
 *  - daftar akun meluncur dari kiri, action menu dari kanan (panel gelap)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HubScreen(
    notifications: List<HubItem>,
    readKeys: Set<String>,
    connected: Boolean,
    filterPackage: String?,
    onFilterChange: (String?) -> Unit,
    onOpen: (HubItem) -> Unit,
    onMarkAllRead: () -> Unit,
    onEnableAccess: () -> Unit,
    onCompose: () -> Unit,
    accountsOpen: Boolean,
    onAccountsOpenChange: (Boolean) -> Unit,
    topInset: Dp,
    modifier: Modifier = Modifier,
) {
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var menuOpen by rememberSaveable { mutableStateOf(false) }

    val visible = remember(notifications, filterPackage, query) {
        val q = query.trim()
        notifications.filter {
            (filterPackage == null || it.packageName == filterPackage) &&
                (q.isEmpty() || it.title.contains(q, true) || it.text.contains(q, true))
        }
    }
    val grouped = remember(visible) { visible.groupBy { it.postTime.toLocalDate() } }
    val title = filterPackage
        ?.let { pkg -> notifications.firstOrNull { it.packageName == pkg }?.appLabel }
        ?: "Hub"
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BackHandler(enabled = menuOpen) { menuOpen = false }
    BackHandler(enabled = searching && !menuOpen) { searching = false; query = "" }

    Box(modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            // Title bar
            Column(Modifier.fillMaxWidth().background(Color.White).padding(top = topInset)) {
                if (searching) {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search", color = Bb10TextGray) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = Bb10TextGray) },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Close, "Close search", tint = Bb10TextGray,
                                modifier = Modifier.clickable { searching = false; query = "" },
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Bb10TextDark,
                            unfocusedTextColor = Bb10TextDark,
                            cursorColor = Bb10Blue,
                            focusedIndicatorColor = Bb10Blue,
                            unfocusedIndicatorColor = Bb10Divider,
                        ),
                    )
                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { onAccountsOpenChange(true) }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            title,
                            color = Bb10Blue,
                            fontSize = 22.sp,
                            fontFamily = Light,
                            fontWeight = FontWeight.Light,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(Icons.Filled.ArrowDropDown, null, tint = Bb10Blue)
                    }
                    HorizontalDivider(color = Bb10Divider)
                }
            }

            // Daftar pesan
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    !connected -> EnableAccess(onEnableAccess, Modifier.align(Alignment.Center))
                    visible.isEmpty() -> Text(
                        if (query.isBlank()) "No messages" else "No results",
                        color = Bb10TextGray,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    else -> LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = ACTION_BAR_TOTAL_HEIGHT + navBottom + 8.dp),
                    ) {
                        grouped.forEach { (day, list) ->
                            stickyHeader(key = "day-$day") { DateBar(day) }
                            items(list, key = { it.readKey }) { item ->
                                HubRow(item, unread = item.readKey !in readKeys, onClick = { onOpen(item) })
                            }
                        }
                    }
                }
            }
        }

        Bb10ActionBar(
            left = listOf(
                ActionSpec(Icons.Filled.Menu, "Accounts") { onAccountsOpenChange(true) },
                ActionSpec(Icons.Filled.Search, "Search") { searching = true },
            ),
            signature = ActionSpec(Icons.Filled.Create, "Compose", onCompose),
            right = listOf(
                ActionSpec(Icons.Outlined.DoneAll, "Mark read", onMarkAllRead),
                ActionSpec(Icons.Filled.MoreVert, "More") { menuOpen = true },
            ),
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        // Daftar akun (tab menu) dari kiri
        SidePanel(visible = accountsOpen, fromLeft = true, onDismiss = { onAccountsOpenChange(false) }) {
            AccountMenu(
                notifications = notifications,
                readKeys = readKeys,
                selected = filterPackage,
                topInset = topInset,
                onSelect = { pkg -> onFilterChange(pkg); onAccountsOpenChange(false) },
            )
        }

        // Action menu (overflow) dari kanan
        SidePanel(visible = menuOpen, fromLeft = false, onDismiss = { menuOpen = false }) {
            Column(Modifier.fillMaxHeight().padding(top = topInset + 8.dp)) {
                MenuItem(Icons.Outlined.DoneAll, "Mark all read") { onMarkAllRead(); menuOpen = false }
                MenuItem(Icons.Filled.Search, "Search") { searching = true; menuOpen = false }
                MenuItem(Icons.AutoMirrored.Filled.Send, "Compose") { onCompose(); menuOpen = false }
                MenuItem(Icons.Outlined.NotificationsActive, "Notification access") { onEnableAccess(); menuOpen = false }
            }
        }
    }
}

@Composable
private fun DateBar(day: LocalDate) {
    Text(
        day.format(dayFormat),
        color = Color(0xFF4A4A4A),
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Bb10DateBar)
            .padding(horizontal = 16.dp, vertical = 5.dp),
    )
}

@Composable
private fun HubRow(item: HubItem, unread: Boolean, onClick: () -> Unit) {
    Column(Modifier.background(Color.White)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            val icon = rememberAppIcon(item.packageName)
            Box(Modifier.padding(top = 2.dp).size(32.dp)) {
                if (icon != null) {
                    Image(
                        icon, null,
                        modifier = Modifier.size(32.dp),
                        // BB10: ikon berwarna = belum dibaca, monokrom = sudah dibaca
                        colorFilter = if (unread) null else GrayscaleFilter,
                        alpha = if (unread) 1f else 0.55f,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        color = Bb10TextDark,
                        fontSize = 16.sp,
                        fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        Instant.ofEpochMilli(item.postTime).atZone(ZoneId.systemDefault()).format(timeFormat),
                        color = Bb10TextGray,
                        fontSize = 12.sp,
                    )
                }
                if (item.text.isNotBlank()) {
                    Text(
                        item.text,
                        color = Bb10TextGray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        HorizontalDivider(Modifier.padding(start = 60.dp), color = Bb10Divider)
    }
}

/** Panel gelap yang meluncur dari sisi kiri/kanan, dengan scrim di belakangnya. */
@Composable
private fun SidePanel(
    visible: Boolean,
    fromLeft: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x66000000))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
        )
    }
    Box(Modifier.fillMaxSize(), contentAlignment = if (fromLeft) Alignment.TopStart else Alignment.TopEnd) {
        AnimatedVisibility(
            visible,
            enter = slideInHorizontally { if (fromLeft) -it else it },
            exit = slideOutHorizontally { if (fromLeft) -it else it },
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(if (fromLeft) 260.dp else 220.dp)
                    .background(Bb10MenuBg)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                    .navigationBarsPadding()
            ) { content() }
        }
    }
}

@Composable
private fun AccountMenu(
    notifications: List<HubItem>,
    readKeys: Set<String>,
    selected: String?,
    topInset: Dp,
    onSelect: (String?) -> Unit,
) {
    val accounts = remember(notifications, readKeys) {
        notifications.groupBy { it.packageName }.map { (pkg, list) ->
            Triple(pkg, list.first().appLabel, list.count { it.readKey !in readKeys })
        }
    }
    val totalUnread = accounts.sumOf { it.third }

    Column(Modifier.fillMaxHeight().padding(top = topInset + 8.dp)) {
        AccountRow(null, "Hub", totalUnread, selected == null) { onSelect(null) }
        HorizontalDivider(color = Color(0xFF3D3D3D))
        LazyColumn {
            items(accounts, key = { it.first }) { (pkg, label, unread) ->
                AccountRow(pkg, label, unread, selected == pkg) { onSelect(pkg) }
            }
        }
    }
}

@Composable
private fun AccountRow(pkg: String?, label: String, unread: Int, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (selected) Color(0xFF3A3A3A) else Color.Transparent)
            .clickable(onClick = onClick)
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(4.dp).fillMaxHeight().background(if (selected) Bb10Blue else Color.Transparent))
        Spacer(Modifier.width(12.dp))
        if (pkg == null) {
            Icon(Icons.Outlined.Inbox, null, tint = Color.White, modifier = Modifier.size(28.dp))
        } else {
            rememberAppIcon(pkg)?.let { Image(it, null, Modifier.size(28.dp)) } ?: Spacer(Modifier.size(28.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = Color.White, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        if (unread > 0) Text("$unread", color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp)
        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun MenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).height(52.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = Color.White, fontSize = 15.sp)
    }
}

@Composable
private fun EnableAccess(onEnable: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Allow notification access so Hub can collect your messages in one place.",
            color = Bb10TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onEnable, colors = ButtonDefaults.buttonColors(containerColor = Bb10Blue)) {
            Text("Enable notification access", color = Color.White)
        }
    }
}

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
