@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun KidsVideoPlayerOverlay(
    title: String,
    isPlaying: Boolean,
    progress: Float,
    upNext: List<Video>,
    onClose: () -> Unit,
    onTogglePlay: () -> Unit,
    onSelectVideo: (Video) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
    ) {
        // Top Bar: Giant Close Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                onClick = onClose,
                modifier = Modifier.size(80.dp),
                shape = ClickableSurfaceDefaults.shape(CircleShape),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.2f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    focusedContainerColor = Color.White
                ),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(BorderStroke(4.dp, Color(0xFFFFD700)))
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF2D3436),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Bottom Section: Integrated Controls & Shelf
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            // Playful Video Shelf
            if (upNext.isNotEmpty()) {
                Text(
                    text = "UP NEXT",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 60.dp, bottom = 16.dp),
                    letterSpacing = 4.sp
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 60.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.padding(bottom = 48.dp)
                ) {
                    itemsIndexed(upNext) { index, video ->
                        KidsUpNextCard(video = video, onClick = { onSelectVideo(video) })
                    }
                }
            }

            // Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Giant Play/Pause Button
                Surface(
                    onClick = onTogglePlay,
                    modifier = Modifier.size(96.dp),
                    shape = ClickableSurfaceDefaults.shape(CircleShape),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                    colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFFFF4B4B)),
                    border = ClickableSurfaceDefaults.border(
                        border = Border(BorderStroke(4.dp, Color.White)),
                        focusedBorder = Border(BorderStroke(4.dp, Color(0xFFFFD700)))
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Play",
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(40.dp))

                // Scrubber & Title
                Column(modifier = Modifier.weight(1f)) {
                    // Bright Scrubber
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(Color(0xFF4BFF7B), RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title.uppercase(),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "KIDS MODE",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun KidsUpNextCard(video: Video, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(256.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(40.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF2D3436),
            focusedContainerColor = Color(0xFF2D3436)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(6.dp, Color(0xFFFFD700)))
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .aspectRatio(16/9f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
            ) {
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color(0xFFFF4B4B), RoundedCornerShape(100.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = video.duration,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 4.dp)
            )
        }
    }
}
