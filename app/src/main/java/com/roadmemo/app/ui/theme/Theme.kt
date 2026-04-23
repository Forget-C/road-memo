package com.roadmemo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = RoadMemoPrimary,
    onPrimary = RoadMemoOnPrimary,
    primaryContainer = RoadMemoPrimaryContainer,
    onPrimaryContainer = RoadMemoOnPrimaryContainer,
    secondary = RoadMemoSecondary,
    onSecondary = RoadMemoOnSecondary,
    secondaryContainer = RoadMemoSecondaryContainer,
    onSecondaryContainer = RoadMemoOnSecondaryContainer,
    background = RoadMemoBackground,
    onBackground = RoadMemoOnBackground,
    surface = RoadMemoSurface,
    onSurface = RoadMemoOnSurface,
    surfaceVariant = RoadMemoSurfaceVariant,
    onSurfaceVariant = RoadMemoOnSurfaceVariant,
)

private val DarkColors = darkColorScheme(
    primary = RoadMemoPrimaryContainer,
    onPrimary = RoadMemoOnPrimaryContainer,
    secondary = RoadMemoSecondaryContainer,
    onSecondary = RoadMemoOnSecondaryContainer,
)

@Composable
fun RoadMemoTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = RoadMemoTypography,
        content = content,
    )
}
