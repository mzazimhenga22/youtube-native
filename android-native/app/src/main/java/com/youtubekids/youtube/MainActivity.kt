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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusGroup
import androidx.compose.ui.focus.focusRestorer
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
        
        val contentFocusRequester = remember { FocusRequester() }
        val leftSidebarFocusRequester = remember { FocusRequester() }
        val rightSidebarFocusRequester = remember { FocusRequester() }

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
                            // Reset navigation to the appropriate home for the selected profile
                            val startRoute = if (profile.mode == "kids") "kids-home" else "home"
                            navController.navigate(startRoute) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                // Full-screen layered layout: content fills everything,
                // sidebars + header float on top as overlays
                Box(modifier = Modifier.fillMaxSize()) {
                    // ── Layer 1: Full-width content ──
                    // Wrap NavHost in a focusable Box to manage entry/exit focus properly
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(contentFocusRequester)
                            .focusProperties {
                                left = leftSidebarFocusRequester
                                right = rightSidebarFocusRequester
                            }
                            .focusRestorer()
                            .focusGroup()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = if (currentProfile?.mode == "kids") "kids-home" else "home",
                            modifier = Modifier.fillMaxSize()
                        ) {
                                composable("home") {
                                    HomeScreen(
                                        onVideoClick = { video ->
                                            val tagged = video.copy(contentType = "video")
                                            viewModel.setGlobalPlayback(tagged, null)
                                            navController.navigate("player")
                                        },
                                        repository = repository
                                    )
                                }
                                composable("kids-home") {
                                    KidsHomeScreen(
                                        onVideoClick = { video ->
                                            val tagged = video.copy(contentType = "kids")
                                            viewModel.setGlobalPlayback(tagged, null)
                                            navController.navigate("kids-player")
                                        },
                                        repository = repository
                                    )
                                }
                                composable("music") {
                                    MusicScreen(
                                        onVideoClick = { video ->
                                            val tagged = video.copy(contentType = "music")
                                            viewModel.setGlobalPlayback(tagged, null)
                                            navController.navigate("music-player")
                                        },
                                        repository = repository
                                    )
                                }
                                composable("movies") {
                                    MoviesScreen(
                                        onVideoClick = { video ->
                                            val tagged = video.copy(contentType = "movie")
                                            viewModel.setGlobalPlayback(tagged, null)
                                            navController.navigate("player")
                                        },
                                        repository = repository
                                    )
                                }
                                composable("subscriptions") {
                                    SubscriptionsScreen(
                                        onVideoClick = { video ->
                                            val tagged = video.copy(contentType = "video")
                                            viewModel.setGlobalPlayback(tagged, null)
                                            navController.navigate("player")
                                        },
                                        repository = repository
                                    )
                                }
                                composable("multiview") {
                                    MultiviewScreen(
                                        onVideoClick = { video ->
                                            val tagged = video.copy(contentType = "video")
                                            viewModel.setGlobalPlayback(tagged, null)
                                            navController.navigate("player")
                                        },
                                        repository = repository
                                    )
                                }
                                composable("search") {
                                    SearchScreen(
                                        onVideoClick = { video ->
                                            val tagged = video.copy(contentType = "video")
                                            viewModel.setGlobalPlayback(tagged, null)
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
                                            // Library preserves whatever contentType was saved
                                            viewModel.setGlobalPlayback(video, null)
                                            when (video.contentType) {
                                                "music" -> navController.navigate("music-player")
                                                "shorts" -> navController.navigate("shorts-player")
                                                "kids" -> navController.navigate("kids-player")
                                                else -> navController.navigate("player")
                                            }
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
                                            when (video.contentType) {
                                                "music" -> navController.navigate("music-player")
                                                "shorts" -> navController.navigate("shorts-player")
                                                "kids" -> navController.navigate("kids-player")
                                                else -> navController.navigate("player")
                                            }
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                composable("live") {
                                    LiveGuideScreen(
                                        onVideoClick = { video ->
                                            val tagged = video.copy(contentType = "live")
                                            viewModel.setGlobalPlayback(tagged, null)
                                            navController.navigate("player")
                                        },
                                        repository = repository
                                    )
                                }
                                composable("shorts") {
                                    ShortsScreen(
                                        onVideoClick = { video ->
                                            val tagged = video.copy(contentType = "shorts")
                                            viewModel.setGlobalPlayback(tagged, null)
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
                                            onClose = { 
                                                viewModel.setGlobalPlayback(null, null)
                                                navController.popBackStack() 
                                            }
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
                                            onClose = {
                                                viewModel.setGlobalPlayback(null, null)
                                                navController.popBackStack()
                                            }
                                        )
                                    }
                                }
                                composable("music-player") {
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
                                            val tagged = video.copy(contentType = "video")
                                            viewModel.setGlobalPlayback(tagged, null)
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
                                composable("settings") {
                                    SettingsScreen(
                                        onLogout = { 
                                            viewModel.logout()
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                        }
                    }

                    // ── Layer 2: Floating Header (top) ──
                    if (currentRoute != "player" && currentRoute != "music-player" && currentRoute != "shorts-player" && currentRoute != "kids-player" && currentRoute != "kids-home" && currentRoute != "search") {
                        Box(modifier = Modifier.align(Alignment.TopCenter)) {
                            FloatingHeader(
                                currentProfile = currentProfile,
                                currentRoute = currentRoute,
                                onSearchClick = { navController.navigate("search") },
                                onProfileClick = { navController.navigate("settings") }
                            )
                        }
                    }

                    // ── Layer 3: Left Sidebar (floating, far left) ──
                    if (currentRoute != "player" && currentRoute != "music-player" && currentRoute != "shorts-player" && currentRoute != "kids-player" && currentRoute != "kids-home") {
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
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .focusRequester(leftSidebarFocusRequester)
                                .focusProperties { right = contentFocusRequester }
                        )
                    }

                    // ── Layer 4: Right Sidebar (floating, far right) ──
                    if (currentRoute != "player" && currentRoute != "music-player" && currentRoute != "shorts-player" && currentRoute != "kids-player" && currentRoute != "kids-home") {
                        Sidebar(
                            side = "right",
                            selectedRoute = currentRoute ?: "home",
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .focusRequester(rightSidebarFocusRequester)
                                .focusProperties { left = contentFocusRequester }
                        )
                    }

                    // ── Layer 5: MiniPlayer (bottom) ──
                    if (globalVideo != null && currentRoute != "player" && currentRoute != "music-player" && currentRoute != "shorts-player" && currentRoute != "kids-player" && currentRoute != "kids-home") {
                        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
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
                }
                }

                if (isIdle && watchHistory.isNotEmpty()) {
                    AmbientMode(videos = watchHistory)
                }
            }
        }
    }
}
