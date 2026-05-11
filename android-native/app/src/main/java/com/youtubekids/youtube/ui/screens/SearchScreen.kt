@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.repository.YouTubeRepository
import com.youtubekids.youtube.ui.components.VideoCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    onVideoClick: (Video) -> Unit,
    repository: YouTubeRepository
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Video>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val keyboardKeys = listOf(
        listOf("A", "B", "C", "D", "E", "F"),
        listOf("G", "H", "I", "J", "K", "L"),
        listOf("M", "N", "O", "P", "Q", "R"),
        listOf("S", "T", "U", "V", "W", "X"),
        listOf("Y", "Z", "1", "2", "3", "4"),
        listOf("5", "6", "7", "8", "9", "0"),
        listOf("&", "@", ".", "-", "_", "/"),
        listOf("SPACE", "BACK", "CLEAR")
    )

    val quickChips = listOf("Music", "Gaming", "News", "Movies", "Shorts", "Live")

    fun performSearch(q: String) {
        searchJob?.cancel()
        searchJob = scope.launch {
            if (q.trim().isNotEmpty()) {
                isLoading = true
                delay(800)
                results = repository.search(q)
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        results = repository.getHomeVideos()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        // 1. Ambient Background Gradients
        SearchAmbientGlow()

        Row(modifier = Modifier.fillMaxSize()) {
            // LEFT PANEL: KEYBOARD
            Column(
                modifier = Modifier
                    .width(520.dp)
                    .fillMaxHeight()
                    .padding(horizontal = 32.dp, vertical = 40.dp)
            ) {
                // Query Bar
                Surface(
                    onClick = { /* Implement selection focus */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(22.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.04f),
                        focusedContainerColor = Color.White.copy(alpha = 0.08f)
                    ),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = if (query.isEmpty()) Color(0xFF3F3F46) else Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (query.isEmpty()) "Search YouTube..." else query,
                            color = if (query.isEmpty()) Color(0xFF3F3F46) else Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f)
                        )
                        if (query.isNotEmpty()) {
                            Surface(
                                onClick = { query = ""; results = emptyList() },
                                modifier = Modifier.size(40.dp),
                                shape = ClickableSurfaceDefaults.shape(CircleShape),
                                colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                onClick = { performSearch(query) },
                                modifier = Modifier.size(44.dp),
                                shape = ClickableSurfaceDefaults.shape(CircleShape),
                                colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.08f), focusedContainerColor = Color.Red)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Custom Keyboard Grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    keyboardKeys.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { key ->
                                val isControl = listOf("SPACE", "BACK", "CLEAR").contains(key)
                                var isKeyFocused by remember { mutableStateOf(false) }
                                Surface(
                                    onClick = {
                                        when (key) {
                                            "BACK" -> if (query.isNotEmpty()) query = query.dropLast(1)
                                            "SPACE" -> query += " "
                                            "CLEAR" -> query = ""
                                            else -> query += key
                                        }
                                        performSearch(query)
                                    },
                                    modifier = (if (isControl) Modifier.weight(1f).height(56.dp) else Modifier.width(64.dp).height(56.dp)).onFocusChanged { isKeyFocused = it.isFocused },
                                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
                                    colors = ClickableSurfaceDefaults.colors(
                                        containerColor = Color.White.copy(alpha = 0.04f),
                                        focusedContainerColor = Color(0xFFCC0000)
                                    ),
                                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (key == "BACK") {
                                            Icon(Icons.Default.Backspace, contentDescription = null, tint = if (isKeyFocused) Color.White else Color(0xFF71717A), modifier = Modifier.size(22.dp))
                                        } else {
                                            Text(
                                                text = key,
                                                color = if (isKeyFocused) Color.White else Color(0xFF71717A),
                                                fontSize = if (isControl) 13.sp else 22.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = if (isControl) 1.5.sp else (-0.5).sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Quick Chips
                Column {
                    Text(
                        "QUICK SEARCH",
                        color = Color(0xFF3F3F46),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        quickChips.take(4).forEach { chip ->
                            var isChipFocused by remember { mutableStateOf(false) }
                            Surface(
                                onClick = { query = chip; performSearch(query) },
                                modifier = Modifier.wrapContentSize().onFocusChanged { isChipFocused = it.isFocused },
                                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = Color.White.copy(alpha = 0.05f),
                                    focusedContainerColor = Color.White
                                )
                            ) {
                                Text(
                                    text = chip,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                    color = if (isChipFocused) Color.Black else Color(0xFF71717A),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Divider
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().padding(vertical = 60.dp).background(Color.White.copy(alpha = 0.04f)))

            // RIGHT PANEL: RESULTS
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 32.dp, vertical = 40.dp)
            ) {
                // Results Header
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                    Text(
                        text = if (query.isEmpty()) "TRENDING" else "RESULTS FOR \"$query\"",
                        color = Color(0xFF52525B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        com.youtubekids.youtube.ui.components.SingularityLoader()
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(results) { video ->
                            VideoCard(
                                video = video,
                                onClick = { onVideoClick(video) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchAmbientGlow() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.TopStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.05f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(600.dp)
                .align(Alignment.BottomEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.02f), Color.Transparent)
                    )
                )
        )
    }
}


