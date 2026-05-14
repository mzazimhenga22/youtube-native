package com.youtubekids.youtube.ui.player

import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.youtubekids.youtube.data.repository.YouTubeRepository

@androidx.annotation.OptIn(UnstableApi::class)
fun ExoPlayer.setYouTubeStream(stream: YouTubeRepository.StreamResult) {
    val audioUrl = stream.audioUrl
    if (audioUrl != null) {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Android) AppleWebKit/537.36 Chrome/130.0.0.0 Mobile Safari/537.36")

        val videoItem = MediaItem.Builder()
            .setUri(stream.url)
            .setMimeType(stream.mimeType)
            .build()
        val audioItem = MediaItem.Builder()
            .setUri(audioUrl)
            .setMimeType(stream.audioMimeType ?: "audio/mp4")
            .build()

        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoItem)
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioItem)
        setMediaSource(MergingMediaSource(videoSource, audioSource))
        return
    }

    val builder = MediaItem.Builder().setUri(stream.url)
    if (stream.mimeType.contains("mpegURL", ignoreCase = true) ||
        stream.mimeType.contains("x-mpegURL", ignoreCase = true) ||
        stream.mimeType.contains("dash", ignoreCase = true)) {
        builder.setMimeType(stream.mimeType)
    }
    setMediaItem(builder.build())
}
