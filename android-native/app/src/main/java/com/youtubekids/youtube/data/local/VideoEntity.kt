package com.youtubekids.youtube.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.youtubekids.youtube.data.model.Video

@Entity(tableName = "videos", primaryKeys = ["id", "type"])
data class VideoEntity(
    val id: String,
    val type: String, // "history", "liked", "watch_later"
    val title: String,
    val channel: String,
    val views: String,
    val thumbnail: String,
    val duration: String,
    val publishedAt: String,
    @ColumnInfo(defaultValue = "video")
    val contentType: String = "video" // "video", "music", "shorts", "kids", "movie", "live"
)

fun Video.toEntity(type: String): VideoEntity = VideoEntity(
    id = id,
    title = title,
    channel = channel,
    views = views,
    thumbnail = thumbnail,
    duration = duration,
    publishedAt = publishedAt ?: "",
    type = type,
    contentType = contentType
)

fun VideoEntity.toVideo(): Video = Video(
    id = id,
    title = title,
    channel = channel,
    views = views,
    thumbnail = thumbnail,
    duration = duration,
    publishedAt = publishedAt,
    contentType = contentType
)

