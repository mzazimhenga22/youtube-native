@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MagicKidsLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "MagicLoader")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bounce"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Dots
        Box(modifier = Modifier.size(280.dp).rotate(rotation)) {
            Dot(Modifier.align(Alignment.TopCenter), Color(0xFFFF4B4B))
            Dot(Modifier.align(Alignment.BottomCenter), Color(0xFF4B7BFF))
            Dot(Modifier.align(Alignment.CenterStart), Color(0xFF4BFF7B))
            Dot(Modifier.align(Alignment.CenterEnd), Color(0xFFFFB84B))
        }

        // Bouncing Play Icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = bounce.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(Color(0xFFFF4B4B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            Text(
                "FINDING MAGIC...",
                style = androidx.compose.ui.text.TextStyle(
                    color = Color(0xFFFF4B4B),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                    fontSize = 28.sp,
                    letterSpacing = 4.sp
                )
            )
        }
    }
}

@Composable
fun Dot(modifier: Modifier, color: Color) {
    Box(modifier = modifier.size(24.dp).clip(CircleShape).background(color))
}
