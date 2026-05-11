@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.repository.YouTubeRepository
import com.youtubekids.youtube.ui.components.HorizontalRail

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelScreen(
    channelId: String,
    channelName: String,
    channelAvatar: String?,
    repository: YouTubeRepository,
    onVideoClick: (Video) -> Unit,
    onBack: () -> Unit
) {
    var videos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var activeTab by remember { mutableStateOf("Home") }

    LaunchedEffect(channelId) {
        videos = repository.search(channelName)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1600",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFF0F0F0F).copy(alpha = 0.95f))
                                )
                            )
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 64.dp)
                        .offset(y = (-80).dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        AsyncImage(
                            model = channelAvatar ?: "https://i.pravatar.cc/150?u=$channelName",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(40.dp))

                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = channelName,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-2).sp,
                                    fontSize = 64.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF3EA6FF),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "@${channelName.lowercase().replace(" ", "")} • 12.4M Subscribers • 1.2K Videos",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFA1A1AA),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Row(modifier = Modifier.padding(top = 32.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Button(onClick = {}) {
                                Text("Subscribe", fontWeight = FontWeight.Black, fontSize = 20.sp)
                            }
                            ChannelIconButton(icon = Icons.Default.Notifications, onClick = {})
                            ChannelIconButton(icon = Icons.Default.Search, onClick = {})
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 64.dp)
                        .padding(bottom = 48.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(48.dp)
                ) {
                    listOf("Home", "Videos", "Shorts", "Live", "Playlists").forEach { tab ->
                        val isSelected = activeTab == tab
                        Surface(
                            onClick = { activeTab = tab },
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                            colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent)
                        ) {
                            Column {
                                Text(
                                    text = tab,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp
                                    ),
                                    color = if (isSelected) Color.White else Color(0xFFA1A1AA)
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .height(4.dp)
                                            .fillMaxWidth()
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                HorizontalRail(
                    title = "Latest uploads",
                    videos = videos.take(8),
                    onVideoClick = onVideoClick
                )
            }
            item {
                HorizontalRail(
                    title = "Popular videos",
                    videos = videos.reversed().take(8),
                    onVideoClick = onVideoClick
                )
            }
        }

        Surface(
            onClick = onBack,
            modifier = Modifier
                .padding(40.dp)
                .size(64.dp)
                .align(Alignment.TopStart),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(containerColor = Color.Black.copy(alpha = 0.4f)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ChannelIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}
