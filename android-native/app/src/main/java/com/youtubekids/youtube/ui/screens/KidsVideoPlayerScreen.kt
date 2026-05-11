@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class, androidx.annotation.OptIn::class)
package com.youtubekids.youtube.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.*
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.repository.YouTubeRepository
import com.youtubekids.youtube.ui.components.KidsVideoPlayerOverlay
import com.youtubekids.youtube.ui.components.MagicKidsLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(UnstableApi::class)
@Composable
fun KidsVideoPlayerScreen(
    video: Video,
    repository: YouTubeRepository,
    exoPlayer: ExoPlayer,
    onClose: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var controlsVisible by remember { mutableStateOf(true) }
    var upNext by remember { mutableStateOf<List<Video>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 1. Fetch Stream and Related Videos
    LaunchedEffect(video.id) {
        loading = true
        error = null
        try {
            val stream = repository.getStream(video.id)
            if (stream?.url != null) {
                exoPlayer.setMediaItem(MediaItem.fromUri(stream.url))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                
                // Fetch related kids content
                upNext = repository.getKidsCategory("explore").shuffled().take(10)
            } else {
                error = "Oops! This video is sleeping."
            }
        } catch (e: Exception) {
            error = "Network issue. Try again!"
        } finally {
            loading = false
        }
    }

    // 2. Track Progress and Visibility
    LaunchedEffect(Unit) {
        while (isActive) {
            if (exoPlayer.duration > 0) {
                progress = exoPlayer.currentPosition.toFloat() / exoPlayer.duration.toFloat()
            }
            delay(500)
        }
    }

    // Auto-hide controls
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(8000) // Longer delay for kids
            controlsVisible = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video View
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading State
        if (loading) {
            MagicKidsLoader()
        }

        // Overlay
        if (controlsVisible && !loading && error == null) {
            KidsVideoPlayerOverlay(
                title = video.title,
                isPlaying = isPlaying,
                progress = progress,
                upNext = upNext,
                onClose = onClose,
                onTogglePlay = {
                    isPlaying = !isPlaying
                    exoPlayer.playWhenReady = isPlaying
                },
                onSelectVideo = { nextVideo ->
                    // Navigation logic handled by parent (MainActivity)
                    // But here we can just update the current video if needed
                }
            )
        }

        // Error State
        if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(32.dp))
                    Surface(
                        onClick = onClose,
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        colors = ClickableSurfaceDefaults.colors(containerColor = Color.White)
                    ) {
                        Text("GO BACK", modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Interaction Area to show controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusable()
                .onKeyEvent {
                    controlsVisible = true
                    false
                }
        )
    }
}
