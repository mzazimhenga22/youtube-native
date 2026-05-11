@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AmbientMode(
    videos: List<Video>,
    modifier: Modifier = Modifier
) {
    var activeIndex by remember { mutableIntStateOf(0) }
    var currentTime by remember { mutableStateOf("") }
    
    val currentVideo = if (videos.isNotEmpty()) videos[activeIndex] else null

    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            currentTime = sdf.format(Date())
            delay(1000)
        }
    }

    LaunchedEffect(videos) {
        while (videos.size > 1) {
            delay(15000)
            activeIndex = (activeIndex + 1) % videos.size
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (currentVideo != null) {
            AnimatedContent(
                targetState = currentVideo,
                transitionSpec = {
                    fadeIn(animationSpec = androidx.compose.animation.core.tween(2000)) togetherWith 
                    fadeOut(animationSpec = androidx.compose.animation.core.tween(2000))
                },
                label = "AmbientBackground"
            ) { video ->
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(48.dp)
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 120.sp
            )
            currentVideo?.let {
                Text(
                    text = it.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
