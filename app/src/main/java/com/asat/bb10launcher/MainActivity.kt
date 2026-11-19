package com.asat.bb10launcher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.asat.bb10launcher.ui.HomeScreen
import com.asat.bb10launcher.ui.theme.Bb10Theme
import kotlinx.coroutines.flow.MutableSharedFlow

class MainActivity : ComponentActivity() {

    /** Dipancarkan saat tombol Home / Back ditekan ketika launcher sudah terbuka. */
    private val homeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        // Launcher tidak boleh "keluar" lewat Back; kembali ke halaman app pertama saja.
        onBackPressedDispatcher.addCallback(this) { homeEvents.tryEmit(Unit) }

        setContent {
            Bb10Theme {
                HomeScreen(homeEvents = homeEvents)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN) homeEvents.tryEmit(Unit)
    }
}
