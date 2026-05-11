@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import com.youtubekids.youtube.ui.components.ShortsCard

data class ShortsCategory(val label: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ShortsScreen(
    onVideoClick: (Video) -> Unit,
    repository: YouTubeRepository
) {
    var shorts by remember { mutableStateOf<List<Video>>(emptyList()) }
    var viralShorts by remember { mutableStateOf<List<Video>>(emptyList()) }
    var focusedShort by remember { mutableStateOf<Video?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val categories = listOf(
        ShortsCategory("Trending", Icons.Default.Whatshot, Color(0xFFFF4400)),
        ShortsCategory("Funny", Icons.Default.Bolt, Color(0xFFFFCC00)),
        ShortsCategory("Gaming", Icons.Default.Gamepad, Color(0xFF00FF99)),
        ShortsCategory("Music", Icons.Default.MusicNote, Color(0xFFFF00FF))
    )

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            shorts = repository.search("viral shorts 2024")
            viralShorts = shorts.take(10)
            if (shorts.isNotEmpty()) focusedShort = shorts[0]
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        // 1. Ambient Background
        AnimatedContent(
            targetState = focusedShort,
            transitionSpec = { fadeIn(tween(800)) togetherWith fadeOut(tween(400)) },
            label = "shortsBg"
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
                                        Color(0xFF050505).copy(alpha = 0.4f),
                                        Color(0xFF050505).copy(alpha = 0.8f),
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
            // 2. Vertical Hero Moment
            item {
                ShortsHero(
                    video = focusedShort,
                    onWatchClick = { focusedShort?.let { onVideoClick(it) } }
                )
            }

            // 3. Categories
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
                                    tint = if (isFocused) Color.Black else category.color,
                                    modifier = Modifier.size(20.dp)
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
                Text(
                    "Viral Shorts",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 48.dp, bottom = 16.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(viralShorts) { video ->
                        ShortsCard(
                            video = video,
                            onClick = { onVideoClick(video) },
                            onFocus = { focusedShort = it }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    "Trending Now",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 48.dp, bottom = 16.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(shorts.drop(10)) { video ->
                        ShortsCard(
                            video = video,
                            onClick = { onVideoClick(video) },
                            onFocus = { focusedShort = it }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ShortsHero(
    video: Video?,
    onWatchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .padding(horizontal = 48.dp, vertical = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Featured Poster
        video?.let {
            Surface(
                onClick = onWatchClick,
                modifier = Modifier
                    .width(300.dp)
                    .aspectRatio(9/16f),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(40.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(4.dp, Color.White)))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = it.thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                    )
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(64.dp))

        // Featured Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .background(Color.Red, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "SHORTS",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }
                Text(
                    text = "${video?.views ?: "0"} loops",
                    color = Color.Gray,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = video?.title ?: "Loading...",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    lineHeight = 72.sp,
                    letterSpacing = (-3).sp
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ShortInfoIcon(Icons.Default.ThumbUp, "Like")
                ShortInfoIcon(Icons.Default.Message, "Comment")
                ShortInfoIcon(Icons.Default.Share, "Share")
            }

            Spacer(modifier = Modifier.height(40.dp))

            Surface(
                onClick = onWatchClick,
                modifier = Modifier.height(64.dp),
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
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Watch Short", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun ShortInfoIcon(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

