@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.tv.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.repository.YouTubeRepository
import com.youtubekids.youtube.ui.AppViewModel
import com.youtubekids.youtube.ui.components.KidsVideoPlayerOverlay
import com.youtubekids.youtube.ui.components.MusicPlayerOverlay
import com.youtubekids.youtube.ui.components.SingularityLoader
import com.youtubekids.youtube.ui.components.VideoPlayerOverlay
import com.youtubekids.youtube.ui.components.StatsForNerds
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    video: Video,
    repository: YouTubeRepository,
    exoPlayer: ExoPlayer,
    viewModel: AppViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val context = LocalContext.current
    
    var currentVideo by remember { mutableStateOf(video) }
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentTime by remember { mutableStateOf("0:00") }
    var durationText by remember { mutableStateOf("0:00") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }
    var relatedVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var showResumePrompt by remember { mutableStateOf(false) }
    var savedProgress by remember { mutableLongStateOf(0L) }
    var isCommentsOpen by remember { mutableStateOf(false) }
    var isLyricsOpen by remember { mutableStateOf(false) }
    var showUpNext by remember { mutableStateOf(false) }
    var upNextCountdown by remember { mutableIntStateOf(10) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showStats by remember { mutableStateOf(false) }
    var currentChapter by remember { mutableStateOf<com.youtubekids.youtube.data.model.Chapter?>(null) }
    var comments by remember { mutableStateOf<List<com.youtubekids.youtube.ui.components.Comment>>(emptyList()) }
    
    val layoutProgress = remember { Animatable(0f) }
    
    LaunchedEffect(isCommentsOpen, isLyricsOpen) {
        layoutProgress.animateTo(
            targetValue = if (isCommentsOpen || isLyricsOpen) 1f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
        )
    }
    
    val currentProfile by viewModel.currentProfile.collectAsState()

    // Fetch data and prepare player
    LaunchedEffect(currentVideo.id, retryKey) {
        isLoading = true
        error = null
        try {
            // Pause current playback if any
            exoPlayer.pause()
            
            val details = repository.getVideoDetails(currentVideo.id)
            if (details != null) {
                currentVideo = details
            }

            val related = if (currentProfile?.mode == "kids") {
                repository.getKidsUpNext(currentVideo.id)
            } else {
                repository.getUpNext(currentVideo.id)
            }
            relatedVideos = related
            
            // Fetch real comments
            launch {
                val fetchedComments = repository.getComments(currentVideo.id)
                comments = fetchedComments.map { 
                    com.youtubekids.youtube.ui.components.Comment(it.id, it.user, it.text, it.avatar, it.likes)
                }
            }

            val stream = repository.getStream(currentVideo.id)
            if (stream != null) {
                val mediaItem = MediaItem.Builder()
                    .setUri(stream.url)
                    .setMimeType(stream.mimeType)
                    .build()
                
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
                isPlaying = true
                
                viewModel.addToHistory(currentVideo)
                viewModel.setGlobalPlayback(currentVideo, stream.url)
            } else {
                error = "This video cannot be played right now"
            }
        } catch (e: Exception) {
            error = "Network error: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    // Progress updates
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (exoPlayer.duration > 0) {
                val p = (exoPlayer.currentPosition.toFloat() / exoPlayer.duration).coerceIn(0f, 1f)
                progress = p
                currentTime = formatTime(exoPlayer.currentPosition)
                durationText = formatTime(exoPlayer.duration)
                viewModel.setGlobalProgress(p)
                
                // Track Chapters
                currentVideo.chapters?.let { chapters ->
                    val chapter = chapters.reversed().find { exoPlayer.currentPosition >= it.time * 1000 }
                    if (chapter != currentChapter) {
                        currentChapter = chapter
                    }
                }

                // Show Up Next card when 90% through
                if (p > 0.90f && relatedVideos.isNotEmpty() && !showUpNext) {
                    showUpNext = true
                }
            }
            delay(500) // Smoother update
        }
    }

    LaunchedEffect(showUpNext) {
        if (showUpNext) {
            for (i in 10 downTo 0) {
                upNextCountdown = i
                if (i == 0 && relatedVideos.isNotEmpty()) {
                    currentVideo = relatedVideos[0]
                    showUpNext = false
                }
                delay(1000)
            }
        }
    }

    // Player listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayerError(e: PlaybackException) {
                error = "Playback failed: ${e.message}"
                isLoading = false
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                viewModel.setGlobalPlaying(playing)
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            SingularityLoader(transparent = true, ambientThumbnail = currentVideo.thumbnail)
        } else if (error != null) {
            ErrorState(
                thumbnail = currentVideo.thumbnail,
                error = error!!,
                onRetry = { retryKey++ },
                onBack = onClose
            )
        } else {
            when {
                currentProfile?.mode == "kids" -> {
                    KidsVideoPlayerOverlay(
                        title = currentVideo.title,
                        isPlaying = isPlaying,
                        progress = progress,
                        upNext = relatedVideos,
                        onTogglePlay = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        onSelectVideo = { nextVideo ->
                            currentVideo = nextVideo
                        },
                        onClose = onClose
                    )
                }
                currentVideo.duration == "" || currentVideo.isLive -> {
                     MusicPlayerOverlay(
                        video = currentVideo,
                        isPlaying = isPlaying,
                        progress = progress,
                        currentTime = currentTime,
                        duration = durationText,
                        recommendations = relatedVideos,
                        onTogglePlay = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        onSeek = { delta ->
                            val newPos = exoPlayer.currentPosition + (delta * 1000).toLong()
                            exoPlayer.seekTo(newPos.coerceIn(0, exoPlayer.duration))
                        },
                        onNext = {
                            if (relatedVideos.isNotEmpty()) {
                                currentVideo = relatedVideos[0]
                            }
                        },
                        onClose = onClose
                    )
                }
                else -> {
                    VideoPlayerOverlay(
                        video = currentVideo,
                        isPlaying = isPlaying,
                        progress = progress,
                        currentTime = currentTime,
                        duration = durationText,
                        onTogglePlay = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        onSeek = { delta ->
                            val newPos = exoPlayer.currentPosition + (delta * 1000).toLong()
                            exoPlayer.seekTo(newPos.coerceIn(0, exoPlayer.duration))
                        },
                        onToggleComments = {
                            isCommentsOpen = !isCommentsOpen
                            isLyricsOpen = false
                        },
                        onToggleLyrics = {
                            isLyricsOpen = !isLyricsOpen
                            isCommentsOpen = false
                        },
                        onToggleStats = { showStats = !showStats },
                        onSetSpeed = { speed ->
                            playbackSpeed = speed
                            exoPlayer.setPlaybackSpeed(speed)
                        },
                        currentSpeed = playbackSpeed,
                        currentChapter = currentChapter?.title,
                        onClose = onClose
                    )
                }
            }

            // Stats for Nerds
            if (showStats) {
                StatsForNerds(
                    video = currentVideo,
                    onClose = { showStats = false }
                )
            }

            // Up Next Countdown Card
            AnimatedVisibility(
                visible = showUpNext,
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut() + slideOutHorizontally { it },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 48.dp, bottom = 120.dp)
            ) {
                UpNextCard(
                    video = relatedVideos.getOrNull(0),
                    countdown = upNextCountdown,
                    onCancel = { showUpNext = false },
                    onPlayNow = {
                        currentVideo = relatedVideos[0]
                        showUpNext = false
                    }
                )
            }

            // Resume Prompt
            if (showResumePrompt) {
                ResumePrompt(
                    timeText = formatTime(savedProgress),
                    onResume = {
                        exoPlayer.seekTo(savedProgress)
                        showResumePrompt = false
                    },
                    onStartOver = { showResumePrompt = false }
                )
            }
        }

        // Sidebar Panels
        Row(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(
                visible = isCommentsOpen,
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut() + slideOutHorizontally { it }
            ) {
                com.youtubekids.youtube.ui.components.CommentsSidebar(
                    comments = comments,
                    onClose = { isCommentsOpen = false }
                )
            }
            AnimatedVisibility(
                visible = isLyricsOpen,
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut() + slideOutHorizontally { it }
            ) {
                com.youtubekids.youtube.ui.components.LyricsSidebar(
                    lyrics = listOf("Welcome to the show", "It's time to go", "Into the unknown", "Where the light has grown"),
                    activeLineIndex = 0,
                    onClose = { isLyricsOpen = false }
                )
            }
        }
    }
}

