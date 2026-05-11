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

data class LiveCategory(val label: String, val icon: ImageVector)

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
        LiveCategory("All", Icons.Default.Tv),
        LiveCategory("News", Icons.Default.Public),
        LiveCategory("Sports", Icons.Default.EmojiEvents),
        LiveCategory("Gaming", Icons.Default.Gamepad)
    )

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            channels = repository.getLiveChannels()
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
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 2. Borderless Live Hero
            item {
                LiveHero(
                    video = focusedVideo,
                    onWatchClick = { focusedVideo?.let { onVideoClick(it) } }
                )
            }

            // 3. Category Navigation
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categories) { category ->
                        var isFocused by remember { mutableStateOf(false) }
                        Surface(
                            onClick = { /* TODO */ },
                            modifier = Modifier.wrapContentSize().onFocusChanged { isFocused = it.isFocused },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color.White.copy(alpha = 0.05f),
                                focusedContainerColor = Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    category.icon,
                                    contentDescription = null,
                                    tint = if (isFocused) Color.Black else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = category.label,
                                    color = if (isFocused) Color.Black else Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // 4. Rails
            item {
                LiveRail(
                    title = "Currently Live",
                    videos = channels,
                    onVideoClick = onVideoClick,
                    onVideoFocus = { focusedVideo = it }
                )
            }

            item {
                LiveRail(
                    title = "Trending Streams",
                    videos = channels.reversed(),
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
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Red Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(pulseAlpha)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.15f), Color.Transparent, Color.Red.copy(alpha = 0.05f))
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
                        alpha = 0.4f
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFF050505).copy(alpha = 0.6f), Color(0xFF050505))
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
            .height(600.dp)
            .padding(horizontal = 48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        video?.let {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .background(Color.Red, RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Radio, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "LIVE NOW",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = it.views,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = it.title,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    lineHeight = 78.sp,
                    letterSpacing = (-3).sp
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(900.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = it.channel,
                    color = Color.LightGray,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Surface(
                    onClick = onWatchClick,
                    modifier = Modifier.height(72.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White,
                        focusedContainerColor = Color.Red
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Watch Live", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
                
                Surface(
                    onClick = { /* TODO */ },
                    modifier = Modifier.height(72.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.SignalCellularAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Full Guide", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
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
    Column(modifier = Modifier.padding(bottom = 56.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 48.dp, bottom = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(videos) { video ->
                var isFocused by remember { mutableStateOf(false) }
                Surface(
                    onClick = { onVideoClick(video) },
                    modifier = Modifier
                        .width(440.dp)
                        .aspectRatio(16/9f)
                        .onFocusChanged { 
                            isFocused = it.isFocused
                            if (it.isFocused) onVideoFocus(video) 
                        },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(40.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(4.dp, Color.White)))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = video.thumbnail,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                        )
                        
                        // Live Badge
                        Box(
                            modifier = Modifier
                                .padding(24.dp)
                                .align(Alignment.TopStart)
                                .background(Color.Red, RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SignalCellularAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("LIVE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            }
                        }

                        // Viewers Badge
                        Box(
                            modifier = Modifier
                                .padding(24.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(video.views, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (isFocused) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .align(Alignment.Center)
                                    .background(Color.White.copy(alpha = 0.9f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp).padding(start = 4.dp))
                            }
                        }
                    }
                }
                
                // Title and Channel under the card
                Column(modifier = Modifier.padding(top = 20.dp, start = 12.dp)) {
                    Text(
                        text = video.title,
                        color = if (isFocused) Color.White else Color.LightGray,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(416.dp)
                    )
                    Text(
                        text = video.channel,
                        color = Color.Gray,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

