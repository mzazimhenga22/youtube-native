@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.ui.AppViewModel
import com.youtubekids.youtube.ui.components.HorizontalRail

data class LibraryShelfData(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val gradient: List<Color>,
    val count: Int
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LibraryScreen(
    onVideoClick: (Video) -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: AppViewModel = hiltViewModel()
) {
    val watchHistory by viewModel.watchHistory.collectAsState()
    val likedVideos by viewModel.likedVideos.collectAsState()
    val watchLater by viewModel.watchLater.collectAsState()

    var activeColor by remember { mutableStateOf(Color(0xFFFF4B4B)) }

    val shelves = listOf(
        LibraryShelfData("history", "History", Icons.Default.History, Color(0xFFFF4B4B), listOf(Color(0xFFFF4B4B), Color(0xFF9B1B1B)), watchHistory.size),
        LibraryShelfData("downloads", "Downloads", Icons.Default.Download, Color(0xFF4B7BFF), listOf(Color(0xFF4B7BFF), Color(0xFF1B3B9B)), 0),
        LibraryShelfData("watch-later", "Watch Later", Icons.Default.Schedule, Color(0xFFFFB84B), listOf(Color(0xFFFFB84B), Color(0xFF9B6B1B)), watchLater.size),
        LibraryShelfData("purchases", "Purchases", Icons.Default.CreditCard, Color(0xFF4BFF7B), listOf(Color(0xFF4BFF7B), Color(0xFF1B9B3B)), 0),
        LibraryShelfData("liked", "Liked Videos", Icons.Default.Favorite, Color(0xFFFF4BFF), listOf(Color(0xFFFF4BFF), Color(0xFF9B1B9B)), likedVideos.size),
        LibraryShelfData("playlists", "Playlists", Icons.Default.PlaylistPlay, Color(0xFF00D1FF), listOf(Color(0xFF00D1FF), Color(0xFF006B9B)), 12)
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        // 1. Dynamic Ambient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.15f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(activeColor, Color.Transparent, Color(0xFF050505))
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header Section
            item {
                Column(modifier = Modifier.padding(start = 64.dp, top = 80.dp, bottom = 48.dp, end = 64.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(
                            text = "Your Library",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 96.sp,
                                letterSpacing = (-4).sp,
                                shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 24f)
                            ),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(64.dp))

                    // Shelves Grid (Manual 3-column rows)
                    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                        shelves.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(32.dp)
                            ) {
                                rowItems.forEach { shelf ->
                                    LibraryShelf(
                                        shelf = shelf,
                                        onFocus = { activeColor = shelf.color },
                                        onClick = { onCategoryClick(shelf.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Fill empty slots if row isn't full
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Recently Played Rail
            if (watchHistory.isNotEmpty()) {
                item {
                    HorizontalRail(
                        title = "Recently Played",
                        videos = watchHistory,
                        onVideoClick = onVideoClick
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // Other Placeholder Rails to match RN design
            item {
                HorizontalRail(
                    title = "Movies & TV",
                    videos = emptyList(),
                    onVideoClick = onVideoClick
                )
                Spacer(modifier = Modifier.height(48.dp))
            }
            item {
                HorizontalRail(
                    title = "Saved to Library",
                    videos = emptyList(),
                    onVideoClick = onVideoClick
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LibraryShelf(
    shelf: LibraryShelfData,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(180.dp)
            .onFocusChanged { 
                isFocused = it.isFocused
                if (isFocused) onFocus()
            },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(40.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF111111),
            focusedContainerColor = Color.Transparent // Will use gradient background
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))),
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.2f)))
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isFocused) Brush.linearGradient(shelf.gradient)
                    else Brush.linearGradient(listOf(Color(0xFF111111), Color(0xFF111111)))
                )
                .padding(32.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon Pod
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (isFocused) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = shelf.icon,
                        contentDescription = null,
                        tint = if (isFocused) Color.White else shelf.color,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                Column {
                    Text(
                        text = shelf.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = if (isFocused) Color.White else Color(0xFFE4E4E7)
                    )
                    Text(
                        text = if (shelf.count > 0) "${shelf.count} items" else "No content yet",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isFocused) Color.White.copy(alpha = 0.7f) else Color(0xFF71717A)
                    )
                }
            }

            // Top-Right Badge
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "OPEN",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
