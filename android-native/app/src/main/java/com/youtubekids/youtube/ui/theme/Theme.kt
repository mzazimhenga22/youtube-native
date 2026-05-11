@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
private val DarkColorScheme = darkColorScheme(
    primary = Red,
    secondary = White,
    tertiary = Gray,
    background = Black,
    surface = DarkGray,
    onPrimary = White,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun YouTubeTVTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
