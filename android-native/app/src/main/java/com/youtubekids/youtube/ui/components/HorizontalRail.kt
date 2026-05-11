@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.youtubekids.youtube.data.model.Video

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HorizontalRail(
    title: String,
    videos: List<Video>,
    onVideoClick: (Video) -> Unit,
    onVideoFocus: (Video) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(videos) { video ->
                VideoCard(
                    video = video,
                    onClick = { onVideoClick(video) },
                    onFocus = { onVideoFocus(video) }
                )
            }
        }
    }
}
