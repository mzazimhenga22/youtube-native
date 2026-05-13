@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage

data class Comment(val id: String, val user: String, val text: String, val avatar: String? = null, val likes: String = "0")

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CommentsSidebar(
    comments: List<Comment>,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(380.dp)
            .background(Color(0xFF0A0A0A).copy(alpha = 0.92f))
    ) {
        // Subtle edge glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.04f), Color.Transparent),
                        startX = Float.POSITIVE_INFINITY,
                        endX = 0f
                    )
                )
        )

        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comments", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${comments.size}",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Surface(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp),
                    shape = ClickableSurfaceDefaults.shape(CircleShape),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        focusedContainerColor = Color.White
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = LocalContentColor.current, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Comments list
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(comments) { comment ->
                    Surface(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.White.copy(alpha = 0.03f),
                            focusedContainerColor = Color.White.copy(alpha = 0.08f)
                        )
                    ) {
                        Row(modifier = Modifier.padding(10.dp)) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF1A1A1A)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (comment.avatar != null) {
                                    AsyncImage(
                                        model = comment.avatar,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                                }
                            }
                            Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                                Text(
                                    "@${comment.user}",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    comment.text,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (comment.likes != "0") {
                                    Text(
                                        "♡ ${comment.likes}",
                                        color = Color.White.copy(alpha = 0.25f),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LyricsSidebar(
    lyrics: List<String>,
    activeLineIndex: Int,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(380.dp)
            .background(Color(0xFF0A0A0A).copy(alpha = 0.92f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF7700FF).copy(alpha = 0.04f), Color.Transparent),
                        startX = Float.POSITIVE_INFINITY,
                        endX = 0f
                    )
                )
        )

        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lyrics", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp),
                    shape = ClickableSurfaceDefaults.shape(CircleShape),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        focusedContainerColor = Color.White
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = LocalContentColor.current, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Lyrics
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(lyrics.size) { index ->
                    val isActive = index == activeLineIndex
                    Text(
                        text = lyrics[index],
                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.25f),
                        fontSize = if (isActive) 20.sp else 16.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        lineHeight = if (isActive) 26.sp else 22.sp,
                        modifier = Modifier.alpha(if (isActive) 1f else 0.5f)
                    )
                }
            }
        }
    }
}
