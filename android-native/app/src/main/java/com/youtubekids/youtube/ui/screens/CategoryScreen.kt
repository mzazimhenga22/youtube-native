@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.ui.components.VideoCard

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryScreen(
    title: String,
    videos: List<Video>,
    onVideoClick: (Video) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(64.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${videos.size} Videos",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )
        }

        if (videos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No videos in this list yet",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Gray.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(300.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items(videos) { video ->
                    VideoCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
