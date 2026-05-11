@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui

import androidx.tv.material3.ExperimentalTvMaterial3Api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubekids.youtube.data.local.VideoDao
import com.youtubekids.youtube.data.local.toEntity
import com.youtubekids.youtube.data.local.toVideo
import com.youtubekids.youtube.data.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Profile(
    val id: String,
    val name: String,
    val avatar: String? = null,
    val mode: String // "kids" or "regular"
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val videoDao: VideoDao
) : ViewModel() {

    private val _currentProfile = MutableStateFlow<Profile?>(null)
    val currentProfile: StateFlow<Profile?> = _currentProfile

    private val _ambientThumbnail = MutableStateFlow<String?>(null)
    val ambientThumbnail: StateFlow<String?> = _ambientThumbnail

    private val _ambientColor = MutableStateFlow("#FFFFFF")
    val ambientColor: StateFlow<String> = _ambientColor

    private val _isAmbientMode = MutableStateFlow(false)
    val isAmbientMode: StateFlow<Boolean> = _isAmbientMode

    val watchHistory = videoDao.getVideosByType("history")
        .map { entities -> entities.map { it.toVideo() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedVideos = videoDao.getVideosByType("liked")
        .map { entities -> entities.map { it.toVideo() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchLater = videoDao.getVideosByType("watch_later")
        .map { entities -> entities.map { it.toVideo() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _globalVideo = MutableStateFlow<Video?>(null)
    val globalVideo: StateFlow<Video?> = _globalVideo

    private val _globalStreamUrl = MutableStateFlow<String?>(null)
    val globalStreamUrl: StateFlow<String?> = _globalStreamUrl

    private val _isGlobalPlaying = MutableStateFlow(false)
    val isGlobalPlaying: StateFlow<Boolean> = _isGlobalPlaying

    private val _isGlobalLoading = MutableStateFlow(false)
    val isGlobalLoading: StateFlow<Boolean> = _isGlobalLoading

    private val _globalProgress = MutableStateFlow(0f)
    val globalProgress: StateFlow<Float> = _globalProgress

    fun setProfile(profile: Profile?) {
        _currentProfile.value = profile
    }

    fun setAmbientState(thumbnail: String?, color: String = "#FFFFFF") {
        _ambientThumbnail.value = thumbnail
        _ambientColor.value = color
        _isAmbientMode.value = thumbnail != null
    }

    fun addToHistory(video: Video) {
        viewModelScope.launch {
            videoDao.insertVideo(video.toEntity("history"))
        }
    }

    fun toggleLiked(video: Video) {
        viewModelScope.launch {
            if (videoDao.isVideoInList(video.id, "liked")) {
                videoDao.deleteVideo(video.id, "liked")
            } else {
                videoDao.insertVideo(video.toEntity("liked"))
            }
        }
    }

    fun toggleWatchLater(video: Video) {
        viewModelScope.launch {
            if (videoDao.isVideoInList(video.id, "watch_later")) {
                videoDao.deleteVideo(video.id, "watch_later")
            } else {
                videoDao.insertVideo(video.toEntity("watch_later"))
            }
        }
    }

    fun setGlobalPlayback(video: Video?, streamUrl: String?, playing: Boolean = true) {
        _globalVideo.value = video
        _globalStreamUrl.value = streamUrl
        _isGlobalPlaying.value = playing
        _globalProgress.value = 0f
    }

    fun setGlobalPlaying(playing: Boolean) {
        _isGlobalPlaying.value = playing
    }

    fun setGlobalLoading(loading: Boolean) {
        _isGlobalLoading.value = loading
    }

    fun setGlobalProgress(progress: Float) {
        _globalProgress.value = progress.coerceIn(0f, 1f)
    }

    fun logout() {
        _currentProfile.value = null
    }
}
