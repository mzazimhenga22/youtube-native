@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun MiniPlayer(
    video: Video?,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onOpenFull: () -> Unit,
    onClose: () -> Unit
) {
    if (video == null) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(600.dp)
                .height(90.dp)
                .shadow(20.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1C1C1E).copy(alpha = 0.95f), Color(0xFF121212).copy(alpha = 0.98f))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail
                    AsyncImage(
                        model = video.thumbnail,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF333333)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    // Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            video.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            video.channel,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    // Controls
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onTogglePlay) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = onOpenFull) {
                            Icon(Icons.Default.OpenInFull, contentDescription = null, tint = Color.White)
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp)
                        )

                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                    }
                }

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}
