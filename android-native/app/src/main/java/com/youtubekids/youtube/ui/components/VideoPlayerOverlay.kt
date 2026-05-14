@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.tv.material3.*
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video

data class VideoRecommendationShelf(
    val title: String,
    val videos: List<Video>
)

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
    recommendationShelves: List<VideoRecommendationShelf> = emptyList(),
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
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Background gradient for better readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 56.dp, top = 56.dp, end = 56.dp, bottom = 40.dp)
                ) {

                // ── Top-right: Video metadata ──
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(320.dp),
                    verticalArrangement = Arrangement.Top
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

                // ── Bottom: Transport controls + progress + recommendations ──
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                ) {
                    // Chapter indicator
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

                    Spacer(modifier = Modifier.height(20.dp))

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

                    val shelves = recommendationShelves.ifEmpty {
                        if (recommendations.isNotEmpty()) listOf(VideoRecommendationShelf("Up Next", recommendations)) else emptyList()
                    }.map { shelf -> shelf.copy(videos = shelf.videos.distinctBy { it.id }.take(12)) }
                        .filter { it.videos.isNotEmpty() }

                    // YouTube-style recommendation shelves under the controls.
                    if (shelves.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 450.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            itemsIndexed(shelves) { index, shelf ->
                                var itemVisible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(100L * index)
                                    itemVisible = true
                                }

                                AnimatedVisibility(
                                    visible = itemVisible,
                                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RecommendationShelfRow(
                                        shelf = shelf,
                                        onVideoClick = { rec ->
                                            onVideoClick(rec)
                                            onUserInteraction()
                                        }
                                    )
                                }
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
private fun RecommendationShelfRow(
    shelf: VideoRecommendationShelf,
    onVideoClick: (Video) -> Unit
) {
    Column {
        Text(
            text = shelf.title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 56.dp)
        ) {
            items(shelf.videos, key = { it.id }) { rec ->
                UpNextVideoCard(video = rec, width = 260.dp) {
                    onVideoClick(rec)
                }
            }
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
    width: androidx.compose.ui.unit.Dp = 280.dp,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(width)
            .onFocusChanged { isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.White.copy(alpha = 0.15f)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(16.dp)
            )
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = Color.White.copy(alpha = 0.1f),
                elevation = 10.dp
            )
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box {
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Duration pill
                if (video.duration.isNotEmpty() && video.duration != "0:00") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = video.duration,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Overlay for focused state to make it pop
                if (isFocused) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.White.copy(alpha = 0.05f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = video.title,
                color = if (isFocused) Color.White else Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = video.channel,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (video.views.isNotEmpty()) {
                    Text(
                        text = " • ${video.views}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
