@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.repository.YouTubeRepository
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
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
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    // Stream state — managed at the screen level, not per-page
    var streamLoading by remember { mutableStateOf(true) }
    var streamReady by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val related = repository.search("shorts viral")
        shorts = listOf(initialVideo) + related.filter { it.id != initialVideo.id }
    }

    // Auto-focus so the Box receives D-pad key events immediately
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // ── Load stream whenever the current page changes ──
    LaunchedEffect(pagerState.currentPage) {
        val video = shorts.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        streamLoading = true
        streamReady = false

        // Stop previous playback
        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        try {
            val stream = repository.getStream(video.id)
            if (stream?.url != null) {
                val builder = MediaItem.Builder().setUri(stream.url)
                if (stream.mimeType.contains("mpegURL", ignoreCase = true)) {
                    builder.setMimeType(stream.mimeType)
                }
                exoPlayer.setMediaItem(builder.build())
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                isPlaying = true
                streamReady = true
            }
        } catch (_: Exception) { }
        streamLoading = false
    }

    // ── Track progress ──
    LaunchedEffect(Unit) {
        while (isActive) {
            if (exoPlayer.duration > 0) {
                progress = (exoPlayer.currentPosition.toFloat() / exoPlayer.duration).coerceIn(0f, 1f)
            }
            delay(500)
        }
    }

    // Player listener for play state sync
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    // Request focus on the active page whenever it changes
    LaunchedEffect(pagerState.currentPage) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        // Ambient orbs
        ShortsAmbientCanvas()

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false // Disable touch scroll; D-pad controls paging
        ) { page ->
            val video = shorts[page]
            val isCurrentPage = pagerState.currentPage == page

            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // ── Floating Player Card ──
                Surface(
                    onClick = {
                        if (isCurrentPage) {
                            isPlaying = !isPlaying
                            exoPlayer.playWhenReady = isPlaying
                        }
                    },
                    modifier = Modifier
                        .height(580.dp)
                        .width(326.dp)
                        .run { if (isCurrentPage) androidx.compose.ui.focus.focusRequester(focusRequester) else this }
                        .onKeyEvent { event ->
                            if (event.type == androidx.compose.ui.input.key.KeyEventType.KeyDown) {
                                when (event.key) {
                                    androidx.compose.ui.input.key.Key.DirectionDown -> {
                                        if (pagerState.currentPage < shorts.size - 1) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        }
                                        true
                                    }
                                    androidx.compose.ui.input.key.Key.DirectionUp -> {
                                        if (pagerState.currentPage > 0) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                            }
                                        }
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        },
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
                        // Show the PlayerView ONLY on the current active page
                        if (isCurrentPage && streamReady) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        useController = false
                                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        layoutParams = FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        // Attach player in factory
                                        player = exoPlayer
                                    }
                                },
                                update = { playerView ->
                                    // Re-attach player on every recomposition to ensure
                                    // this PlayerView is the one rendering the video
                                    playerView.player = exoPlayer
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Thumbnail fallback when not active or stream loading
                            AsyncImage(
                                model = video.thumbnail,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.6f
                            )
                        }

                        // Loading spinner
                        if (isCurrentPage && streamLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                SingularityDot()
                            }
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
                        if (isCurrentPage) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .fillMaxHeight()
                                        .background(Color.Red, CircleShape)
                                )
                            }
                        } else {
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
private fun SingularityDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "shortsDot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "dotPulse"
    )
    Box(
        modifier = Modifier
            .size(16.dp)
            .alpha(dotAlpha)
            .background(Color.White, CircleShape)
    )
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
