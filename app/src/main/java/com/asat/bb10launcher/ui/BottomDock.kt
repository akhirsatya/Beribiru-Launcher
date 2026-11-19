package com.asat.bb10launcher.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.asat.bb10launcher.R

/** Tiga aksi tetap di bawah: Telepon | Search | Kamera (ikon PNG custom di res/drawable-nodpi) */
@Composable
fun BottomDock(onPhone: () -> Unit, onSearch: () -> Unit, onCamera: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DockButton(R.drawable.ic_dock_call, "Phone", onPhone)
        DockButton(R.drawable.ic_dock_search, "Search", onSearch)
        DockButton(R.drawable.ic_dock_camera, "Camera", onCamera)
    }
}

@Composable
private fun DockButton(@DrawableRes icon: Int, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) {
        // Image (bukan Icon) supaya warna asli PNG dipakai tanpa di-tint
        Image(painterResource(icon), contentDescription = description, modifier = Modifier.size(30.dp))
    }
}
