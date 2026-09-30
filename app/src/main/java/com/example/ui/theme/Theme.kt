package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val CoachFitColorScheme = darkColorScheme(
    primary = NeonGreen, // Pure White #FFFFFF
    onPrimary = Color.Black, // High-contrast Black on Pure White CTA
    primaryContainer = NeonGreenContainer,
    onPrimaryContainer = OnNeonGreenContainer,

    secondary = AthleticOrange, // Silver Slate #E2E8F0
    onSecondary = Color.Black,
    secondaryContainer = AthleticOrangeContainer,
    onSecondaryContainer = OnAthleticOrangeContainer,

    tertiary = AthleticCyan, // Muted Silver #CBD5E1
    onTertiary = Color.Black,
    tertiaryContainer = AthleticCyanContainer,
    onTertiaryContainer = AthleticCyan,

    background = DarkBackground, // Pure Deep Black Canvas (#000000)
    onBackground = TextPrimary, // White Type (#FFFFFF)

    surface = DarkSurface, // Light Charcoal (#22232A)
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant, // Elevated Light Charcoal (#2C2E38)
    onSurfaceVariant = TextSecondary,

    outline = DividerDark, // Crisp Charcoal Outline (#3E4150)
    outlineVariant = Color(0xFF4A4D5E)
)

val CoachFitShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun CoachFitTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkBackground.toArgb()
                window.navigationBarColor = DarkBackground.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = CoachFitColorScheme,
        shapes = CoachFitShapes,
        typography = Typography,
        content = content
    )
}
