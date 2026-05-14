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
        listOf("SPACE", "⌫", "CLR")
    )

    val quickChips = listOf("Music", "Gaming", "News", "Movies", "Shorts", "Live")

    fun performSearch(q: String) {
        searchJob?.cancel()
        searchJob = scope.launch {
            if (q.trim().isNotEmpty()) {
                isLoading = true
                delay(500)
                results = repository.search(q)
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        results = repository.getHomeVideos()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        SearchAmbientGlow()

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp) // Clear floating header
        ) {
            // ═══════════════════════════════════
            // LEFT: Keyboard Panel
            // ═══════════════════════════════════
            Column(
                modifier = Modifier
                    .width(400.dp)
                    .fillMaxHeight()
                    .padding(start = 80.dp, end = 16.dp, top = 20.dp, bottom = 24.dp)
            ) {
                // ── Query Bar ──
                Surface(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        focusedContainerColor = Color.White.copy(alpha = 0.08f)
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(
                            androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = if (query.isEmpty()) Color.White.copy(alpha = 0.2f) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (query.isEmpty()) "Search YouTube..." else query,
                            color = if (query.isEmpty()) Color.White.copy(alpha = 0.25f) else Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        if (query.isNotEmpty()) {
                            Surface(
                                onClick = { query = ""; results = emptyList() },
                                modifier = Modifier.size(32.dp),
                                shape = ClickableSurfaceDefaults.shape(CircleShape),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = Color.Transparent,
                                    focusedContainerColor = Color.White.copy(alpha = 0.1f)
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                onClick = { performSearch(query) },
                                modifier = Modifier.size(36.dp),
                                shape = ClickableSurfaceDefaults.shape(CircleShape),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = Color.Red.copy(alpha = 0.8f),
                                    focusedContainerColor = Color.Red
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Keyboard Grid ──
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    keyboardKeys.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { key ->
                                val isControl = key == "SPACE" || key == "⌫" || key == "CLR"
                                var isKeyFocused by remember { mutableStateOf(false) }

                                Surface(
                                    onClick = {
                                        when (key) {
                                            "⌫" -> if (query.isNotEmpty()) query = query.dropLast(1)
                                            "SPACE" -> query += " "
                                            "CLR" -> query = ""
                                            else -> query += key
                                        }
                                        performSearch(query)
                                    },
                                    modifier = (if (isControl)
                                        Modifier.weight(1f).height(44.dp)
                                    else
                                        Modifier.weight(1f).height(44.dp)
                                    ).onFocusChanged { isKeyFocused = it.isFocused },
                                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                                    colors = ClickableSurfaceDefaults.colors(
                                        containerColor = Color.White.copy(alpha = 0.04f),
                                        focusedContainerColor = Color.Red
                                    ),
                                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (key == "⌫") {
                                            Icon(
                                                Icons.Default.Backspace,
                                                contentDescription = null,
                                                tint = if (isKeyFocused) Color.White else Color.White.copy(alpha = 0.4f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Text(
                                                text = key,
                                                color = if (isKeyFocused) Color.White else Color.White.copy(alpha = 0.4f),
                                                fontSize = if (isControl) 11.sp else 16.sp,
                                                fontWeight = if (isKeyFocused) FontWeight.Bold else FontWeight.Medium,
                                                letterSpacing = if (isControl) 1.sp else 0.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Quick Chips ──
                Text(
                    "QUICK SEARCH",
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickChips.take(4).forEach { chip ->
                        var isChipFocused by remember { mutableStateOf(false) }
                        Surface(
                            onClick = { query = chip; performSearch(query) },
                            modifier = Modifier.weight(1f).height(36.dp).onFocusChanged { isChipFocused = it.isFocused },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color.White.copy(alpha = 0.04f),
                                focusedContainerColor = Color.White
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = chip,
                                    color = if (isChipFocused) Color.Black else Color.White.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickChips.drop(4).forEach { chip ->
                        var isChipFocused by remember { mutableStateOf(false) }
                        Surface(
                            onClick = { query = chip; performSearch(query) },
                            modifier = Modifier.weight(1f).height(36.dp).onFocusChanged { isChipFocused = it.isFocused },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color.White.copy(alpha = 0.04f),
                                focusedContainerColor = Color.White
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = chip,
                                    color = if (isChipFocused) Color.Black else Color.White.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Divider ──
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .padding(vertical = 40.dp)
                    .background(Color.White.copy(alpha = 0.04f))
            )

            // ═══════════════════════════════════
            // RIGHT: Results Panel
            // ═══════════════════════════════════
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 24.dp, end = 80.dp, top = 20.dp, bottom = 24.dp)
            ) {
                // Results header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.04f)))
                    Text(
                        text = if (query.isEmpty()) "TRENDING" else "\"${query.uppercase()}\"",
                        color = Color.White.copy(alpha = 0.25f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.04f)))
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        com.youtubekids.youtube.ui.components.SingularityLoader(minimal = true)
                    }
                } else {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(minSize = 260.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(results.filter { it.id.isNotBlank() }, key = { it.id }) { video ->
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
                .size(400.dp)
                .align(Alignment.TopStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.03f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.BottomEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.01f), Color.Transparent)
                    )
                )
        )
    }
}
