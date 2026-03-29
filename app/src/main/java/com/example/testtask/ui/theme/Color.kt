package com.example.testtask.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

private val SwYellow = Color(0xFFFFE81F)
private val SpaceBlack = Color(0xFF0A0A12)
private val SpaceSurface = Color(0xFF121218)
private val SpaceSurfaceVariant = Color(0xFF1A1A24)
private val OnSpaceLight = Color(0xFFE8E8EC)
private val OnSpaceMuted = Color(0xFFB0B0BC)
private val MutedGold = Color(0xFFC4B896)

val StarWarsColorScheme = darkColorScheme(
    primary = SwYellow,
    onPrimary = SpaceBlack,
    primaryContainer = Color(0xFF3D3818),
    onPrimaryContainer = SwYellow,
    secondary = MutedGold,
    onSecondary = SpaceBlack,
    secondaryContainer = Color(0xFF2A2820),
    onSecondaryContainer = MutedGold,
    tertiary = Color(0xFF8A9BB8),
    onTertiary = SpaceBlack,
    background = SpaceBlack,
    onBackground = OnSpaceLight,
    surface = SpaceSurface,
    onSurface = OnSpaceLight,
    surfaceVariant = SpaceSurfaceVariant,
    onSurfaceVariant = OnSpaceMuted,
    outline = Color(0xFF5A5A68),
    outlineVariant = Color(0xFF383844),
    inverseSurface = OnSpaceLight,
    inverseOnSurface = SpaceBlack,
    inversePrimary = Color(0xFF5C5610),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)
