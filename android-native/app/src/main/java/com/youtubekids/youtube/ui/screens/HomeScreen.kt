@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.repository.YouTubeRepository
import com.youtubekids.youtube.ui.AppViewModel
import com.youtubekids.youtube.ui.components.CategoryChips
import com.youtubekids.youtube.ui.components.HeroBanner
import com.youtubekids.youtube.ui.components.HorizontalRail
import com.youtubekids.youtube.ui.components.SingularityLoader

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onVideoClick: (Video) -> Unit,
    repository: YouTubeRepository,
    viewModel: AppViewModel = hiltViewModel()
) {
    var homeVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var trendingVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var categoryVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("Recommended") }
    var isLoading by remember { mutableStateOf(true) }
    
    val watchHistory by viewModel.watchHistory.collectAsState()

    // Initial data fetch with retry
    LaunchedEffect(Unit) {
        isLoading = true
        var retries = 0
        val maxRetries = 3
        while (retries < maxRetries) {
            try {
                homeVideos = repository.getHomeVideos()
                trendingVideos = repository.getTrending()
                // If we got data, stop retrying
                if (homeVideos.isNotEmpty() || trendingVideos.isNotEmpty()) break
            } catch (e: Exception) {
                e.printStackTrace()
            }
            retries++
            if (retries < maxRetries) {
                kotlinx.coroutines.delay(2000L * retries)
            }
        }
        isLoading = false
    }

    // Category fetch
    LaunchedEffect(selectedCategory) {
        if (selectedCategory != "Recommended" && selectedCategory != "Trending") {
            isLoading = true
            try {
                categoryVideos = repository.search(selectedCategory)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading && (if (selectedCategory == "Recommended") homeVideos.isEmpty() else categoryVideos.isEmpty())) {
        SingularityLoader()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 72.dp, bottom = 120.dp)
        ) {
            item {
                val heroVideos = when (selectedCategory) {
                    "Recommended" -> homeVideos.take(10).ifEmpty { trendingVideos.take(10) }
                    "Trending" -> trendingVideos.take(10)
                    else -> categoryVideos.take(10)
                }
                
                if (heroVideos.isNotEmpty()) {
                    HeroBanner(
                        videos = heroVideos,
                        onPlayClick = onVideoClick
                    )
                }
            }

            item {
                CategoryChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            if (selectedCategory == "Recommended") {
                if (watchHistory.isNotEmpty()) {
                    item {
                        HorizontalRail(
                            title = "Continue Watching",
                            videos = watchHistory,
                            onVideoClick = onVideoClick,
                            onVideoFocus = { viewModel.setAmbientState(it.thumbnail) }
                        )
                    }
                }
                if (homeVideos.isNotEmpty()) {
                    item {
                        HorizontalRail(
                            title = "Recommended",
                            videos = homeVideos,
                            onVideoClick = onVideoClick,
                            onVideoFocus = { viewModel.setAmbientState(it.thumbnail) }
                        )
                    }
                }
            } else if (selectedCategory == "Trending") {
                if (trendingVideos.isNotEmpty()) {
                    item {
                        HorizontalRail(
                            title = "Trending Now",
                            videos = trendingVideos,
                            onVideoClick = onVideoClick,
                            onVideoFocus = { viewModel.setAmbientState(it.thumbnail) }
                        )
                    }
                }
            } else {
                if (categoryVideos.isNotEmpty()) {
                    item {
                        HorizontalRail(
                            title = selectedCategory,
                            videos = categoryVideos,
                            onVideoClick = onVideoClick,
                            onVideoFocus = { viewModel.setAmbientState(it.thumbnail) }
                        )
                    }
                }
            }
        }
    }
}
