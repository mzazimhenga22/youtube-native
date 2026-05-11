@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import com.youtubekids.youtube.data.model.Video

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VideoPlayerOverlay(
    video: Video,
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    duration: String,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleComments: () -> Unit,
    onToggleLyrics: () -> Unit,
    onToggleStats: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    currentSpeed: Float = 1.0f,
    currentChapter: String? = null,
    onClose: () -> Unit,
    recommendations: List<Video> = emptyList(),
    onVideoClick: (Video) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    var showStats by remember { mutableStateOf(false) }
    var interactionKey by remember { mutableIntStateOf(0) }

    val safeProgress = progress.coerceIn(0f, 1f)

    // Show controls on play state change
    LaunchedEffect(isPlaying) {
        isVisible = true
        interactionKey++
    }

    // Auto-hide
    LaunchedEffect(isVisible, isPlaying, interactionKey) {
        if (isVisible && isPlaying) {
            kotlinx.coroutines.delay(5000)
            isVisible = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Tap-to-show when hidden
        if (!isVisible) {
            Surface(
                onClick = {
                    isVisible = true
                    interactionKey++
                },
                modifier = Modifier
                    .fillMaxSize()
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            isVisible = true
                            interactionKey++
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
            Box(modifier = Modifier.fillMaxSize()) {
                // ── Top gradient ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )

                // ── Bottom gradient ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // ── Top Bar: Close + Title ──
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button
                    Surface(
                        onClick = onClose,
                        modifier = Modifier.size(40.dp),
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color.White
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = LocalContentColor.current,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Title in top bar
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (video.channel.isNotEmpty()) {
                            Text(
                                text = video.channel,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Settings gear
                    Surface(
                        onClick = { onToggleStats(); interactionKey++ },
                        modifier = Modifier.size(40.dp),
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color.White
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = LocalContentColor.current, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // ── Center Play/Pause ──
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    Surface(
                        onClick = { onSeek(-10f); interactionKey++ },
                        modifier = Modifier.size(48.dp),
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Replay10, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }

                    // Play/Pause button
                    Surface(
                        onClick = { onTogglePlay(); interactionKey++ },
                        modifier = Modifier.size(64.dp),
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
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Forward 10s
                    Surface(
                        onClick = { onSeek(10f); interactionKey++ },
                        modifier = Modifier.size(48.dp),
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Forward10, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }

                // ── Bottom Controls ──
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    // Chapter indicator
                    if (currentChapter != null) {
                        Text(
                            text = "▸ $currentChapter",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                        )
                    }

                    // ── Progress Bar ──
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            currentTime,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Track
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .padding(horizontal = 12.dp)
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                        ) {
                            // Progress fill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(safeProgress)
                                    .fillMaxHeight()
                                    .background(Color.Red, RoundedCornerShape(3.dp))
                            )
                            // Scrub dot
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .offset(x = (safeProgress * 100).dp.coerceAtMost(500.dp))
                            ) {
                                // Dot will be approximated by the bar end
                            }
                        }

                        Text(
                            duration,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Action Buttons Row ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: social actions
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OverlayButton(icon = Icons.Default.ThumbUp, label = "Like") { interactionKey++ }
                            OverlayButton(icon = Icons.Default.ThumbDown, label = "Dislike") { interactionKey++ }
                            OverlayButton(icon = Icons.Default.Comment, label = "Comments") {
                                onToggleComments(); interactionKey++
                            }
                            OverlayButton(icon = Icons.Default.MusicNote, label = "Lyrics") {
                                onToggleLyrics(); interactionKey++
                            }

                            // Speed toggle
                            Surface(
                                onClick = {
                                    val nextSpeed = when (currentSpeed) {
                                        1.0f -> 1.25f
                                        1.25f -> 1.5f
                                        1.5f -> 2.0f
                                        else -> 1.0f
                                    }
                                    onSetSpeed(nextSpeed)
                                    interactionKey++
                                },
                                modifier = Modifier.height(36.dp),
                                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    focusedContainerColor = Color.White
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxHeight().padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${currentSpeed}x",
                                        color = LocalContentColor.current,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Right: fullscreen / PiP
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OverlayButton(icon = Icons.Default.PictureInPicture, label = "PiP") { interactionKey++ }
                            OverlayButton(icon = Icons.Default.Fullscreen, label = "Fullscreen") { interactionKey++ }
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
        modifier = Modifier.size(36.dp),
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
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
