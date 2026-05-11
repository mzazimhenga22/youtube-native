@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage

/**
 * Clean, professional loading indicator.
 *
 * @param transparent  true when overlaying video (uses ambient thumbnail)
 * @param minimal      true for inline use (no full-screen background)
 * @param ambientThumbnail  blurred background image URL when transparent
 */
@Composable
fun SingularityLoader(
    transparent: Boolean = false,
    minimal: Boolean = false,
    ambientThumbnail: String? = null
) {
    if (minimal) {
        // Inline loader — just the spinner + text
        MinimalLoader()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (transparent) Color.Transparent else Color(0xFF050505)),
        contentAlignment = Alignment.Center
    ) {
        // Blurred ambient background when overlaying video
        if (transparent && ambientThumbnail != null) {
            AsyncImage(
                model = ambientThumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.25f).blur(40.dp),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
        }

        // Center content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SpinnerRing()
            Spacer(modifier = Modifier.height(20.dp))
            PulsingDots()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Loading",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }
}

/**
 * Compact inline loader for use inside panels / grids.
 */
@Composable
private fun MinimalLoader() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SmallSpinner()
            Spacer(modifier = Modifier.height(12.dp))
            PulsingDots()
        }
    }
}

// ─────────────────────────────────────────────
// Spinner Ring — clean arc animation
// ─────────────────────────────────────────────
@Composable
private fun SpinnerRing() {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier.size(80.dp).scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        // Outer track
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Animated arc
        Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
            drawArc(
                brush = Brush.sweepGradient(
                    0f to Color.Transparent,
                    0.4f to Color.Red.copy(alpha = 0.6f),
                    0.8f to Color.White
                ),
                startAngle = 0f,
                sweepAngle = 100f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Center dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color.Red, CircleShape)
        )
    }
}

// ─────────────────────────────────────────────
// Small Spinner — for inline/minimal use
// ─────────────────────────────────────────────
@Composable
private fun SmallSpinner() {
    val infiniteTransition = rememberInfiniteTransition(label = "smallSpinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "smallRotation"
    )

    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
        Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
            drawArc(
                color = Color.Red,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Pulsing Dots — subtle 3-dot wave
// ─────────────────────────────────────────────
@Composable
private fun PulsingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { i ->
            val delay = i * 200
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$i"
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .alpha(alpha)
                    .background(Color.White, CircleShape)
            )
        }
    }
}
