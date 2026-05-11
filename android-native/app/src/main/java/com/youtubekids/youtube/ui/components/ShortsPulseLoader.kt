@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ShortsPulseLoader() {
    var phase by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            phase = (phase + 1) % 4
            delay(2000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B)),
        contentAlignment = Alignment.Center
    ) {
        // Scanning background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFEF4444).copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Pulse Rings
                repeat(3) { i ->
                    PulseRing(delay = i * 400)
                }

                // Central Icon
                Surface(
                    onClick = {},
                    modifier = Modifier.size(80.dp),
                    shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color(0xFFEF4444),
                        focusedContainerColor = Color(0xFFF87171)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Monitor,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "SYNCHRONIZING FEED",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loading stats
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LoadingStat("CORE", "${(80..99).random()}%")
                LoadingStat("BUFF", "${(10..256).random()}MB")
                LoadingStat("SYNC", "ACTIVE")
            }
        }

        // Corner brackets
        Bracket(Modifier.align(Alignment.TopStart).padding(40.dp), true, true)
        Bracket(Modifier.align(Alignment.TopEnd).padding(40.dp), true, false)
        Bracket(Modifier.align(Alignment.BottomStart).padding(40.dp), false, true)
        Bracket(Modifier.align(Alignment.BottomEnd).padding(40.dp), false, false)

        // Floating data bits
        repeat(10) {
            FloatingDataBit()
        }
    }
}

@Composable
fun PulseRing(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = delay, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = delay, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clip(CircleShape)
            .background(Color(0xFFEF4444).copy(alpha = 0.3f))
    )
}

@Composable
fun LoadingStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color(0xFFEF4444), fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun FloatingDataBit() {
    val xPos = remember { (0..1000).random().toFloat() }
    val yPos = remember { (0..1000).random().toFloat() }
    val infiniteTransition = rememberInfiniteTransition(label = "dataBit")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween((1000..3000).random(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val text = remember { listOf("0", "1", "SYNC", "LOAD", "DATA").random() }

    Box(
        modifier = Modifier
            .offset(x = xPos.dp, y = yPos.dp)
            .alpha(alpha)
    ) {
        Text(text, color = Color(0xFF64748B), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
fun Bracket(modifier: Modifier, isTop: Boolean, isLeft: Boolean) {
    Canvas(modifier = modifier.size(20.dp)) {
        val strokeWidth = 2.dp.toPx()
        val color = Color(0xFFEF4444).copy(alpha = 0.3f)
        
        if (isTop) {
            drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth)
        } else {
            drawLine(color, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth)
        }
        
        if (isLeft) {
            drawLine(color, Offset(0f, 0f), Offset(0f, size.height), strokeWidth)
        } else {
            drawLine(color, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth)
        }
    }
}
