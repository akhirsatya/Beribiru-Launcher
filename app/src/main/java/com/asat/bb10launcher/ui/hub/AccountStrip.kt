package com.asat.bb10launcher.ui.hub

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asat.bb10launcher.hub.HubItem

/**
 * Strip vertikal ikon akun + jumlah pesan belum dibaca,
 * muncul di tepi lapisan home saat Hub di-"peek".
 */
@Composable
fun AccountStrip(
    notifications: List<HubItem>,
    readKeys: Set<String>,
    onAccountClick: (String?) -> Unit,
    topInset: Dp,
    modifier: Modifier = Modifier,
) {
    // groupBy mempertahankan urutan -> akun dengan pesan terbaru di atas
    val accounts = remember(notifications, readKeys) {
        notifications.groupBy { it.packageName }
            .map { (pkg, list) -> pkg to list.count { it.readKey !in readKeys } }
            .take(7)
    }

    Column(
        modifier
            .background(Color(0xE61C1C1C))
            .padding(top = topInset + 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            Icons.Outlined.Inbox,
            contentDescription = "All",
            tint = Color.White,
            modifier = Modifier.size(24.dp).clickable { onAccountClick(null) },
        )
        accounts.forEach { (pkg, unread) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onAccountClick(pkg) }.padding(2.dp),
            ) {
                Box {
                    val icon = rememberAppIcon(pkg)
                    if (icon != null) Image(icon, null, Modifier.size(28.dp))
                    if (unread > 0) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 3.dp, y = (-3).dp)
                                .size(9.dp)
                                .background(Color(0xFFE53935), CircleShape)
                        )
                    }
                }
                Text(
                    if (unread > 0) unread.toString() else "",
                    color = Color.White,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
