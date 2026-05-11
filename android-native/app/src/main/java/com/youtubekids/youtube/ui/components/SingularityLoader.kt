@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SingularityLoader(
    transparent: Boolean = false,
    minimal: Boolean = false,
    ambientThumbnail: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (transparent) Color.Transparent else Color.Black)
    ) {
        if (transparent && ambientThumbnail != null) {
            AsyncImage(
                model = ambientThumbnail,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.3f)
                    .blur(60.dp),
                contentScale = ContentScale.Crop
            )
        } else if (!transparent) {
            DataMist()
        }

        // Scanline Effect Overlay (simulated with a very subtle alpha)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.02f))
        )

        Column(modifier = Modifier.fillMaxSize()) {
            if (!minimal) {
                LoaderHeader()
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(21f / 9f)
                        .clip(RoundedCornerShape(64.dp))
                        .background(Color(0xFF020202))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(64.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!minimal) {
                        ViewportDecals()
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SingularityEngine()
                        Spacer(modifier = Modifier.height(32.dp))
                        BootStatus()
                    }

                    // Ambient Vignette
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )
                }
            }

            if (!minimal) {
                LoaderFooter()
            }
        }
    }
}

@Composable
fun DataMist() {
    val particles = remember { List(14) { ParticleData() } }
    Box(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            Particle(p)
        }
    }
}

data class ParticleData(
    val id: Int = kotlin.random.Random.nextInt(0, 100000),
    val size: Float = kotlin.random.Random.nextFloat() * (4f - 1f) + 1f,
    val x: Float = kotlin.random.Random.nextFloat() * 1000f, // Relative %
    val y: Float = kotlin.random.Random.nextFloat() * 1000f, // Relative %
    val duration: Int = kotlin.random.Random.nextInt(10000, 25000)
)

@Composable
fun Particle(p: ParticleData) {
    val infiniteTransition = rememberInfiniteTransition()
    val translateY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -150f,
        animationSpec = infiniteRepeatable(
            animation = timing(p.duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val opacity by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = timing(p.duration / 2, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .offset(x = (p.x / 10).dp, y = (p.y / 10).dp + translateY.dp)
            .size(p.size.dp)
            .alpha(opacity)
            .background(Color(0xFFEF4444), CircleShape)
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LoaderHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFDC2626), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row {
                    Text("STREAM", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Black)
                    Text("FLOW", style = MaterialTheme.typography.headlineSmall, color = Color(0xFFEF4444), fontWeight = FontWeight.Black)
                }
                Text("Video Engine v4.0.2", fontSize = 9.sp, color = Color(0xFF64748B), letterSpacing = 2.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            StatItem("Streaming Hub", "YT-CORE-NODE-01", Icons.Default.Monitor)
            StatItem("Codec", "VP9 / AV1 4K", Icons.Default.Monitor)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color(0xFF475569))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label.uppercase(), fontSize = 9.sp, color = Color(0xFF475569), fontWeight = FontWeight.Bold)
        }
        Text(value, fontSize = 12.sp, color = Color.White, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun ViewportDecals() {
    Box(modifier = Modifier.fillMaxSize().padding(40.dp)) {
        Text(
            "// Streaming Metrics\n// Bitrate: 24.5 Mbps\n// Buffer: 4K Stable",
            modifier = Modifier.align(Alignment.TopStart),
            fontSize = 8.sp,
            color = Color(0xFF1E293B),
            lineHeight = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            "Playback: Ready\nPriority: Ultra-High\nNetwork: Optimized",
            modifier = Modifier.align(Alignment.TopEnd),
            fontSize = 8.sp,
            color = Color(0xFF1E293B),
            lineHeight = 12.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
fun SingularityEngine() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = timing(3600, easing = LinearEasing)
        )
    )
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = timing(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.size(240.dp), contentAlignment = Alignment.Center) {
        // Deep Space Ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(0.5.dp, Color.White.copy(alpha = 0.05f), CircleShape)
        )

        // Outer Dashed Ring
        Canvas(modifier = Modifier.fillMaxSize().padding(32.dp).rotate(-180f)) {
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = 0.2f),
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            )
        }

        // Animated Arc
        Canvas(modifier = Modifier.size(240.dp).rotate(rotation)) {
            drawArc(
                brush = Brush.sweepGradient(
                    0f to Color.Transparent,
                    0.5f to Color(0xFFEF4444).copy(alpha = 0.3f),
                    1f to Color.White
                ),
                startAngle = 0f,
                sweepAngle = 80f,
                useCenter = false,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Core
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "STREAMING",
                modifier = Modifier.scale(corePulse).alpha(corePulse),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 4.sp
            )
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFEF4444), Color(0xFFEF4444), Color.Transparent)
                        )
                    )
            )
            Text(
                "Status: Buffering",
                fontSize = 10.sp,
                color = Color(0xFFEF4444).copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun BootStatus() {
    var bootProgress by remember { mutableIntStateOf(0) }
    val infiniteTransition = rememberInfiniteTransition()
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = timing(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(480)
            bootProgress = if (bootProgress >= 100) 0 else bootProgress + 4
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(8) { i ->
                val phase = kotlin.math.abs(pulseProgress - i / 7f)
                val opacity = (1f - phase * 2f).coerceIn(0.1f, 1f)
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(12.dp)
                        .alpha(opacity)
                        .background(Color(0xFFDC2626), RoundedCornerShape(2.dp))
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.01f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    "Optimizing Video Stream ... $bootProgress%",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 5.sp
                )
            }
            Text(
                "Fetching High-Resolution Recommendations",
                fontSize = 8.sp,
                color = Color(0xFFEF4444).copy(alpha = 0.4f),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LoaderFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
            Text("Protocol: HLS", fontSize = 10.sp, color = Color(0xFF334155), fontFamily = FontFamily.Monospace, letterSpacing = 4.sp)
            Text("Stream: 4K HDR", fontSize = 10.sp, color = Color(0xFF334155), fontFamily = FontFamily.Monospace, letterSpacing = 4.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Monitor, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF450A0A))
            Text("Video Pipeline Ready", fontSize = 10.sp, color = Color(0xFF334155), fontFamily = FontFamily.Monospace, letterSpacing = 4.sp)
        }
    }
}

fun <T> timing(duration: Int, easing: Easing = LinearEasing) = tween<T>(duration, easing = easing)
