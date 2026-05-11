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
        // Ambient background
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
                        alpha = 0.35f
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(
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
            contentPadding = PaddingValues(top = 72.dp, bottom = 120.dp)
        ) {
            // Hero
            item {
                ShortsHero(
                    video = focusedShort,
                    onWatchClick = { focusedShort?.let { onVideoClick(it) } }
                )
            }

            // Categories
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 80.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { category ->
                        var isFocused by remember { mutableStateOf(false) }
                        Surface(
                            onClick = {},
                            modifier = Modifier.height(40.dp).onFocusChanged { isFocused = it.isFocused },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color.White.copy(alpha = 0.06f),
                                focusedContainerColor = Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(category.icon, contentDescription = null,
                                    tint = if (isFocused) category.color else category.color.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp))
                                Text(category.label,
                                    color = if (isFocused) Color.Black else Color.White,
                                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Viral Shorts rail
            item {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text("Viral Shorts", color = Color.White, fontSize = 22.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp,
                        modifier = Modifier.padding(start = 80.dp, bottom = 12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(viralShorts, key = { it.id }) { video ->
                            ShortsCard(
                                video = video,
                                onClick = { onVideoClick(video) },
                                onFocus = { focusedShort = it }
                            )
                        }
                    }
                }
            }

            // Trending rail
            item {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text("Trending Now", color = Color.White, fontSize = 22.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp,
                        modifier = Modifier.padding(start = 80.dp, bottom = 12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(shorts.drop(10), key = { it.id }) { video ->
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
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ShortsHero(video: Video?, onWatchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 80.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Featured poster (portrait)
        video?.let {
            Surface(
                onClick = onWatchClick,
                modifier = Modifier.width(200.dp).aspectRatio(9f / 16f),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(androidx.compose.foundation.BorderStroke(3.dp, Color.White))
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = it.thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)))
                    ))
                    // Play circle
                    Box(
                        modifier = Modifier.size(48.dp).align(Alignment.Center)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(40.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            // Badge
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFF4400), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SHORTS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    }
                }
                Text("${video?.views ?: "0"} loops", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = video?.title ?: "Loading...",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 38.sp,
                letterSpacing = (-1).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 500.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Social actions
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                ShortInfoIcon(Icons.Default.ThumbUp, "Like")
                ShortInfoIcon(Icons.Default.Message, "Comment")
                ShortInfoIcon(Icons.Default.Share, "Share")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Watch button
            Surface(
                onClick = onWatchClick,
                modifier = Modifier.height(48.dp),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                colors = ClickableSurfaceDefaults.colors(containerColor = Color.White, focusedContainerColor = Color(0xFFFF4400))
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Watch Short", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ShortInfoIcon(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
