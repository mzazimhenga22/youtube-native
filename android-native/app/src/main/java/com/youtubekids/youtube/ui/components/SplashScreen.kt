@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.youtubekids.youtube.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "singularity")
    
    // Core pulse animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    val coreAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { coreAlpha.animateTo(1f, tween(1500)) }
        launch { delay(1000); textAlpha.animateTo(1f, tween(1000)) }
        delay(4500)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020205)),
        contentAlignment = Alignment.Center
    ) {
        // 1. Cinematic Background Rays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.15f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(0.5f, 0.5f),
                        radius = 1200f
                    )
                )
        )

        // 2. Animated Singularity Core (Procedural UI)
        Box(
            modifier = Modifier
                .size(400.dp)
                .scale(pulseScale)
                .alpha(coreAlpha.value),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Outer Glow
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    radius = size.width / 2
                )
            }

            // Rotating Energy Rings
            repeat(3) { i ->
                val ringRotation = (rotation * (i + 1) * 0.5f) % 360f
                Canvas(
                    modifier = Modifier
                        .size(180.dp + (i * 40).dp)
                        .rotate(ringRotation)
                ) {
                    drawArc(
                        color = Color.Red.copy(alpha = 0.3f - (i * 0.05f)),
                        startAngle = 0f,
                        sweepAngle = 120f,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color.Red.copy(alpha = 0.3f - (i * 0.05f)),
                        startAngle = 180f,
                        sweepAngle = 120f,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Central Core Play Symbol (Procedural)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Red, CircleShape)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width * 0.25f, 0f)
                        lineTo(size.width, size.height / 2)
                        lineTo(size.width * 0.25f, size.height)
                        close()
                    }
                    drawPath(path, Color.White)
                }
            }
            
            // Core Ripple
            val rippleScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 2.5f,
                animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
                label = "ripple"
            )
            val rippleAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
                label = "rippleAlpha"
            )
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(rippleScale)
                    .alpha(rippleAlpha)
                    .border(1.5.dp, Color.Red, CircleShape)
            )
        }

        // 3. Footer Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .alpha(textAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TypewriterText("made by mzazimhenga")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Minimalist Progress Line
            val progressWidth = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                delay(1500)
                progressWidth.animateTo(1f, tween(2000, easing = FastOutSlowInEasing))
            }
            
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressWidth.value)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.Red, Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun TypewriterText(text: String) {
    var displayText by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        delay(1200)
        text.forEachIndexed { index, _ ->
            displayText = text.substring(0, index + 1)
            delay(50)
        }
    }
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = displayText.uppercase(),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 6.sp,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(
                    color = Color.Red.copy(alpha = 0.3f),
                    offset = Offset(0f, 0f),
                    blurRadius = 10f
                )
            )
        )
        
        // Animated Cursor
        val cursorAlpha by rememberInfiniteTransition(label = "").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
            label = ""
        )
        
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(4.dp)
                .alpha(cursorAlpha)
                .background(Color.Red, CircleShape)
        )
    }
}
