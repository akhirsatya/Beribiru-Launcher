package com.asat.bb10launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asat.bb10launcher.data.AppInfo

/**
 * Grid 4 x 6 tetap seperti BB10: slot kosong tetap memakan tempat.
 * Long-press di area kosong (di luar ikon) -> [onEmptyLongClick] (pilih icon pack).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGrid(
    apps: List<AppInfo>,
    onClick: (AppInfo) -> Unit,
    onLongClick: (AppInfo) -> Unit,
    onEmptyLongClick: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        repeat(GRID_ROWS) { row ->
            Row(Modifier.weight(1f).fillMaxWidth()) {
                repeat(GRID_COLUMNS) { col ->
                    val app = apps.getOrNull(row * GRID_COLUMNS + col)
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .combinedClickable(
                                interactionSource = null,
                                indication = null,
                                onClick = {},
                                onLongClick = onEmptyLongClick,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (app != null) {
                            AppIcon(app, onClick = { onClick(app) }, onLongClick = { onLongClick(app) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(app: AppInfo, onClick: () -> Unit, onLongClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(4.dp),
    ) {
        Image(bitmap = app.icon, contentDescription = app.label, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(6.dp))
        Text(
            text = app.label,
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = Shadow(Color.Black, Offset(0f, 1f), 4f)),
            modifier = Modifier.widthIn(max = 80.dp),
        )
    }
}
