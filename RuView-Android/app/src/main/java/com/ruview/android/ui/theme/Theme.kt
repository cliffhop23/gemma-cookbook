package com.ruview.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.Black,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = Primary,
    secondary = StatusGreen,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003322),
    onSecondaryContainer = StatusGreen,
    tertiary = StatusBlue,
    onTertiary = Color.Black,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = StatusRed,
    onError = Color.White,
    outline = CardBorder,
)

@Composable
fun RuViewTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
