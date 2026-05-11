@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.repository.YouTubeRepository
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ShortsPlayerScreen(
    initialVideo: Video,
    repository: YouTubeRepository,
    exoPlayer: ExoPlayer,
    onClose: () -> Unit
) {
    var shorts by remember { mutableStateOf(listOf(initialVideo)) }
    val pagerState = rememberPagerState(pageCount = { shorts.size })
    
    LaunchedEffect(Unit) {
        val related = repository.search("shorts viral")
        shorts = listOf(initialVideo) + related.filter { it.id != initialVideo.id }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        // 1. Ambient Background Orbs
        ShortsAmbientCanvas()

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val video = shorts[page]
            ShortPage(
                video = video,
                isActive = pagerState.currentPage == page,
                exoPlayer = exoPlayer,
                repository = repository
            )
        }

        // Exit Button
        Surface(
            onClick = onClose,
            modifier = Modifier.padding(48.dp).size(48.dp).align(Alignment.TopStart),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color.White
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = if (MaterialTheme.colorScheme.onSurface == Color.Black) Color.Black else Color.White
                )
            }
        }
    }
}

@Composable
fun ShortPage(
    video: Video,
    isActive: Boolean,
    exoPlayer: ExoPlayer,
    repository: YouTubeRepository
) {
    var streamUrl by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(true) }

    LaunchedEffect(isActive) {
        if (isActive) {
            val stream = repository.getStream(video.id)
            streamUrl = stream?.url
            streamUrl?.let {
                exoPlayer.setMediaItem(MediaItem.fromUri(it))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Floating Player Card
        Surface(
            onClick = { 
                isPlaying = !isPlaying
                exoPlayer.playWhenReady = isPlaying
            },
            modifier = Modifier
                .height(800.dp)
                .width(450.dp),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(48.dp)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
            border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))))
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                if (isActive && streamUrl != null) {
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                player = exoPlayer
                                useController = false
                                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = video.thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.6f
                    )
                }

                // Info Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 500f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(32.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                        ) {
                            AsyncImage(
                                model = video.thumbnail,
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "@${video.channel.replace(" ", "").lowercase()}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${video.views} Views",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 32.sp,
                        maxLines = 2
                    )
                }

                // Progress Bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp, start = 32.dp, end = 32.dp)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    // Progress could be synced with exoPlayer here
                }
            }
        }

        Spacer(modifier = Modifier.width(64.dp))

        // Action Dock
        Column(
            modifier = Modifier.wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShortsAction(Icons.Default.ThumbUp, "Like", Color(0xFF00E5FF))
            ShortsAction(Icons.Default.ThumbDown, "Dislike", Color(0xFFFF3D00))
            ShortsAction(Icons.Default.Message, "Talk", Color(0xFFFFD600))
            ShortsAction(Icons.Default.Share, "Send", Color(0xFF00E676))
            ShortsAction(Icons.Default.MoreVert, null, Color.White)
        }
    }
}

@Composable
fun ShortsAction(icon: ImageVector, label: String?, color: Color) {
    var isFocused by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = {},
            modifier = Modifier.size(64.dp).onFocusChanged { isFocused = it.isFocused },
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color.White
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isFocused) Color.Black else color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        if (label != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                color = if (isFocused) Color.White else Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ShortsAmbientCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "shortsAmbient")
    
    val orb1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (-100).dp, y = (-100).dp)
                .size(500.dp)
                .alpha(orb1Alpha)
                .background(Color(0xFFFF0048), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(600.dp)
                .alpha(0.1f)
                .background(Color(0xFF00E5FF), CircleShape)
        )
    }
}

