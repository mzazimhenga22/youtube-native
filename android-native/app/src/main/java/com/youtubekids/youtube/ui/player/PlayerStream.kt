package com.youtubekids.youtube.ui.player

import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MergingMediaSource
import com.youtubekids.youtube.data.repository.YouTubeRepository

@androidx.annotation.OptIn(UnstableApi::class)
fun ExoPlayer.setYouTubeStream(stream: YouTubeRepository.StreamResult) {
    stop()
    clearMediaItems()

    val dataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent(YOUTUBE_USER_AGENT)
        .setAllowCrossProtocolRedirects(true)
        .setDefaultRequestProperties(YOUTUBE_REQUEST_HEADERS)
    val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

    val audioUrl = stream.audioUrl
    if (audioUrl != null) {
        val videoItem = MediaItem.Builder()
            .setUri(stream.url)
            .setMimeType(stream.mimeType)
            .build()
        val audioItem = MediaItem.Builder()
            .setUri(audioUrl)
            .setMimeType(stream.audioMimeType ?: "audio/mp4")
            .build()

        val videoSource = mediaSourceFactory.createMediaSource(videoItem)
        val audioSource = mediaSourceFactory.createMediaSource(audioItem)
        setMediaSource(MergingMediaSource(videoSource, audioSource))
        return
    }

    val builder = MediaItem.Builder().setUri(stream.url)
    if (stream.mimeType.contains("mpegURL", ignoreCase = true) ||
        stream.mimeType.contains("x-mpegURL", ignoreCase = true) ||
        stream.mimeType.contains("dash", ignoreCase = true)) {
        builder.setMimeType(stream.mimeType)
    }
    setMediaSource(mediaSourceFactory.createMediaSource(builder.build()))
}

private const val YOUTUBE_USER_AGENT =
    "Mozilla/5.0 (Android) AppleWebKit/537.36 Chrome/130.0.0.0 Mobile Safari/537.36"

private val YOUTUBE_REQUEST_HEADERS = mapOf(
    "Accept-Language" to "en-US,en;q=0.9",
    "Origin" to "https://www.youtube.com",
    "Referer" to "https://www.youtube.com/"
)
