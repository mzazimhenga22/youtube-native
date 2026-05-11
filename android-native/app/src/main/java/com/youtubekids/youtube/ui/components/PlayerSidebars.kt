@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
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
            .width(480.dp)
            .background(Color(0xFF050505).copy(alpha = 0.85f))
    ) {
        // Side Aura Light Bleed
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.1f), Color.Transparent),
                        startX = Float.POSITIVE_INFINITY,
                        endX = 0f
                    )
                )
        )

        Column(modifier = Modifier.padding(40.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Comments", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }
                Surface(
                    onClick = onClose,
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                    colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f), focusedContainerColor = Color.White)
                ) {
                    Text("Close", modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), color = if (androidx.tv.material3.LocalContentColor.current == Color.Black) Color.Black else Color.White, fontWeight = FontWeight.Black)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(comments) { comment ->
                    Surface(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                        colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.05f), focusedContainerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.DarkGray)) {
                                if (comment.avatar != null) {
                                    AsyncImage(model = comment.avatar, contentDescription = null, contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                            Column(modifier = Modifier.padding(start = 14.dp)) {
                                Text("@${comment.user}", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Text(comment.text, color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(top = 4.dp))
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
            .width(480.dp)
            .background(Color(0xFF050505).copy(alpha = 0.85f))
    ) {
        // Side Aura Light Bleed
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.1f), Color.Transparent),
                        startX = Float.POSITIVE_INFINITY,
                        endX = 0f
                    )
                )
        )

        Column(modifier = Modifier.padding(40.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Lyrics", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }
                Surface(
                    onClick = onClose,
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                    colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f), focusedContainerColor = Color.White)
                ) {
                    Text("Close", modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), color = if (androidx.tv.material3.LocalContentColor.current == Color.Black) Color.Black else Color.White, fontWeight = FontWeight.Black)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                items(lyrics.size) { index ->
                    val isActive = index == activeLineIndex
                    Text(
                        text = lyrics[index],
                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.4f),
                        fontSize = if (isActive) 40.sp else 32.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = if (isActive) 48.sp else 40.sp,
                        modifier = Modifier.alpha(if (isActive) 1f else 0.4f)
                    )
                }
            }
        }
    }
}
