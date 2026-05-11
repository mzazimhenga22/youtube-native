@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 80.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ─── Left Pod: Branding ───
        Surface(
            onClick = {},
            modifier = Modifier.height(52.dp),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(26.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color(0xFF0D0D0D).copy(alpha = 0.7f),
                focusedContainerColor = Color(0xFF0D0D0D).copy(alpha = 0.85f)
            ),
            border = ClickableSurfaceDefaults.border(
                border = Border(
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.06f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Logo play icon
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color.Red, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(11.dp)) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width * 0.15f, 0f)
                            lineTo(size.width, size.height / 2)
                            lineTo(size.width * 0.15f, size.height)
                            close()
                        }
                        drawPath(path, Color.White)
                    }
                }

                Text(
                    text = "YouTube",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = (-0.8).sp
                )

                // Section badge
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
                            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = label.uppercase(),
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }

        // ─── Right Pod: Action Buttons ───
        Surface(
            onClick = {},
            modifier = Modifier.height(52.dp),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(26.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color(0xFF0D0D0D).copy(alpha = 0.7f),
                focusedContainerColor = Color(0xFF0D0D0D).copy(alpha = 0.85f)
            ),
            border = ClickableSurfaceDefaults.border(
                border = Border(
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.06f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HeaderButton(icon = Icons.Default.Search, onClick = onSearchClick)
                HeaderButton(icon = Icons.Default.Mic, onClick = {})
                HeaderButton(icon = Icons.Default.Notifications, onClick = {})

                Spacer(modifier = Modifier.width(4.dp))

                // Profile avatar
                Surface(
                    onClick = onProfileClick,
                    modifier = Modifier.size(36.dp),
                    shape = ClickableSurfaceDefaults.shape(CircleShape),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color(0xFF2A2A2A),
                        focusedContainerColor = Color(0xFF3A3A3A)
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(
                            androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                        )
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (currentProfile?.avatar != null) {
                            AsyncImage(
                                model = currentProfile.avatar,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
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
        modifier = Modifier.size(40.dp),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
