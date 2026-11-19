package com.asat.bb10launcher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asat.bb10launcher.iconpack.IconPackInfo
import com.asat.bb10launcher.ui.theme.Bb10Blue

/** Pengaturan home screen. Dibuka dengan long-press di area kosong home screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPackSheet(
    packs: List<IconPackInfo>,
    selected: String?,
    onSelect: (String?) -> Unit,
    bb10StatusBar: Boolean,
    onBb10StatusBarChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF1E1E1E)) {
        Text(
            "Home screen",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        LazyColumn {
            item {
                ListItem(
                    headlineContent = { Text("BB10 status bar", color = Color.White) },
                    supportingContent = {
                        Text("Clock in the middle, hides the Android status bar", color = Color.White.copy(alpha = 0.6f))
                    },
                    trailingContent = {
                        Switch(
                            checked = bb10StatusBar,
                            onCheckedChange = onBb10StatusBarChange,
                            colors = SwitchDefaults.colors(checkedTrackColor = Bb10Blue),
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onBb10StatusBarChange(!bb10StatusBar) },
                )
                HorizontalDivider(color = Color(0xFF333333))
                Text(
                    "Icon pack",
                    color = Bb10Blue,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 4.dp),
                )
            }
            item {
                PackRow(label = "System icons", icon = null, selected = selected == null) { onSelect(null) }
            }
            items(packs, key = { it.packageName }) { pack ->
                PackRow(pack.label, pack.icon, selected == pack.packageName) { onSelect(pack.packageName) }
            }
            if (packs.isEmpty()) {
                item {
                    Text(
                        "No icon packs installed. Any pack that supports Nova, ADW or Apex will work.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    )
                }
            }
            item { Spacer(Modifier.navigationBarsPadding().padding(bottom = 16.dp)) }
        }
    }
}

@Composable
private fun PackRow(label: String, icon: ImageBitmap?, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label, color = Color.White) },
        leadingContent = {
            if (icon != null) Image(icon, null, Modifier.size(40.dp))
            else Icon(Icons.Outlined.Apps, null, tint = Color.White, modifier = Modifier.size(40.dp))
        },
        trailingContent = {
            if (selected) Icon(Icons.Filled.Check, "Selected", tint = Bb10Blue)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}
