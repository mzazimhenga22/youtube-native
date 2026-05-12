@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
        MusicCategory("Trending", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF00FF99)),
        MusicCategory("New Releases", Icons.Default.Album, Color(0xFF00AAFF)),
        MusicCategory("Live", Icons.Default.Radio, Color(0xFFFF9900))
    )

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            homeMusic = repository.getMusicHome()
            trendingMusic = repository.search("Trending music Kenya")
            relaxMusic = repository.search("Lofi hip hop relax chill")
            if (homeMusic.isNotEmpty()) focusedVideo = homeMusic[0]
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        // Visualizer background
        MusicVisualizerBackground()

        // Focused video ambient bg
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 72.dp, bottom = 120.dp)
        ) {
            // Hero
            item {
                MusicHero(
                    video = focusedVideo,
                    onPlayClick = { focusedVideo?.let { onVideoClick(it) } }
                )
            }

            // Category pills
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 80.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { category ->
                        var isFocused by remember { mutableStateOf(false) }
                        Surface(
                            onClick = { /* TODO */ },
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

            // Rails
            item {
                MusicRail("Your Favorites", homeMusic, true, onVideoClick) { focusedVideo = it }
            }
            item {
                MusicRail("Trending Now", trendingMusic, false, onVideoClick) { focusedVideo = it }
            }
            item {
                MusicRail("Relaxing Vibes", relaxMusic, true, onVideoClick) { focusedVideo = it }
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
            items(videos, key = { it.id }) { video ->
                MusicCard(
                    video = video,
                    isSquare = isSquare,
                    width = if (isSquare) 240 else 340,
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
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "wave1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label = "wave2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().alpha(wave1).background(
            Brush.verticalGradient(listOf(Color(0xFF7800FF).copy(alpha = 0.08f), Color.Transparent))
        ))
        Box(modifier = Modifier.fillMaxSize().alpha(wave2).background(
            Brush.verticalGradient(listOf(Color.Transparent, Color(0xFFFF0064).copy(alpha = 0.06f)))
        ))
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MusicHero(video: Video?, onPlayClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 80.dp, end = 80.dp, top = 20.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Badge
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFFF0055), Color(0xFF7700FF))),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text("NOW PLAYING", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
            video?.title ?: "Loading...",
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

        // Channel pill + listeners
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(video?.channel ?: "", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("${video?.views ?: "0"} listeners", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                onClick = onPlayClick,
                modifier = Modifier.height(48.dp),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                colors = ClickableSurfaceDefaults.colors(containerColor = Color.White, focusedContainerColor = Color(0xFFFF0055))
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Listen Now", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                onClick = {},
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
                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mix", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
