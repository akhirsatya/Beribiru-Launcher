package com.asat.bb10launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Indikator halaman ala BB10:
 *  - amplop = Hub (tap untuk membuka)
 *  - titik  = halaman app (yang aktif tampil sebagai nomor dalam lingkaran)
 */
@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    onHubClick: () -> Unit,
    onPageClick: (Int) -> Unit,
) {
    val dim = Color.White.copy(alpha = 0.55f)
    Row(
        Modifier.fillMaxWidth().height(28.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(24.dp).clip(CircleShape).clickable(onClick = onHubClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Email, "Hub", tint = dim, modifier = Modifier.size(14.dp))
        }
        for (page in 0 until pageCount) {
            Box(
                Modifier.size(24.dp).clip(CircleShape).clickable { onPageClick(page) },
                contentAlignment = Alignment.Center,
            ) {
                if (page == currentPage) {
                    Box(Modifier.size(16.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                        Text("${page + 1}", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(Modifier.size(6.dp).background(dim, CircleShape))
                }
            }
        }
    }
}
