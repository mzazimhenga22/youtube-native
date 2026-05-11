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

    // Clamp progress to valid range for fillMaxWidth
    val safeProgress = progress.coerceIn(0f, 1f)

    // Show controls when play state changes
    LaunchedEffect(isPlaying) {
        isVisible = true
        interactionKey++
    }

    // Auto-hide logic - restarts whenever interactionKey changes
    LaunchedEffect(isVisible, isPlaying, interactionKey) {
        if (isVisible && isPlaying) {
            kotlinx.coroutines.delay(5000)
            isVisible = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // When controls are hidden, show an invisible focusable Surface
        // Any remote button press (D-pad or Select) re-shows the overlay
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
                        false // Don't consume — let default handling proceed
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
                // Background Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                                startY = 500f
                            )
                        )
                )

                // Close Button
                Surface(
                    onClick = onClose,
                    modifier = Modifier.padding(24.dp).size(48.dp).align(Alignment.TopStart),
                    shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.White,
                        contentColor = Color.White,
                        focusedContentColor = Color.Black
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = LocalContentColor.current)
                    }
                }

                // Video Info & Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 48.dp, bottom = 48.dp, end = 48.dp)
                ) {
                    Column {
                        Text(
                            text = video.title,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (currentChapter != null) {
                            Text(
                                text = "Chapter: $currentChapter",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Progress Bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(currentTime, color = Color.White.copy(alpha = 0.6f))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .padding(horizontal = 16.dp)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(safeProgress)
                                    .fillMaxHeight()
                                    .background(Color.Red)
                            )
                        }
                        Text(duration, color = Color.White.copy(alpha = 0.6f))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            IconButton(icon = Icons.Default.ThumbUp, onClick = { interactionKey++ })
                            IconButton(icon = Icons.Default.Comment, onClick = { onToggleComments(); interactionKey++ })
                            IconButton(icon = Icons.Default.MusicNote, onClick = { onToggleLyrics(); interactionKey++ })
                            
                            // Speed Toggle
                            Surface(
                                onClick = { 
                                    val nextSpeed = when(currentSpeed) {
                                        1.0f -> 1.25f
                                        1.25f -> 1.5f
                                        1.5f -> 2.0f
                                        else -> 1.0f
                                    }
                                    onSetSpeed(nextSpeed)
                                    interactionKey++
                                },
                                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                                colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f), focusedContainerColor = Color.White)
                            ) {
                                Text(
                                    text = "${currentSpeed}x",
                                    color = if (androidx.tv.material3.LocalContentColor.current == Color.Black) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Black
                                )
                            }

                            IconButton(icon = Icons.Default.Info, onClick = { onToggleStats(); interactionKey++ })
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(icon = Icons.Default.Replay10, onClick = { onSeek(-10f); interactionKey++ })
                            
                            Surface(
                                onClick = { onTogglePlay(); interactionKey++ },
                                modifier = Modifier.size(80.dp),
                                shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
                                colors = ClickableSurfaceDefaults.colors(containerColor = Color.White)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            IconButton(icon = Icons.Default.Forward10, onClick = { onSeek(10f); interactionKey++ })
                        }

                        IconButton(icon = Icons.Default.Settings, onClick = { interactionKey++ })
                    }
                }
            }
        }

        if (showStats) {
            StatsForNerds(video = video, onClose = { showStats = false })
        }
    }
}
