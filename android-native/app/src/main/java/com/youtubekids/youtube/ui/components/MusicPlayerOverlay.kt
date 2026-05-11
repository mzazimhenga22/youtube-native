@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
fun MusicPlayerOverlay(
    video: Video,
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    duration: String,
    recommendations: List<Video>,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))) {
        // Clamp progress to valid range
        val safeProgress = progress.coerceIn(0f, 1f)
        // Background Aura
        AsyncImage(
            model = video.thumbnail,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.6f),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))

        // Close Button
        Surface(
            onClick = onClose,
            modifier = Modifier.padding(40.dp).size(56.dp).align(Alignment.TopStart),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.1f),
                contentColor = Color.White,
                focusedContainerColor = Color.White,
                focusedContentColor = Color.Black
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Close, contentDescription = null, tint = LocalContentColor.current)
            }
        }

        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 80.dp), verticalAlignment = Alignment.CenterVertically) {
            // Left Pane: Now Playing
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .shadow(40.dp, RoundedCornerShape(40.dp))
                        .clip(RoundedCornerShape(40.dp))
                        .background(Color.DarkGray)
                ) {
                    AsyncImage(
                        model = video.thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    video.title,
                    color = Color.White,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    lineHeight = 60.sp
                )
                Text(
                    video.channel,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Progress Bar
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(safeProgress)
                                .background(Color.White, CircleShape)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(currentTime, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Black)
                        Text(duration, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Controls
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Using Surface for buttons instead of IconButton
                    Surface(onClick = {}, shape = ClickableSurfaceDefaults.shape(CircleShape), modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.padding(8.dp))
                    }
                    Surface(onClick = { onSeek(-10f) }, shape = ClickableSurfaceDefaults.shape(CircleShape), modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = null, modifier = Modifier.padding(8.dp))
                    }
                    
                    Surface(
                        onClick = onTogglePlay,
                        modifier = Modifier.size(96.dp),
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.White,
                            focusedContainerColor = Color(0xFFF4F4F5)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Surface(onClick = onNext, shape = ClickableSurfaceDefaults.shape(CircleShape), modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.padding(8.dp))
                    }
                    Surface(onClick = {}, shape = ClickableSurfaceDefaults.shape(CircleShape), modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.padding(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(80.dp))

            // Right Pane: Up Next
            Column(modifier = Modifier.width(400.dp).fillMaxHeight().padding(vertical = 40.dp)) {
                Text("Up Next", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(32.dp))
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(40.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(20.dp)
                ) {
                    items(recommendations) { item ->
                        Surface(
                            onClick = { /* Navigate to video */ },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color.White.copy(alpha = 0.05f),
                                focusedContainerColor = Color.White
                            )
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = item.thumbnail,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(item.title, color = Color.Unspecified, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
                                    Text(item.channel, color = Color.Unspecified, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
