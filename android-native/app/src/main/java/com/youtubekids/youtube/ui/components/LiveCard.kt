@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveCard(
    video: Video,
    onFocus: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }

    Column {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .width(320.dp)
                .aspectRatio(16 / 9f)
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (it.isFocused) onFocus()
                },
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                )
            ),
            glow = ClickableSurfaceDefaults.glow(
                focusedGlow = Glow(
                    elevationColor = Color.White.copy(alpha = 0.12f),
                    elevation = 12.dp
                )
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )

                // Live Badge
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Red, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "LIVE",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Viewers
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Groups,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = video.views,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isFocused) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(start = 2.dp)
                        )
                    }
                }
            }
        }

        // Title and Channel under the card
        Column(modifier = Modifier.padding(top = 8.dp).width(320.dp)) {
            Text(
                text = video.title,
                color = if (isFocused) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = video.channel,
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
