@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MultiviewGrid(
    streams: List<Video>,
    modifier: Modifier = Modifier
) {
    val displayStreams = streams.take(4)

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.weight(1f)) {
            StreamCard(displayStreams.getOrNull(0), Modifier.weight(1f).fillMaxHeight())
            StreamCard(displayStreams.getOrNull(1), Modifier.weight(1f).fillMaxHeight())
        }
        Row(modifier = Modifier.weight(1f)) {
            StreamCard(displayStreams.getOrNull(2), Modifier.weight(1f).fillMaxHeight())
            StreamCard(displayStreams.getOrNull(3), Modifier.weight(1f).fillMaxHeight())
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF00FF99), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Audio syncs with focused stream",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(modifier = Modifier.size(4.dp).background(Color.DarkGray, CircleShape))
                Text(
                    "Press OK for Fullscreen",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StreamCard(video: Video?, modifier: Modifier = Modifier) {
    if (video == null) {
        Box(modifier = modifier.padding(12.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp)))
        return
    }

    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = { /* TODO */ },
        modifier = modifier
            .padding(12.dp)
            .onFocusChanged { isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(32.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(4.dp, Color.White))),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = video.thumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = if (isFocused) 0.2f else 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isFocused) 0.6f else 0.2f)
                            )
                        )
                    )
            )

            // Live Badge (Top Left)
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.TopStart)
                    .background(if (isFocused) Color.Red else Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("LIVE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }

            if (isFocused) {
                // Audio Indicator (Top Right)
                Box(
                    modifier = Modifier
                        .padding(20.dp)
                        .align(Alignment.TopEnd)
                        .background(Color(0xFF00FF99), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AUDIO", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Info (Bottom)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = video.channel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Audio Focus Bar (Bottom)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFF00FF99))
                )
            }
        }
    }
}
