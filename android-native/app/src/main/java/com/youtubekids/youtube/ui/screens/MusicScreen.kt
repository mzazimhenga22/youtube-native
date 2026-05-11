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
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.ui.components.MusicCard
import com.youtubekids.youtube.data.repository.YouTubeRepository
import com.youtubekids.youtube.ui.components.HorizontalRail
import kotlinx.coroutines.delay

data class MusicCategory(val label: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MusicScreen(
    onVideoClick: (Video) -> Unit,
    repository: YouTubeRepository
) {
    var homeMusic by remember { mutableStateOf<List<Video>>(emptyList()) }
    var trendingMusic by remember { mutableStateOf<List<Video>>(emptyList()) }
    var relaxMusic by remember { mutableStateOf<List<Video>>(emptyList()) }
    var focusedVideo by remember { mutableStateOf<Video?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val categories = listOf(
        MusicCategory("My Mix", Icons.Default.Shuffle, Color(0xFFFF0055)),
        MusicCategory("For You", Icons.Default.AutoAwesome, Color(0xFF7700FF)),
        MusicCategory("Trending", Icons.Default.TrendingUp, Color(0xFF00FF99)),
        MusicCategory("New Releases", Icons.Default.Album, Color(0xFF00AAFF)),
        MusicCategory("Live", Icons.Default.Radio, Color(0xFFFF9900))
    )

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            homeMusic = repository.search("Official Music Videos")
            trendingMusic = repository.search("Trending music")
            relaxMusic = repository.search("Lofi hip hop relax")
            if (homeMusic.isNotEmpty()) focusedVideo = homeMusic[0]
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        // 1. Music Visualizer Background
        MusicVisualizerBackground()

        // 2. Focused Video Background Fade
        AnimatedContent(
            targetState = focusedVideo,
            transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(500)) },
            label = "musicBgFade"
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                MusicHero(
                    video = focusedVideo,
                    onPlayClick = { focusedVideo?.let { onVideoClick(it) } }
                )
            }

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
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(32.dp)),
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
                                    tint = if (isFocused) Color.Black else category.color,
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

            item {
                MusicRail(
                    title = "Your Favorites",
                    videos = homeMusic,
                    isSquare = true,
                    onVideoClick = onVideoClick,
                    onVideoFocus = { focusedVideo = it }
                )
            }

            item {
                MusicRail(
                    title = "Trending Now",
                    videos = trendingMusic,
                    onVideoClick = onVideoClick,
                    onVideoFocus = { focusedVideo = it }
                )
            }

            item {
                MusicRail(
                    title = "Relaxing Vibes",
                    videos = relaxMusic,
                    isSquare = true,
                    onVideoClick = onVideoClick,
                    onVideoFocus = { focusedVideo = it }
                )
            }
        }
    }
}

@Composable
fun MusicRail(
    title: String,
    videos: List<Video>,
    isSquare: Boolean = false,
    onVideoClick: (Video) -> Unit,
    onVideoFocus: (Video) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 48.dp, bottom = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(videos) { video ->
                MusicCard(
                    video = video,
                    isSquare = isSquare,
                    width = if (isSquare) 300 else 420,
                    onFocus = { onVideoFocus(video) },
                    onClick = { onVideoClick(video) }
                )
            }
        }
    }
}

@Composable
private fun MusicVisualizerBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "musicVisualizer")
    
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave1"
    )

    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(wave1)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF7800FF).copy(alpha = 0.1f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(wave2)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFFF0064).copy(alpha = 0.08f))
                    )
                )
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MusicHero(
    video: Video?,
    onPlayClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(560.dp)
            .padding(horizontal = 48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Music World",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Red,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            video?.title ?: "Loading...",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                lineHeight = 72.sp,
                letterSpacing = (-3).sp
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(800.dp)
        )

        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = video?.channel ?: "",
                    color = Color.LightGray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${video?.views ?: "0"} listeners",
                color = Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                onClick = onPlayClick,
                modifier = Modifier.height(64.dp),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.White,
                    focusedContainerColor = Color.Red
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Listen Now", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }
            
            Surface(
                onClick = { /* TODO */ },
                modifier = Modifier.height(64.dp),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
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
                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Mix", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

