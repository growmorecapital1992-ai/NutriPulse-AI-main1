package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NutriPulseColorScheme = darkColorScheme(
    primary = PulseEmerald,
    onPrimary = BioDarkBg,
    primaryContainer = PulseEmeraldDark,
    onPrimaryContainer = TextPrimary,
    secondary = CyberCyan,
    onSecondary = BioDarkBg,
    secondaryContainer = ObsidianCardElevated,
    onSecondaryContainer = TextPrimary,
    background = BioDarkBg,
    onBackground = TextPrimary,
    surface = ObsidianCard,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianBorder,
    error = AlertCoral,
    onError = TextPrimary
)

@Composable
fun NutriPulseTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BioDarkBg.toArgb()
            window.navigationBarColor = BioDarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = NutriPulseColorScheme,
        typography = Typography,
        content = content
    )
}
