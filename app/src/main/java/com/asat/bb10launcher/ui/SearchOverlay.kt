package com.asat.bb10launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.asat.bb10launcher.data.AppInfo

/** Search sederhana (sementara hanya mencari app; kontak & file menyusul). */
@Composable
fun SearchOverlay(
    apps: List<AppInfo>,
    topInset: androidx.compose.ui.unit.Dp,
    onDismiss: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
) {
    BackHandler(onBack = onDismiss)

    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query, apps) {
        val q = query.trim()
        if (q.isEmpty()) emptyList() else apps.filter { it.label.contains(q, ignoreCase = true) }
    }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xF2121212))
            .padding(top = topInset)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { results.firstOrNull()?.let(onLaunch) }),
                modifier = Modifier.weight(1f).focusRequester(focus),
            )
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
        LazyColumn {
            items(results, key = { it.key }) { app ->
                ListItem(
                    headlineContent = { Text(app.label) },
                    leadingContent = { Image(app.icon, contentDescription = null, modifier = Modifier.size(40.dp)) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onLaunch(app) },
                )
            }
        }
    }
}
