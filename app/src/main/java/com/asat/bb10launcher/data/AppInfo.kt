package com.asat.bb10launcher.data

import android.content.ComponentName
import android.os.UserHandle
import androidx.compose.ui.graphics.ImageBitmap

data class AppInfo(
    val label: String,
    val component: ComponentName,
    val user: UserHandle,
    val icon: ImageBitmap,
) {
    val key: String get() = component.flattenToString() + "#" + user.toString()
}
