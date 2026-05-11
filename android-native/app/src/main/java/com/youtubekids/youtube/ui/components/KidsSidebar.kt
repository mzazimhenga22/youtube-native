@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun KidsSidebar(
    onNavigate: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(Icons.Default.Tv, Color(0xFFFF5C5C), "Shows"),
        Triple(Icons.Default.PlayArrow, Color(0xFF3B82F6), "Explore"),
        Triple(Icons.Default.MusicNote, Color(0xFFA855F7), "Music"),
        Triple(Icons.Default.Edit, Color(0xFF10B981), "Learning")
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(Color.White)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEach { (icon, color, label) ->
                var isFocused by remember { mutableStateOf(false) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        onClick = { onNavigate(label.lowercase()) },
                        modifier = Modifier.size(48.dp).onFocusChanged { isFocused = it.isFocused },
                        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF3F4F6)
                        ),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(
                                androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700))
                            )
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = color,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(
                        label,
                        color = if (isFocused) Color.Black else Color(0xFF9CA3AF),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Surface(
            onClick = onSettingsClick,
            modifier = Modifier.size(40.dp),
            shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color(0xFFF3F4F6),
                focusedContainerColor = Color(0xFFE5E7EB)
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF71717A),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
