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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.repository.YouTubeRepository
import com.youtubekids.youtube.ui.components.HorizontalRail
import kotlinx.coroutines.delay

data class Channel(val name: String, val id: String, val avatar: String, val subCount: String, val isLive: Boolean)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    onVideoClick: (Video) -> Unit,
    repository: YouTubeRepository
) {
    var subRails by remember { mutableStateOf<Map<String, List<Video>>>(emptyMap()) }
    var focusedThumbnail by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val channels = listOf(
        Channel("MrBeast", "mrbeast", "https://yt3.googleusercontent.com/ytc/AIdro_nO_96S5V58U-t36T0R-T1x798v1=s176-c-k-c0x00ffffff-no-rj", "250M", true),
        Channel("MKBHD", "mkbhd", "https://yt3.googleusercontent.com/lkH3_nbB9667Y6G96fW3D8100.png", "18M", false),
        Channel("Veritasium", "veritasium", "https://yt3.googleusercontent.com/ytc/AIdro_n_96S5V58U-t36T0R-T1x798v1=s176-c-k-c0x00ffffff-no-rj", "15M", true),
        Channel("Fireship", "fireship", "https://yt3.googleusercontent.com/ytc/AIdro_nO_96S5V58U-t36T0R-T1x798v1=s176-c-k-c0x00ffffff-no-rj", "3M", false),
        Channel("Lofi Girl", "lofigirl", "https://yt3.googleusercontent.com/ytc/AIdro_nO_96S5V58U-t36T0R-T1x798v1=s176-c-k-c0x00ffffff-no-rj", "14M", true)
    )
    
    var focusedChannel by remember { mutableStateOf(channels[0]) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val trending = repository.getTrending()
            val recent = repository.search("new videos from my subscriptions")
            
            subRails = mapOf(
                "New Today" to trending.take(8),
                "This Week" to recent.take(12),
                "Recently Uploaded" to trending.reversed().take(8)
            )
            if (trending.isNotEmpty()) focusedThumbnail = trending[0].thumbnail
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        // 1. Ambient Background
        AnimatedContent(
            targetState = focusedThumbnail,
            transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(500)) },
            label = "subsBgFade"
        ) { thumb ->
            thumb?.let {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = it,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.3f
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 2. Cinematic Channel Banner
            item {
                SubscriptionHero(
                    channel = focusedChannel,
                    onLatestClick = { /* Navigate to latest video */ }
                )
            }

            // 3. Channel Nav Bar
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    items(channels) { channel ->
                        var isFocused by remember { mutableStateOf(false) }
                        Surface(
                            onClick = { /* Navigate to channel */ },
                            modifier = Modifier.wrapContentSize().onFocusChanged { 
                                isFocused = it.isFocused
                                if (it.isFocused) {
                                    focusedChannel = channel
                                }
                            },
                            shape = ClickableSurfaceDefaults.shape(CircleShape),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(100.dp)
                            ) {
                                Box(contentAlignment = Alignment.BottomCenter) {
                                    AsyncImage(
                                        model = channel.avatar,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                            .background(Color.DarkGray),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (channel.isLive) {
                                        Box(
                                            modifier = Modifier
                                                .offset(y = 4.dp)
                                                .background(Color.Red, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "LIVE",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = channel.name,
                                    color = if (isFocused) Color.White else Color.Gray,
                                    fontSize = 18.sp,
                                    fontWeight = if (isFocused) FontWeight.Black else FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                if (isFocused) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(4.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Content Rails
            subRails.forEach { (title, videos) ->
                item {
                    HorizontalRail(
                        title = title,
                        videos = videos,
                        onVideoClick = onVideoClick,
                        onVideoFocus = { focusedThumbnail = it.thumbnail }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SubscriptionHero(
    channel: Channel,
    onLatestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .padding(horizontal = 48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedContent(
            targetState = channel,
            transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(300)) },
            label = "channelBannerFade"
        ) { targetChannel ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                AsyncImage(
                    model = targetChannel.avatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray),
                    contentScale = ContentScale.Crop
                )
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = targetChannel.name,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-2).sp
                            ),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Official Artist", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "${targetChannel.subCount} Subscribers • 1.2K Videos",
                        color = Color.Gray,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                onClick = { /* Unsubscribe logic */ },
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
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Subscribed", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }
            
            Surface(
                onClick = { /* Notification settings */ },
                modifier = Modifier.size(64.dp),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
            
            Surface(
                onClick = onLatestClick,
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
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Latest Video", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

