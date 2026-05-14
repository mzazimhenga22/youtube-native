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

// Kids-friendly color palette
private val KidsRed = Color(0xFFFF4B4B)
private val KidsGreen = Color(0xFF4BFF7B)
private val KidsGold = Color(0xFFFFD700)
private val KidsBlue = Color(0xFF4BA3FF)
private val KidsDark = Color(0xFF2D3436)

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
    val safeProgress = progress.coerceIn(0f, 1f)
    val playPauseFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            playPauseFocusRequester.requestFocus()
        } catch (e: Exception) {
            // Ignore focus request failures during quick transitions
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
    ) {
        // ── Close Button (Top-Right) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                onClick = onClose,
                modifier = Modifier.size(56.dp),
                shape = ClickableSurfaceDefaults.shape(CircleShape),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    focusedContainerColor = Color.White
                ),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(BorderStroke(3.dp, KidsGold))
                )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = KidsDark,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // ── Bottom Section ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            // Up Next shelf
            if (upNext.isNotEmpty()) {
                Text(
                    text = "UP NEXT",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(start = 48.dp, bottom = 12.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    itemsIndexed(upNext) { _, video ->
                        KidsUpNextCard(video = video, onClick = { onSelectVideo(video) })
                    }
                }
            }

            // ── Control Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause button
                Surface(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(64.dp)
                        .androidx.compose.ui.focus.focusRequester(playPauseFocusRequester),
                    shape = ClickableSurfaceDefaults.shape(CircleShape),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                    colors = ClickableSurfaceDefaults.colors(containerColor = KidsRed),
                    border = ClickableSurfaceDefaults.border(
                        border = Border(BorderStroke(3.dp, Color.White)),
                        focusedBorder = Border(BorderStroke(3.dp, KidsGold))
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Scrubber + Title
                Column(modifier = Modifier.weight(1f)) {
                    // Progress bar (chunky for kids)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(safeProgress)
                                .background(KidsGreen, RoundedCornerShape(6.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title.uppercase(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .background(KidsBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .border(1.dp, KidsBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "KIDS MODE",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
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
        modifier = Modifier.width(200.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = KidsDark,
            focusedContainerColor = KidsDark
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(3.dp, KidsGold))
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
            ) {
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Duration badge
                if (video.duration.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(KidsRed, RoundedCornerShape(8.dp))
                            .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = video.duration,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 2.dp)
            )
        }
    }
}
