package com.asat.bb10launcher.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asat.bb10launcher.ui.theme.Bb10Blue

data class ActionSpec(val icon: ImageVector, val label: String, val onClick: () -> Unit)

/** Tinggi total action bar termasuk lingkaran signature yang menonjol (tanpa nav bar). */
val ACTION_BAR_TOTAL_HEIGHT = 68.dp
private val SIGNATURE_RAISE = 12.dp
private val SIGNATURE_SIZE = 52.dp

/**
 * Action bar ala BB10.3:
 *  - tema terang: latar putih, ikon & label abu gelap
 *  - "signature action" di tengah: lingkaran biru yang menonjol ke atas bar
 *  - aksi sekunder mengapit di kiri & kanan
 */
@Composable
fun Bb10ActionBar(
    left: List<ActionSpec>,
    signature: ActionSpec,
    right: List<ActionSpec>,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    val bg = if (dark) Color(0xFF262626) else Color.White
    val fg = if (dark) Color.White else Color(0xFF3C3C3C)
    val line = if (dark) Color(0xFF3A3A3A) else Color(0xFFD6D6D6)

    Box(modifier.fillMaxWidth()) {
        Column(
            Modifier
                .padding(top = SIGNATURE_RAISE)
                .fillMaxWidth()
                .background(bg)
        ) {
            HorizontalDivider(color = line)
            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                left.forEach { ActionCell(it, fg, Modifier.weight(1f)) }
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                    Text(signature.label, color = fg, fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
                right.forEach { ActionCell(it, fg, Modifier.weight(1f)) }
            }
            Spacer(Modifier.navigationBarsPadding())
        }

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(SIGNATURE_SIZE)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(Bb10Blue)
                .clickable(onClick = signature.onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(signature.icon, signature.label, tint = Color.White, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun ActionCell(spec: ActionSpec, fg: Color, modifier: Modifier) {
    Column(
        modifier.fillMaxHeight().clickable(onClick = spec.onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(spec.icon, spec.label, tint = fg, modifier = Modifier.size(24.dp))
        Text(spec.label, color = fg, fontSize = 10.sp)
    }
}
