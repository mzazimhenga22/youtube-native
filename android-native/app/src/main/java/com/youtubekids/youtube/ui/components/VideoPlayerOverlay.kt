@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.tv.material3.*
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VideoPlayerOverlay(
    video: Video,
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    duration: String,
    isVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    onUserInteraction: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleComments: () -> Unit,
    onToggleLyrics: () -> Unit,
    onToggleStats: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onToggleLiked: () -> Unit = {},
    onToggleWatchLater: () -> Unit = {},
    currentSpeed: Float = 1.0f,
    currentChapter: String? = null,
    onClose: () -> Unit,
    recommendations: List<Video> = emptyList(),
    onVideoClick: (Video) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showStats by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Tap-to-show when hidden
        if (!isVisible) {
            Surface(
                onClick = { onVisibilityChange(true) },
                modifier = Modifier
                    .fillMaxSize()
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            onVisibilityChange(true)
                        }
                        false
                    },
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
            ) { }
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Main layout is a LazyColumn, giving us vertical scrolling for the right side and bottom rows
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp, top = 48.dp, end = 48.dp, bottom = 48.dp)
            ) {
                item {
                    // Top row contains empty space for the shrunk video on the left, and metadata on the right
                    Row(
                        modifier = Modifier.fillMaxWidth().height(252.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Metadata column on the right
                        Column(
                            modifier = Modifier
                                .width(320.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = video.title,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = video.channel,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${video.views} • ${video.publishedAt ?: ""}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    // Transport controls and progress bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (currentChapter != null) {
                            Text(
                                text = "▸ $currentChapter",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Progress Bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                currentTime,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .padding(horizontal = 16.dp)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .background(Color.Red, RoundedCornerShape(3.dp))
                                )
                            }

                            Text(
                                duration,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left side: Playback controls
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                OverlayButton(icon = Icons.Default.Replay10, label = "Rewind") { onSeek(-10f); onUserInteraction() }
                                
                                Surface(
                                    onClick = { onTogglePlay(); onUserInteraction() },
                                    modifier = Modifier.size(56.dp),
                                    shape = ClickableSurfaceDefaults.shape(CircleShape),
                                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                                    colors = ClickableSurfaceDefaults.colors(
                                        containerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    )
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                OverlayButton(icon = Icons.Default.Forward10, label = "Forward") { onSeek(10f); onUserInteraction() }
                            }

                            // Right side: Social and settings
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OverlayButton(icon = Icons.Default.ThumbUp, label = "Like") { onToggleLiked(); onUserInteraction() }
                                OverlayButton(icon = Icons.Default.WatchLater, label = "Save") { onToggleWatchLater(); onUserInteraction() }
                                OverlayButton(icon = Icons.AutoMirrored.Filled.Comment, label = "Comments") { onToggleComments(); onUserInteraction() }
                                
                                Surface(
                                    onClick = {
                                        val nextSpeed = when (currentSpeed) {
                                            1.0f -> 1.25f
                                            1.25f -> 1.5f
                                            1.5f -> 2.0f
                                            else -> 1.0f
                                        }
                                        onSetSpeed(nextSpeed)
                                        onUserInteraction()
                                    },
                                    modifier = Modifier.height(44.dp),
                                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                                    colors = ClickableSurfaceDefaults.colors(
                                        containerColor = Color.White.copy(alpha = 0.1f),
                                        focusedContainerColor = Color.White
                                    )
                                ) {
                                    Box(modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${currentSpeed}x",
                                            color = LocalContentColor.current,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                OverlayButton(icon = Icons.Default.Settings, label = "Settings") { onToggleStats(); onUserInteraction() }
                                OverlayButton(icon = Icons.AutoMirrored.Filled.ArrowBack, label = "Close") { onClose() }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }

                // Up Next Recommendations
                if (recommendations.isNotEmpty()) {
                    item {
                        Text(
                            text = "Up Next",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(end = 48.dp, bottom = 48.dp)
                        ) {
                            items(recommendations) { rec ->
                                UpNextVideoCard(video = rec) {
                                    onVideoClick(rec)
                                    onUserInteraction()
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showStats) {
            StatsForNerds(video = video, onClose = { showStats = false })
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun OverlayButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = label,
                tint = LocalContentColor.current,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun UpNextVideoCard(
    video: Video,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(280.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.White.copy(alpha = 0.1f)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box {
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                if (video.duration.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = video.duration,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = video.channel,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (video.views.isNotEmpty()) {
                Text(
                    text = video.views,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
