@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.youtubekids.youtube.R
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.repository.YouTubeRepository
import com.youtubekids.youtube.ui.AppViewModel
import com.youtubekids.youtube.ui.components.KidsVideoCard
import com.youtubekids.youtube.ui.components.MagicKidsLoader
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class KidsNavItem(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val color: Color = Color.Transparent
)

@Composable
fun KidsHomeScreen(
    onVideoClick: (Video) -> Unit,
    repository: YouTubeRepository,
    viewModel: AppViewModel = hiltViewModel()
) {
    var activeCategory by remember { mutableStateOf("home") }
    var sections by remember { mutableStateOf<List<Pair<String, List<Video>>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val watchHistory by viewModel.watchHistory.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()

    val sidebarItems = listOf(
        KidsNavItem("home", Icons.Default.Home, "Home"),
        KidsNavItem("shows", Icons.Default.Tv, "Shows"),
        KidsNavItem("explore", Icons.Default.Explore, "Explore"),
        KidsNavItem("mystuff", Icons.Default.Star, "My Stuff")
    )

    val bottomItems = listOf(
        KidsNavItem("learning", Icons.Default.School, "Learn", Color(0xFF4CC9F0)),
        KidsNavItem("play", Icons.Default.Gamepad, "Play", Color(0xFFF72585)),
        KidsNavItem("watch", Icons.Default.Tv, "Watch", Color(0xFF7209B7)),
        KidsNavItem("music", Icons.Default.MusicNote, "Listen", Color(0xFFF3722C))
    )

    LaunchedEffect(activeCategory, watchHistory) {
        loading = true
        sections = when (activeCategory) {
            "home" -> {
                coroutineScope {
                    val shows = async { repository.getKidsCategory("shows") }
                    val learning = async { repository.getKidsCategory("learning") }
                    val music = async { repository.getKidsCategory("music") }
                    val explore = async { repository.getKidsCategory("explore") }

                    listOf(
                        "Popular Shows" to shows.await(),
                        "Time to Learn!" to learning.await(),
                        "Sing Along" to music.await(),
                        "Explore the World" to explore.await()
                    )
                }
            }
            "mystuff" -> listOf("My Stuff" to watchHistory)
            else -> {
                val title = activeCategory.replaceFirstChar { it.uppercase() }
                listOf(title to repository.getKidsCategory(activeCategory))
            }
        }
        loading = false
    }

    if (loading && sections.isEmpty()) {
        MagicKidsLoader()
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Background image
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.kids_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0xFF0F172A).copy(alpha = 0.85f))
                )
            )
        )

        // Ambient bubbles
        AmbientBubbles()

        Row(modifier = Modifier.fillMaxSize()) {
            // ── Sidebar ──
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(110.dp)
                    .padding(start = 24.dp, top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glassmorphic background for sidebar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(72.dp)
                        .background(
                            Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(36.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(36.dp))
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        sidebarItems.forEach { item ->
                            KidsSidebarItem(
                                item = item,
                                isSelected = activeCategory == item.id,
                                onClick = { activeCategory = item.id }
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Logout with extra 'aura'
                        Surface(
                            onClick = { 
                                viewModel.logout()
                            },
                            modifier = Modifier.size(52.dp),
                            shape = ClickableSurfaceDefaults.shape(CircleShape),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color.Red.copy(alpha = 0.2f),
                                focusedContainerColor = Color.Red
                            ),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.2f),
                            glow = ClickableSurfaceDefaults.glow(
                                focusedGlow = Glow(
                                    elevationColor = Color.Red.copy(alpha = 0.4f),
                                    elevation = 15.dp
                                )
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

            // ── Content ──
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(top = 56.dp, bottom = 100.dp, end = 40.dp)
            ) {
                // Header
                item {
                    Column(modifier = Modifier.padding(bottom = 32.dp, start = 8.dp)) {
                        Text(
                            text = if (activeCategory == "home") "Kids Mode" else activeCategory.replaceFirstChar { it.uppercase() },
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = if (activeCategory == "home") "A world of fun, learning & adventure!" else "Discover amazing $activeCategory content!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Video rails
                items(sections) { (title, videos) ->
                    Column(modifier = Modifier.padding(bottom = 24.dp)) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(videos, key = { it.id }) { video ->
                                KidsVideoCard(video = video, onClick = { onVideoClick(video) })
                            }
                        }
                    }
                }

                // Bottom pods
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomItems.forEach { item ->
                            KidsBottomPod(
                                item = item,
                                isSelected = activeCategory == item.id,
                                onClick = { activeCategory = item.id }
                            )
                        }
                    }
                }
            }
        }

        // ── Profile Greeting (Top-Right) ──
        Surface(
            onClick = {},
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Black.copy(alpha = 0.3f),
                focusedContainerColor = Color.White.copy(alpha = 0.2f)
            ),
            border = ClickableSurfaceDefaults.border(
                border = Border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))),
                focusedBorder = Border(BorderStroke(1.5.dp, Color.White))
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFB923C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Face, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hi, ${currentProfile?.name ?: "Explorer"}!",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun KidsSidebarItem(item: KidsNavItem, isSelected: Boolean, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(52.dp).onFocusChanged { isFocused = it.isFocused },
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = if (isSelected) Color(0xFF9061FF) else Color.Black.copy(alpha = 0.25f),
                focusedContainerColor = Color.White
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    item.icon,
                    contentDescription = item.label,
                    tint = if (isFocused) Color(0xFF9061FF) else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            item.label,
            color = if (isSelected || isFocused) Color.White else Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun KidsBottomPod(item: KidsNavItem, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
            colors = ClickableSurfaceDefaults.colors(containerColor = item.color),
            border = ClickableSurfaceDefaults.border(
                border = if (isSelected) Border(BorderStroke(2.dp, Color.White)) else Border.None,
                focusedBorder = Border(BorderStroke(2.dp, Color.White))
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(item.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.label.uppercase(),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun AmbientBubbles() {
    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")
    val bubbleY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Restart),
        label = "bubbleY"
    )

    Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
        val count = 12
        for (i in 0 until count) {
            val x = (i * 373) % size.width
            val y = (size.height + 200 - ((bubbleY + (i * 450)) % (size.height + 400)))
            val radius = 20f + (i * 10) % 50f
            drawCircle(
                color = Color.White,
                radius = radius,
                center = Offset(x, y),
                style = Stroke(width = 1.5f)
            )
        }
    }
}
