package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = TeakPrimary,
    onPrimary = TeakOnPrimary,
    primaryContainer = TeakPrimaryContainer,
    onPrimaryContainer = TeakOnPrimaryContainer,
    secondary = ForestSecondary,
    onSecondary = ForestOnSecondary,
    secondaryContainer = ForestSecondaryContainer,
    onSecondaryContainer = ForestOnSecondaryContainer,
    tertiary = BrassTertiary,
    onTertiary = BrassOnTertiary,
    tertiaryContainer = BrassTertiaryContainer,
    onTertiaryContainer = BrassOnTertiaryContainer,
    background = ParchmentBackground,
    onBackground = InkOnSurface,
    surface = IvorySurface,
    onSurface = InkOnSurface,
    surfaceVariant = SandSurfaceLow,
    onSurfaceVariant = BarkOnSurfaceVariant,
    surfaceContainerLowest = IvorySurface,
    surfaceContainerLow = SandSurfaceLow,
    surfaceContainer = LinenSurfaceContainer,
    surfaceContainerHigh = WarmSurfaceHigh,
    outline = BarkOutline,
    outlineVariant = GrainOutlineVariant,
    error = TerracottaError,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = CedarPrimaryDark,
    onPrimary = OnCedarPrimaryDark,
    primaryContainer = CedarPrimaryContainerDark,
    onPrimaryContainer = OnCedarPrimaryContainerDark,
    secondary = MossSecondaryDark,
    onSecondary = OnMossSecondaryDark,
    secondaryContainer = MossSecondaryContainerDark,
    onSecondaryContainer = OnMossSecondaryContainerDark,
    tertiary = GoldTertiaryDark,
    onTertiary = OnGoldTertiaryDark,
    tertiaryContainer = GoldTertiaryContainerDark,
    onTertiaryContainer = OnGoldTertiaryContainerDark,
    background = WalnutBackgroundDark,
    onBackground = CreamOnSurfaceDark,
    surface = WalnutSurfaceDark,
    onSurface = CreamOnSurfaceDark,
    surfaceVariant = WalnutSurfaceLowDark,
    onSurfaceVariant = SandOnSurfaceVariantDark,
    surfaceContainerLowest = WalnutBackgroundDark,
    surfaceContainerLow = WalnutSurfaceLowDark,
    surfaceContainer = SmokedOakContainerDark,
    surfaceContainerHigh = WalnutSurfaceHighDark,
    outline = BarkOutlineDark,
    outlineVariant = GrainOutlineVariantDark,
    error = TerracottaErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

val KathHisabShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun KathHisabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KathHisabTypography,
        shapes = KathHisabShapes,
        content = content
    )
}
