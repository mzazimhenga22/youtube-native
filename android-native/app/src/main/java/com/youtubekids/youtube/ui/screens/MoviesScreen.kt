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
            val featured = repository.getMoviesHome()
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
        // Theater background
        TheaterBackground(focusedVideo)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 72.dp, bottom = 120.dp)
        ) {
            // Hero
            item {
                MovieHero(
                    video = focusedVideo,
                    onWatchClick = { focusedVideo?.let { onVideoClick(it) } }
                )
            }

            // Movie rails
            movieRails.forEach { (title, videos) ->
                item {
                    MovieRail(title, videos, onVideoClick) { focusedVideo = it }
                }
            }
        }
    }
}

@Composable
fun TheaterBackground(focusedVideo: Video?) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505)))

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
                        alpha = 0.4f
                    )
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF050505).copy(alpha = 0.2f),
                                Color(0xFF050505).copy(alpha = 0.7f),
                                Color(0xFF050505)
                            )
                        )
                    ))
                }
            }
        }

        // Red theater accent
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xFF780000).copy(alpha = 0.08f), Color(0xFF3C0000).copy(alpha = 0.15f))
                    )
                )
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieHero(video: Video?, onWatchClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 80.dp, end = 80.dp, top = 20.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        video?.let {
            // Badge
            Text(
                text = "PREMIERE NIGHT",
                color = Color(0xFFFF3333),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Title
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

            // Subtitle
            Text(
                text = "Exclusive Digital Release • HDR 10+ • Dolby Atmos",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    onClick = onWatchClick,
                    modifier = Modifier.height(48.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    colors = ClickableSurfaceDefaults.colors(containerColor = Color.White, focusedContainerColor = Color.Red)
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Watch Now", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("My List", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                var isFocused by remember { mutableStateOf(false) }

                Column {
                    // Poster card (portrait)
                    Surface(
                        onClick = { onVideoClick(video) },
                        modifier = Modifier
                            .width(200.dp)
                            .aspectRatio(2f / 3f)
                            .onFocusChanged {
                                isFocused = it.isFocused
                                if (it.isFocused) onVideoFocus(video)
                            },
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(
                                androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                            )
                        ),
                        glow = ClickableSurfaceDefaults.glow(
                            focusedGlow = Glow(
                                elevationColor = Color.White.copy(alpha = 0.12f),
                                elevation = 12.dp
                            )
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = video.thumbnail,
                                contentDescription = video.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Bottom gradient
                            Box(modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)))
                            ))

                            // Play icon on focus
                            if (isFocused) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .align(Alignment.Center)
                                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Title + meta below poster
                    Column(modifier = Modifier.padding(top = 8.dp).width(200.dp)) {
                        Text(
                            text = video.title,
                            color = if (isFocused) Color.White else Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = video.channel,
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
