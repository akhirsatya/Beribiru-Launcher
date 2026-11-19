package com.asat.bb10launcher.ui.hub

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * State animasi "peek" ala BB10.
 *
 * Lapisan home (grid app) digeser ke kanan sejauh [offset] piksel,
 * memperlihatkan Hub yang diam di bawahnya.
 *  - offset = 0      -> Hub tertutup
 *  - offset = width  -> Hub terbuka penuh
 */
@Stable
class HubRevealState internal constructor(private val scope: CoroutineScope) {

    var offset by mutableFloatStateOf(0f)
        private set

    var width by mutableFloatStateOf(0f)

    val progress: Float get() = if (width > 0f) offset / width else 0f

    private var animJob: Job? = null

    /** Geser manual (dari jari). Mengembalikan delta yang benar-benar terpakai. */
    fun dragBy(delta: Float): Float {
        animJob?.cancel()
        val old = offset
        offset = (offset + delta).coerceIn(0f, width)
        return offset - old
    }

    /** Dipanggil saat jari dilepas: tentukan buka penuh atau tutup. */
    fun settle(velocity: Float) {
        val target = when {
            velocity > FLING_VELOCITY -> width
            velocity < -FLING_VELOCITY -> 0f
            offset > width * OPEN_THRESHOLD -> width
            else -> 0f
        }
        animateTo(target, velocity)
    }

    fun open() = animateTo(width, 0f)
    fun close() = animateTo(0f, 0f)

    private fun animateTo(target: Float, velocity: Float) {
        animJob?.cancel()
        animJob = scope.launch {
            animate(
                initialValue = offset,
                targetValue = target,
                initialVelocity = velocity,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ) { value, _ -> offset = value.coerceIn(0f, width) }
        }
    }

    /**
     * Dipasang di HorizontalPager halaman app:
     *  - sisa swipe ke kanan saat pager sudah di halaman pertama -> buka Hub
     *  - swipe ke kiri saat Hub sedang terbuka sebagian -> tutup Hub dulu
     */
    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
            if (source == NestedScrollSource.UserInput && offset > 0f && available.x < 0f) {
                Offset(dragBy(available.x), 0f)
            } else Offset.Zero

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
            if (source == NestedScrollSource.UserInput && available.x > 0f) {
                Offset(dragBy(available.x), 0f)
            } else Offset.Zero

        override suspend fun onPreFling(available: Velocity): Velocity =
            if (offset > 0f) {
                settle(available.x)
                available
            } else Velocity.Zero
    }

    private companion object {
        const val FLING_VELOCITY = 1200f
        const val OPEN_THRESHOLD = 0.35f
    }
}

@Composable
fun rememberHubRevealState(): HubRevealState {
    val scope = rememberCoroutineScope()
    return remember { HubRevealState(scope) }
}
