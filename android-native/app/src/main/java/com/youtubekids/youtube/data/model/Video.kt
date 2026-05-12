package com.youtubekids.youtube.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Video(
    val id: String,
    val title: String,
    val channel: String,
    val channelId: String? = null,
    val views: String,
    val thumbnail: String,
    val duration: String,
    val publishedAt: String? = null,
    val streamUrl: String? = null,
    val isLive: Boolean = false,
    val viewerCount: String? = null,
    val chapters: List<Chapter>? = null,
    val description: String? = null,
    val contentType: String = "video" // "video", "music", "shorts", "kids", "movie", "live"
)

@Serializable
data class Chapter(
    val title: String,
    val time: Int // in seconds
)
