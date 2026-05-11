@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.ui.Profile

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FloatingHeader(
    currentProfile: Profile?,
    currentRoute: String?,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        // Left Pod: Branding
        Surface(
            onClick = {},
            modifier = Modifier
                .align(Alignment.TopStart)
                .height(60.dp),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(30.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color(0xFF121212).copy(alpha = 0.5f)
            ),
            border = ClickableSurfaceDefaults.border(
                border = Border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)))
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Logo Triangle Box
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Red, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, size.height / 2)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path, Color.White)
                    }
                }
                
                Text(
                    text = "YouTube",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = (-1).sp
                )

                // Theme Badge
                val theme = when {
                    currentRoute?.contains("music") == true -> "Music" to Color(0xFFFF0055)
                    currentRoute?.contains("movies") == true -> "Movies" to Color(0xFFFF4444)
                    currentRoute?.contains("live") == true -> "Live" to Color(0xFFFF0000)
                    currentRoute?.contains("shorts") == true -> "Shorts" to Color(0xFFFF4400)
                    else -> null
                }

                theme?.let { (label, color) ->
                    Box(
                        modifier = Modifier
                            .background(color.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label.uppercase(),
                            color = color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }
        }

        // Right Pod: Actions
        Surface(
            onClick = {},
            modifier = Modifier
                .align(Alignment.TopEnd)
                .height(60.dp),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(30.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color(0xFF121212).copy(alpha = 0.5f)
            ),
            border = ClickableSurfaceDefaults.border(
                border = Border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)))
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeaderButton(icon = Icons.Default.Search, onClick = onSearchClick)
                HeaderButton(icon = Icons.Default.Mic, onClick = {})
                
                // Avatar Button
                Surface(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape),
                    shape = ClickableSurfaceDefaults.shape(CircleShape),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color.White)))
                ) {
                    if (currentProfile?.avatar != null) {
                        AsyncImage(
                            model = currentProfile.avatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1F1F1F)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HeaderButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}
