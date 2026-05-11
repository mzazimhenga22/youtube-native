@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroBanner(
    videos: List<Video>,
    onPlayClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    if (videos.isEmpty()) return

    val heroVideos = videos.take(5)
    var activeIndex by remember { mutableIntStateOf(0) }
    val currentVideo = heroVideos[activeIndex]
    
    // Auto-rotate logic
    LaunchedEffect(heroVideos) {
        while (heroVideos.size > 1) {
            delay(8000)
            activeIndex = (activeIndex + 1) % heroVideos.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(720.dp)
            .padding(horizontal = 48.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color.Black)
    ) {
        // Background Image with Crossfade
        Crossfade(
            targetState = currentVideo.thumbnail,
            animationSpec = tween(1000),
            label = "heroBackground"
        ) { thumbnail ->
            AsyncImage(
                model = thumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 1. Left Gradient for text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 1400f
                    )
                )
        )

        // 2. Bottom Fade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF0A0A0A).copy(alpha = 0.3f),
                            Color(0xFF0A0A0A).copy(alpha = 0.9f),
                            Color(0xFF0A0A0A)
                        ),
                        startY = 0f
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 80.dp),
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = currentVideo,
                transitionSpec = {
                    fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 2 } togetherWith
                    fadeOut(tween(400))
                },
                label = "heroContent"
            ) { video ->
                Column {
                    // Trending Badge
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color.Red, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (activeIndex == 0) "FEATURED" else "#${activeIndex + 1} TRENDING",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
                        if (video.views.isNotEmpty()) {
                            Text(
                                text = "${video.views} views",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            lineHeight = 78.sp,
                            letterSpacing = (-3).sp
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(800.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Channel
                    Text(
                        text = video.channel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 40.dp)
                    )
                }
            }

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                HeroButton(
                    label = "Play Now",
                    icon = Icons.Default.PlayArrow,
                    primary = true,
                    onClick = { onPlayClick(currentVideo) }
                )
                HeroButton(
                    label = "More Info",
                    icon = Icons.Default.Info,
                    primary = false,
                    onClick = {}
                )
                Surface(
                    onClick = {},
                    modifier = Modifier.size(64.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.White.copy(alpha = 0.25f)
                    ),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White)))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        // Bottom Navigation (Dots + Arrows)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 60.dp, end = 80.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Dots
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(heroVideos.size) { i ->
                    val isActive = activeIndex == i
                    val width by animateDpAsState(if (isActive) 32.dp else 10.dp, label = "dotWidth")
                    Box(
                        modifier = Modifier
                            .size(width = width, height = 10.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color.Red else Color.White.copy(alpha = 0.3f))
                    )
                }
            }

            // Arrows
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ArrowButton(icon = Icons.Default.ChevronLeft) {
                    activeIndex = (activeIndex - 1 + heroVideos.size) % heroVideos.size
                }
                ArrowButton(icon = Icons.Default.ChevronRight) {
                    activeIndex = (activeIndex + 1) % heroVideos.size
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HeroButton(
    label: String,
    icon: ImageVector,
    primary: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(64.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) Color.White else Color.White.copy(alpha = 0.1f),
            focusedContainerColor = if (primary) Color.Red else Color.White.copy(alpha = 0.25f)
        ),
        border = if (!primary) ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White))) else ClickableSurfaceDefaults.border()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (primary) Color.Black else Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = if (primary) Color.Black else Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ArrowButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(60.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(18.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.25f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}
