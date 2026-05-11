package com.youtubekids.youtube

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.youtubekids.youtube.data.repository.YouTubeRepository
import com.youtubekids.youtube.ui.components.AmbientMode
import com.youtubekids.youtube.ui.components.FloatingHeader
import com.youtubekids.youtube.ui.components.MiniPlayer
import com.youtubekids.youtube.ui.components.Sidebar
import com.youtubekids.youtube.ui.components.SplashScreen
import com.youtubekids.youtube.ui.screens.*
import com.youtubekids.youtube.ui.theme.YouTubeTVTheme
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.delay
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject lateinit var repository: YouTubeRepository
    @Inject lateinit var exoPlayer: ExoPlayer

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YouTubeTVTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    SplashScreen(onFinish = { showSplash = false })
                } else {
                    MainContent()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    @OptIn(ExperimentalTvMaterial3Api::class)
    @Composable
    fun MainContent() {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            val viewModel: com.youtubekids.youtube.ui.AppViewModel = hiltViewModel()
            val currentProfile by viewModel.currentProfile.collectAsState()
            val globalVideo by viewModel.globalVideo.collectAsState()
            val isGlobalPlaying by viewModel.isGlobalPlaying.collectAsState()
            val globalProgress by viewModel.globalProgress.collectAsState()
            val watchHistory by viewModel.watchHistory.collectAsState()

            var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
            var isIdle by remember { mutableStateOf(false) }

            LaunchedEffect(lastInteractionTime) {
                isIdle = false
                delay(300000) // 5 minutes of inactivity
                if (currentRoute != "player") {
                    isIdle = true
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onKeyEvent { 
                        lastInteractionTime = System.currentTimeMillis()
                        if (isIdle) {
                            isIdle = false
                            return@onKeyEvent true
                        }
                        false
                    }
            ) {
                if (currentProfile == null) {
                    ProfilePickerScreen(
                        onProfileSelected = { profile ->
                            viewModel.setProfile(profile)
                        }
                    )
                } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f)) {
                        // Sidebar is shown on most screens, but hidden in player and kids mode
                        if (currentRoute != "player" && currentRoute != "shorts-player" && currentRoute != "kids-home") {
                            Sidebar(
                                side = "left",
                                selectedRoute = currentRoute ?: "home",
                                currentProfile = currentProfile,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            NavHost(
                                navController = navController,
                                startDestination = if (currentProfile?.mode == "kids") "kids-home" else "home",
                                modifier = Modifier.fillMaxSize()
                            ) {
                            composable("home") {
                                HomeScreen(
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("player")
                                    },
                                    repository = repository
                                )
                            }
                            composable("kids-home") {
                                KidsHomeScreen(
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("kids-player")
                                    },
                                    repository = repository
                                )
                            }
                            composable("music") {
                                MusicScreen(
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("player")
                                    },
                                    repository = repository
                                )
                            }
                            composable("movies") {
                                MoviesScreen(
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("player")
                                    },
                                    repository = repository
                                )
                            }
                            composable("subscriptions") {
                                SubscriptionsScreen(
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("player")
                                    },
                                    repository = repository
                                )
                            }
                            composable("multiview") {
                                MultiviewScreen(
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("player")
                                    },
                                    repository = repository
                                )
                            }
                            composable("search") {
                                SearchScreen(
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("player")
                                    },
                                    repository = repository
                                )
                            }
                            composable("ask") {
                                AskScreen(onSearch = { query ->
                                    navController.navigate("search")
                                })
                            }
                            composable("library") {
                                LibraryScreen(
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("player")
                                    },
                                    onCategoryClick = { categoryId ->
                                        navController.navigate("category/$categoryId")
                                    }
                                )
                            }
                            composable("category/{categoryId}") { backStackEntry ->
                                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                                val videos by when(categoryId) {
                                    "liked" -> viewModel.likedVideos.collectAsState()
                                    "watch-later" -> viewModel.watchLater.collectAsState()
                                    else -> viewModel.watchHistory.collectAsState()
                                }
                                val title = when(categoryId) {
                                    "liked" -> "Liked Videos"
                                    "watch-later" -> "Watch Later"
                                    "history" -> "History"
                                    "downloads" -> "Downloads"
                                    else -> "Playlists"
                                }
                                CategoryScreen(
                                    title = title,
                                    videos = videos,
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("player")
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("live") {
                                LiveGuideScreen(
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("player")
                                    },
                                    repository = repository
                                )
                            }
                            composable("shorts") {
                                ShortsScreen(
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("shorts-player")
                                    },
                                    repository = repository
                                )
                            }
                            composable("shorts-player") {
                                val video by viewModel.globalVideo.collectAsState()
                                video?.let {
                                    ShortsPlayerScreen(
                                        initialVideo = it,
                                        repository = repository,
                                        exoPlayer = exoPlayer,
                                        onClose = { navController.popBackStack() }
                                    )
                                }
                            }
                            composable("channel/{channelId}/{channelName}/{channelAvatar}") { backStackEntry ->
                                val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
                                val channelName = backStackEntry.arguments?.getString("channelName") ?: ""
                                val channelAvatar = backStackEntry.arguments?.getString("channelAvatar") ?: ""
                                ChannelScreen(
                                    channelId = channelId,
                                    channelName = channelName,
                                    channelAvatar = channelAvatar,
                                    repository = repository,
                                    onVideoClick = { video ->
                                        viewModel.setGlobalPlayback(video, null)
                                        navController.navigate("player")
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("player") {
                                val video by viewModel.globalVideo.collectAsState()
                                video?.let {
                                    VideoPlayerScreen(
                                        video = it,
                                        repository = repository,
                                        exoPlayer = exoPlayer,
                                        onClose = { navController.popBackStack() }
                                    )
                                }
                            }
                            composable("kids-player") {
                                val video by viewModel.globalVideo.collectAsState()
                                video?.let {
                                    KidsVideoPlayerScreen(
                                        video = it,
                                        repository = repository,
                                        exoPlayer = exoPlayer,
                                        onClose = { navController.popBackStack() }
                                    )
                                }
                            }
                            composable("settings") {
                                SettingsScreen(
                                    onLogout = { 
                                        viewModel.logout()
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }

                        // Floating Header Pods
                        if (currentRoute != "player" && currentRoute != "shorts-player" && currentRoute != "kids-home" && currentRoute != "search") {
                            FloatingHeader(
                                currentProfile = currentProfile,
                                currentRoute = currentRoute,
                                onSearchClick = { navController.navigate("search") },
                                onProfileClick = { navController.navigate("settings") }
                            )
                        }

                        if (currentRoute != "player" && currentRoute != "shorts-player" && currentRoute != "kids-home") {
                            Sidebar(
                                side = "right",
                                selectedRoute = currentRoute ?: "home",
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }

                // MiniPlayer — in Column layout so D-pad Down can reach it
                    if (globalVideo != null && currentRoute != "player") {
                        MiniPlayer(
                            video = globalVideo,
                            isPlaying = isGlobalPlaying,
                            progress = globalProgress,
                            onTogglePlay = { viewModel.setGlobalPlayback(globalVideo, null, !isGlobalPlaying) },
                            onOpenFull = { navController.navigate("player") },
                            onClose = { viewModel.setGlobalPlayback(null, null) }
                        )
                    }
                }

                if (isIdle && watchHistory.isNotEmpty()) {
                    AmbientMode(videos = watchHistory)
                }
            }
        }
    }
    }
}
