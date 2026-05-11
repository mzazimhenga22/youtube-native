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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.repository.YouTubeRepository
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MoviesScreen(
    onVideoClick: (Video) -> Unit,
    repository: YouTubeRepository
) {
    var movieRails by remember { mutableStateOf<Map<String, List<Video>>>(emptyMap()) }
    var focusedVideo by remember { mutableStateOf<Video?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val featured = repository.search("New Movies 2024 Full Length")
            val action = repository.search("Popular Action Movies 4K")
            val scifi = repository.search("Sci-Fi movies full length")
            
            movieRails = mapOf(
                "Featured Blockbusters" to featured,
                "Action & Adventure" to action,
                "Sci-Fi & Fantasy" to scifi
            )
            if (featured.isNotEmpty()) focusedVideo = featured[0]
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        // 1. Theater Background
        TheaterBackground(focusedVideo)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 2. Cinematic Hero Header
            item {
                MovieHero(
                    video = focusedVideo,
                    onWatchClick = { focusedVideo?.let { onVideoClick(it) } }
                )
            }

            // 3. Movie Rails
            movieRails.forEach { (title, videos) ->
                item {
                    MovieRail(
                        title = title,
                        videos = videos,
                        onVideoClick = onVideoClick,
                        onVideoFocus = { focusedVideo = it }
                    )
                }
            }
        }
    }
}

@Composable
fun TheaterBackground(focusedVideo: Video?) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Base Dark Room
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505)))

        // Crossfading Hero Image
        AnimatedContent(
            targetState = focusedVideo,
            transitionSpec = { fadeIn(tween(1500)) togetherWith fadeOut(tween(800)) },
            label = "movieBgFade"
        ) { video ->
            video?.let {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = it.thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.5f
                    )
                    // Unified Blend Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF050505).copy(alpha = 0.2f),
                                        Color(0xFF050505).copy(alpha = 0.7f),
                                        Color(0xFF050505)
                                    )
                                )
                            )
                    )
                }
            }
        }

        // Theater Red Accent (Bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF780000).copy(alpha = 0.1f), Color(0xFF3C0000).copy(alpha = 0.2f))
                    )
                )
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieHero(
    video: Video?,
    onWatchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(640.dp)
            .padding(horizontal = 48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        video?.let {
            Text(
                text = "Premiere Night",
                color = Color.Red,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = it.title,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    lineHeight = 90.sp,
                    letterSpacing = (-4).sp
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(1000.dp)
            )

            Text(
                text = "Exclusive Digital Release • HDR 10+ • Dolby Atmos Surround",
                color = Color.Gray,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Surface(
                    onClick = onWatchClick,
                    modifier = Modifier.height(72.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(32.dp)),
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
                        Text("Watch Now", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
                
                Surface(
                    onClick = { /* TODO */ },
                    modifier = Modifier.height(72.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(32.dp)),
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
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("My List", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieRail(
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
            modifier = Modifier.padding(start = 48.dp, bottom = 20.dp)
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
                        .width(300.dp)
                        .aspectRatio(2/3f)
                        .onFocusChanged { 
                            isFocused = it.isFocused
                            if (it.isFocused) onVideoFocus(video) 
                        },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
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
                        
                        if (isFocused) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .align(Alignment.Center)
                                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(32.dp).padding(start = 4.dp))
                            }
                        }
                    }
                }
                
                // Movie Info under the card
                Column(modifier = Modifier.padding(top = 16.dp, start = 8.dp)) {
                    Text(
                        text = video.title,
                        color = if (isFocused) Color.White else Color.LightGray,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(284.dp)
                    )
                    Text(
                        text = "2024 • 4K ULTRA HD",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

