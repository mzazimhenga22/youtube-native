package com.youtubekids.youtube.ui.player

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MergingMediaSource
import com.youtubekids.youtube.data.repository.YouTubeRepository

private const val TAG = "PlayerStream"

@androidx.annotation.OptIn(UnstableApi::class)
fun ExoPlayer.setYouTubeStream(stream: YouTubeRepository.StreamResult) {
    stop()
    clearMediaItems()

    val dataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent(YOUTUBE_USER_AGENT)
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15_000)
        .setReadTimeoutMs(30_000)
        .setDefaultRequestProperties(YOUTUBE_REQUEST_HEADERS)
    val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

    // Ensure ratebypass=yes on all URLs to help with throttle bypass
    val videoUrl = ensureRateBypass(stream.url)
    val audioUrl = stream.audioUrl?.let { ensureRateBypass(it) }

    Log.d(TAG, "Setting stream: mime=${stream.mimeType} adaptive=${stream.adaptive} hasAudio=${audioUrl != null}")
    Log.d(TAG, "Video URL: ${videoUrl.take(120)}...")

    if (audioUrl != null) {
        Log.d(TAG, "Audio URL: ${audioUrl.take(120)}...")
        val videoItem = MediaItem.Builder()
            .setUri(videoUrl)
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

    val builder = MediaItem.Builder().setUri(videoUrl)
    if (stream.mimeType.contains("mpegURL", ignoreCase = true) ||
        stream.mimeType.contains("x-mpegURL", ignoreCase = true) ||
        stream.mimeType.contains("dash", ignoreCase = true)) {
        builder.setMimeType(stream.mimeType)
    }
    setMediaSource(mediaSourceFactory.createMediaSource(builder.build()))
}

/**
 * Appends ratebypass=yes to YouTube stream URLs.
 * This hints YouTube's CDN to not throttle the stream.
 */
private fun ensureRateBypass(url: String): String {
    if (url.contains("ratebypass=")) return url
    val sep = if (url.contains("?")) "&" else "?"
    return "$url${sep}ratebypass=yes"
}

private const val YOUTUBE_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"

private val YOUTUBE_REQUEST_HEADERS = mapOf(
    "Accept-Language" to "en-US,en;q=0.9",
    "Origin" to "https://www.youtube.com",
    "Referer" to "https://www.youtube.com/"
)
