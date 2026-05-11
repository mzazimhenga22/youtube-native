@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.repository.YouTubeRepository
import com.youtubekids.youtube.ui.components.MultiviewGrid

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MultiviewScreen(
    onVideoClick: (Video) -> Unit,
    repository: YouTubeRepository
) {
    var streams by remember { mutableStateOf<List<Video>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            streams = repository.search("live sports streams hd")
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        // Ambient Sports Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF00FF99).copy(alpha = 0.05f), Color.Transparent),
                        radius = 1000f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header Pod
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = {},
                        modifier = Modifier.size(56.dp),
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.GridView, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = "Multiview",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-2).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Sports HQ • Ultra Low Latency",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF00FF99),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Surface(
                    onClick = { /* Layout Settings */ },
                    modifier = Modifier.height(56.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(28.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Grid Layout", fontWeight = FontWeight.Black)
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Syncing live feeds...", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            } else {
                MultiviewGrid(
                    streams = streams,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
