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
import androidx.compose.ui.text.style.TextOverflow
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
        // Ambient orbs
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

        // ── Back Button ──
        Surface(
            onClick = onClose,
            modifier = Modifier.padding(24.dp).size(40.dp).align(Alignment.TopStart),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color.White
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LocalContentColor.current, modifier = Modifier.size(20.dp))
            }
        }

        // ── Page indicator (right edge) ──
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(minOf(shorts.size, 8)) { i ->
                val isActive = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .size(if (isActive) 8.dp else 5.dp)
                        .background(
                            if (isActive) Color.White else Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
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
        // ── Floating Player Card ──
        Surface(
            onClick = {
                isPlaying = !isPlaying
                exoPlayer.playWhenReady = isPlaying
            },
            modifier = Modifier
                .height(580.dp)
                .width(326.dp),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(28.dp)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.4f))
                )
            ),
            glow = ClickableSurfaceDefaults.glow(
                focusedGlow = Glow(
                    elevationColor = Color.White.copy(alpha = 0.1f),
                    elevation = 16.dp
                )
            )
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

                // Bottom gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 300f
                            )
                        )
                )

                // Info overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    // Channel row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                        ) {
                            AsyncImage(
                                model = video.thumbnail,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "@${video.channel.replace(" ", "").lowercase()}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${video.views} views",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Progress bar at absolute bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        // ── Action Dock ──
        Column(
            modifier = Modifier.wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShortsAction(Icons.Default.ThumbUp, "Like", Color(0xFF00E5FF))
            ShortsAction(Icons.Default.ThumbDown, "Nah", Color(0xFFFF3D00))
            ShortsAction(Icons.Default.Message, "Chat", Color(0xFFFFD600))
            ShortsAction(Icons.Default.Share, "Send", Color(0xFF00E676))
            ShortsAction(Icons.Default.MoreVert, null, Color.White)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ShortsAction(icon: ImageVector, label: String?, color: Color) {
    var isFocused by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = {},
            modifier = Modifier.size(44.dp).onFocusChanged { isFocused = it.isFocused },
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.08f),
                focusedContainerColor = Color.White
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isFocused) Color.Black else color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (label != null) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label.uppercase(),
                color = if (isFocused) Color.White else Color.White.copy(alpha = 0.35f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
fun ShortsAmbientCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "shortsAmbient")

    val orb1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(tween(5000), RepeatMode.Reverse),
        label = "orb1"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-80).dp)
                .size(400.dp)
                .alpha(orb1Alpha)
                .background(Color(0xFFFF0048), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .size(500.dp)
                .alpha(0.06f)
                .background(Color(0xFF00E5FF), CircleShape)
        )
    }
}
