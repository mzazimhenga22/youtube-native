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

    // Auto-rotate
    LaunchedEffect(heroVideos) {
        while (heroVideos.size > 1) {
            delay(8000)
            activeIndex = (activeIndex + 1) % heroVideos.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(horizontal = 72.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF0A0A0A))
    ) {
        // ── Background Image ──
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

        // ── Left gradient for text ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.9f),
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 1200f
                    )
                )
        )

        // ── Bottom vignette ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.95f)
                        ),
                        startY = 200f
                    )
                )
        )

        // ── Content ──
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 56.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            AnimatedContent(
                targetState = currentVideo,
                transitionSpec = {
                    fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 } togetherWith
                    fadeOut(tween(300))
                },
                label = "heroContent"
            ) { video ->
                Column {
                    // Badge row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Red, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (activeIndex == 0) "FEATURED" else "#${activeIndex + 1} TRENDING",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        }
                        if (video.views.isNotEmpty()) {
                            Text(
                                text = video.views,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 42.sp,
                        letterSpacing = (-1.5).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 600.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Channel
                    Text(
                        text = video.channel,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Action Buttons ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                // Add to list button
                Surface(
                    onClick = {},
                    modifier = Modifier.size(48.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        focusedContainerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)))
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

        // ── Pagination (Bottom Right) ──
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 40.dp, end = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(heroVideos.size) { i ->
                    val isActive = activeIndex == i
                    val w by animateDpAsState(if (isActive) 24.dp else 8.dp, label = "dot")
                    Box(
                        modifier = Modifier
                            .size(width = w, height = 8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color.Red else Color.White.copy(alpha = 0.25f))
                    )
                }
            }

            // Arrows
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        modifier = Modifier.height(48.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) Color.White else Color.White.copy(alpha = 0.08f),
            focusedContainerColor = if (primary) Color.Red else Color.White.copy(alpha = 0.2f)
        ),
        border = if (!primary) ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)))
        ) else ClickableSurfaceDefaults.border()
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (primary) Color.Black else Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                color = if (primary) Color.Black else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
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
        modifier = Modifier.size(40.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.08f),
            focusedContainerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}
