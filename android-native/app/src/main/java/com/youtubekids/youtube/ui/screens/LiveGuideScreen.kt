@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.alpha
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
import com.youtubekids.youtube.data.repository.YouTubeRepository
import com.youtubekids.youtube.ui.components.LiveCard

data class LiveCategory(val label: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveGuideScreen(
    onVideoClick: (Video) -> Unit,
    repository: YouTubeRepository
) {
    var channels by remember { mutableStateOf<List<Video>>(emptyList()) }
    var focusedVideo by remember { mutableStateOf<Video?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val categories = listOf(
        LiveCategory("All Live", Icons.Default.Tv, Color.Red),
        LiveCategory("News", Icons.Default.Public, Color(0xFF00AAFF)),
        LiveCategory("Sports", Icons.Default.EmojiEvents, Color(0xFFFFCC00)),
        LiveCategory("Gaming", Icons.Default.Gamepad, Color(0xFF00FF99)),
        LiveCategory("Music", Icons.Default.MusicNote, Color(0xFFFF00FF))
    )

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            channels = repository.getLiveGuide()
            if (channels.isNotEmpty()) focusedVideo = channels[0]
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        // 1. Pulsing Ambient Background
        LiveAmbientBackground(focusedVideo)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 72.dp, bottom = 120.dp)
        ) {
            // 2. Live Hero
            item {
                LiveHero(
                    video = focusedVideo,
                    onWatchClick = { focusedVideo?.let { onVideoClick(it) } }
                )
            }

            // 3. Category Navigation
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 80.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { category ->
                        var isFocused by remember { mutableStateOf(false) }
                        Surface(
                            onClick = { /* TODO: Implement category filtering */ },
                            modifier = Modifier
                                .height(40.dp)
                                .onFocusChanged { isFocused = it.isFocused },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color.White.copy(alpha = 0.06f),
                                focusedContainerColor = Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    category.icon,
                                    contentDescription = null,
                                    tint = if (isFocused) category.color else category.color.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = category.label,
                                    color = if (isFocused) Color.Black else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 4. Rails
            item {
                LiveRail(
                    title = "Featured Live Streams",
                    videos = channels,
                    onVideoClick = onVideoClick,
                    onVideoFocus = { focusedVideo = it }
                )
            }

            item {
                LiveRail(
                    title = "Breaking News",
                    videos = channels.shuffled().take(6),
                    onVideoClick = onVideoClick,
                    onVideoFocus = { focusedVideo = it }
                )
            }
        }
    }
}

@Composable
fun LiveAmbientBackground(focusedVideo: Video?) {
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Subtle Red Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(pulseAlpha)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.1f), Color.Transparent, Color.Red.copy(alpha = 0.05f))
                    )
                )
        )

        // Thumbnail Fade
        AnimatedContent(
            targetState = focusedVideo,
            transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(500)) },
            label = "liveBgFade"
        ) { video ->
            video?.let {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = it.thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.35f
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF050505).copy(alpha = 0.3f),
                                        Color(0xFF050505).copy(alpha = 0.7f),
                                        Color(0xFF050505)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveHero(
    video: Video?,
    onWatchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 80.dp, end = 80.dp, top = 20.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        video?.let {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .background(Color.Red, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Radio, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "LIVE NOW",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
                
                Text(
                    text = "${it.views} watching",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = it.title,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 42.sp,
                letterSpacing = (-1.5).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 600.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = it.channel,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    onClick = onWatchClick,
                    modifier = Modifier.height(48.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White,
                        focusedContainerColor = Color.Red
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Watch Live", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Surface(
                    onClick = { /* TODO */ },
                    modifier = Modifier.height(48.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        focusedContainerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SignalCellularAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Full Guide", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveRail(
    title: String,
    videos: List<Video>,
    onVideoClick: (Video) -> Unit,
    onVideoFocus: (Video) -> Unit
) {
    if (videos.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
            modifier = Modifier.padding(start = 80.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(videos) { video ->
                LiveCard(
                    video = video,
                    onClick = { onVideoClick(video) },
                    onFocus = { onVideoFocus(video) }
                )
            }
        }
    }
}
