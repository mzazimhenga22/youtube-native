@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MusicPlayerOverlay(
    video: Video,
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    duration: String,
    recommendations: List<Video>,
    exoPlayer: ExoPlayer? = null,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onNext: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onClose: () -> Unit
) {
    val safeProgress = progress.coerceIn(0f, 1f)

    // Vinyl spin animation for album art
    val infiniteTransition = rememberInfiniteTransition(label = "musicAnim")
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylSpin"
    )
    
    // Glow pulse for the album art border
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Background: Live video playing behind everything ──
        if (exoPlayer != null) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { playerView ->
                    playerView.player = exoPlayer
                },
                modifier = Modifier
                    .fillMaxSize()
                    .blur(16.dp) // Cinematic blur on background video
                    .alpha(0.55f)
            )
        } else {
            // Fallback: Blurred thumbnail if no exoPlayer provided
            AsyncImage(
                model = video.thumbnail,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(12.dp)
                    .alpha(0.5f),
                contentScale = ContentScale.Crop
            )
        }

        // Dark overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Ambient color orbs based on music vibe
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .alpha(glowAlpha * 0.3f)
                .background(Color(0xFFFF006E), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .alpha(glowAlpha * 0.25f)
                .background(Color(0xFF00E5FF), CircleShape)
        )

        // ── Back Button ──
        Surface(
            onClick = onClose,
            modifier = Modifier.padding(24.dp).size(40.dp).align(Alignment.TopStart),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color.White
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LocalContentColor.current, modifier = Modifier.size(20.dp))
            }
        }

        // ── "NOW PLAYING" badge top center ──
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 28.dp)
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Animated equalizer bars
            repeat(3) { i ->
                val barHeight by infiniteTransition.animateFloat(
                    initialValue = 4f,
                    targetValue = 14f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400 + i * 150, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar$i"
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(if (isPlaying) barHeight.dp else 4.dp)
                        .background(Color(0xFFFF006E), RoundedCornerShape(2.dp))
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "NOW PLAYING",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Left: Now Playing ──
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                // Album art with video preview inset
                Box(
                    modifier = Modifier
                        .size(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow ring behind album art
                    Box(
                        modifier = Modifier
                            .size(310.dp)
                            .alpha(glowAlpha)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF006E).copy(alpha = 0.4f), Color.Transparent)
                                ),
                                CircleShape
                            )
                    )
                    
                    // Album art card. The background owns the PlayerView; using
                    // another PlayerView here steals ExoPlayer's video surface.
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .shadow(24.dp, RoundedCornerShape(28.dp))
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color.DarkGray)
                    ) {
                        AsyncImage(
                            model = video.thumbnail,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title + channel
                Text(
                    video.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 28.sp,
                    modifier = Modifier.widthIn(max = 320.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    video.channel,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress bar
                Column(modifier = Modifier.widthIn(max = 320.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(safeProgress)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF006E), Color(0xFFFF4081))
                                    ),
                                    CircleShape
                                )
                        )
                        // Seek dot at the end of progress
                        Box(
                            modifier = Modifier
                                .offset(x = (safeProgress * 290).dp) // Approximate based on max width
                                .size(12.dp)
                                .align(Alignment.CenterStart)
                                .background(Color.White, CircleShape)
                                .shadow(4.dp, CircleShape)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(currentTime, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(duration, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MusicControlBtn(Icons.Default.Shuffle) {}
                    MusicControlBtn(Icons.Default.SkipPrevious) { onSeek(-10f) }

                    // Play/Pause — highlighted with accent color
                    Surface(
                        onClick = onTogglePlay,
                        modifier = Modifier.size(64.dp),
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.White,
                            focusedContainerColor = Color(0xFFFF006E)
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = LocalContentColor.current,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    MusicControlBtn(Icons.Default.SkipNext) { onNext() }
                    MusicControlBtn(Icons.Default.Repeat) {}
                }
            }

            Spacer(modifier = Modifier.width(48.dp))

            // ── Right: Up Next ──
            Column(
                modifier = Modifier.width(320.dp).fillMaxHeight().padding(vertical = 20.dp)
            ) {
                val visibleRecommendations = recommendations
                    .filter { it.id.isNotBlank() && it.id != video.id }
                    .distinctBy { it.id }
                    .take(20)

                Text("Up Next", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visibleRecommendations, key = { it.id }) { item ->
                        Surface(
                            onClick = { onVideoClick(item) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color.White.copy(alpha = 0.04f),
                                focusedContainerColor = Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.thumbnail,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.title,
                                        color = LocalContentColor.current,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        item.channel,
                                        color = LocalContentColor.current.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MusicControlBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.3f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}