@Composable
fun UpNextCard(video: Video?, countdown: Int, onCancel: () -> Unit, onPlayNow: () -> Unit) {
    Surface(
        onClick = onPlayNow,
        modifier = Modifier.width(380.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF0A0A0A).copy(alpha = 0.85f)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Up Next", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.background(Color.Red, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("${countdown}s", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
                androidx.tv.material3.IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                AsyncImage(
                    model = video?.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.size(140.dp, 80.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(video?.title ?: "", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 2)
                    Text(video?.channel ?: "", color = Color.Gray, fontSize = 13.sp)
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.White.copy(alpha = 0.1f))) {
                Box(modifier = Modifier.fillMaxWidth(1f - (countdown / 10f)).fillMaxHeight().background(Color.Red))
            }
        }
    }
}

@Composable
fun ResumePrompt(timeText: String, onResume: () -> Unit, onStartOver: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
        Surface(
            onClick = {},
            modifier = Modifier.width(520.dp),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(32.dp)),
            colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF18181B))
        ) {
            Column(modifier = Modifier.padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Continue watching?", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Text("You stopped at $timeText. Resume?", color = Color.Gray, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(onClick = onResume, shape = ClickableSurfaceDefaults.shape(CircleShape), colors = ClickableSurfaceDefaults.colors(containerColor = Color.White)) {
                        Text("Resume", modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp), color = Color.Black, fontWeight = FontWeight.Black)
                    }
                    Surface(onClick = onStartOver, shape = ClickableSurfaceDefaults.shape(CircleShape), colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f), focusedContainerColor = Color.White)) {
                        Text("Start Over", modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp), color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    thumbnail: String,
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = thumbnail,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Video unavailable",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 620.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.colors(containerColor = Color.White),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text("Try Again", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text("Go Back", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms < 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
