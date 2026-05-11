@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White.copy(alpha = 0.1f),
    focusedContainerColor: Color = Color.White,
    contentColor: Color = Color.Unspecified,
    focusedContentColor: Color = Color.Black
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = containerColor,
            focusedContainerColor = focusedContainerColor,
            contentColor = contentColor,
            focusedContentColor = focusedContentColor
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    focusedContainerColor: Color = Color.White.copy(alpha = 0.2f),
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = containerColor,
            focusedContainerColor = focusedContainerColor
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
