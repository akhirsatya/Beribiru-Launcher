package com.asat.bb10launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Warna-warna gaya BB10.3 */
val Bb10Blue = Color(0xFF0092CC)        // aksen & signature action
val Bb10TextDark = Color(0xFF1A1A1A)
val Bb10TextGray = Color(0xFF707070)
val Bb10Divider = Color(0xFFDADADA)
val Bb10DateBar = Color(0xFFE6E6E6)
val Bb10MenuBg = Color(0xFF2B2B2B)      // panel akun & action menu (gelap)

private val Bb10Colors = darkColorScheme(
    primary = Bb10Blue,
    onPrimary = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
)

@Composable
fun Bb10Theme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Bb10Colors, content = content)
}
