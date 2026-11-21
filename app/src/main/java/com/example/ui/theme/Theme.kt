package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ViralClipDarkColorScheme = darkColorScheme(
    primary = NeonCrimson,
    onPrimary = TextPrimary,
    primaryContainer = ColorMix.CrimsonContainer,
    onPrimaryContainer = TextPrimary,
    secondary = ElectricCyan,
    onSecondary = VoidBlack,
    secondaryContainer = ColorMix.CyanContainer,
    onSecondaryContainer = ElectricCyan,
    tertiary = VoltYellow,
    onTertiary = VoidBlack,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = CarbonSurface,
    onSurface = TextPrimary,
    surfaceVariant = SmokedGlass,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = VoidBlack,
    surfaceContainerLow = ObsidianBg,
    surfaceContainer = CarbonSurface,
    surfaceContainerHigh = SmokedGlass,
    surfaceContainerHighest = ElevatedGlass,
    outline = GlassBorder,
    outlineVariant = GlassBorderBright,
    error = ErrorNeon,
    onError = VoidBlack
)

private object ColorMix {
    val CrimsonContainer = androidx.compose.ui.graphics.Color(0xFF3A1021)
    val CyanContainer = androidx.compose.ui.graphics.Color(0xFF0A2A35)
}

val ViralClipShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ViralClipXTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ViralClipDarkColorScheme,
        typography = ViralClipTypography,
        shapes = ViralClipShapes,
        content = content
    )
}
