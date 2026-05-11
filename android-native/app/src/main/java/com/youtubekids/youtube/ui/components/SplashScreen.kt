@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val logoScale = remember { Animatable(0.94f) }
    val logoOpacity = remember { Animatable(0f) }
    val flareOffset = remember { Animatable(-1000f) }
    val flareOpacity = remember { Animatable(0f) }
    val shimmerOffset = remember { Animatable(-200f) }
    val progressWidth = remember { Animatable(0f) }
    val bgGlowOpacity = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val ambientGlow by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse),
        label = "ambientGlow"
    )

    LaunchedEffect(Unit) {
        // 1. Entrance
        launch { logoScale.animateTo(1f, tween(1800, easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f))) }
        launch { logoOpacity.animateTo(1f, tween(800)) }

        // 2. Optical Flare
        delay(400)
        launch { flareOpacity.animateTo(0.5f, tween(600)) }
        launch { flareOffset.animateTo(1000f, tween(1800, easing = FastOutSlowInEasing)) }
        launch { delay(1000); flareOpacity.animateTo(0f, tween(1000)) }

        // 3. Shimmer
        delay(800)
        launch { shimmerOffset.animateTo(300f, tween(1000, easing = LinearOutSlowInEasing)) }

        // 4. Progress
        delay(1300)
        progressWidth.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))

        delay(800)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303)),
        contentAlignment = Alignment.Center
    ) {
        // Background Ambient Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(ambientGlow)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(0.5f, 0.75f)
                    )
                )
        )

        // 3D Grid Floor
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(300.dp)
                .alpha(0.05f)
                .graphicsLayer {
                    rotationX = 70f
                    cameraDistance = 8 * density
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridStep = 40.dp.toPx()
                for (x in -10..20) {
                    drawLine(Color.White, Offset(x * gridStep, 0f), Offset(x * gridStep, size.height), 1f)
                }
                for (y in 0..15) {
                    drawLine(Color.White, Offset(-size.width, y * gridStep), Offset(size.width * 2, y * gridStep), 1f)
                }
            }
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black))))
        }

        // Optical Flare Layer
        Box(
            modifier = Modifier
                .offset(x = flareOffset.value.dp)
                .width(400.dp)
                .height(40.dp)
                .alpha(flareOpacity.value)
                .rotate(12f)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )

        // Main Logo Group
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(logoScale.value)
                .alpha(logoOpacity.value)
        ) {
            Box {
                // Main Logo
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(280.dp),
                    contentScale = ContentScale.Fit
                )

                // Shimmer Overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 40.dp, vertical = 80.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = shimmerOffset.value.dp)
                            .width(60.dp)
                            .fillMaxHeight()
                            .alpha(0.3f)
                            .rotate(-20f)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, Color.White, Color.Transparent)
                                )
                            )
                    )
                }
            }

            // Floor Reflection
            Box(
                modifier = Modifier
                    .offset(y = (-40).dp)
                    .scale(scaleX = 1f, scaleY = -0.5f)
                    .alpha(0.1f)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(280.dp),
                    contentScale = ContentScale.Fit
                )
                Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black))))
            }

            Spacer(modifier = Modifier.height(24.dp))

            TypewriterText("made by mzazimhenga")
        }

        // Progress Loader
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .width(180.dp)
                .height(2.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(1.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressWidth.value)
                    .background(Color.Red, RoundedCornerShape(1.dp))
            )
        }

        // Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                        center = Offset(0.5f, 0.5f)
                    )
                )
        )
    }
}

@Composable
fun TypewriterText(text: String) {
    var displayText by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        delay(2200)
        text.forEachIndexed { index, _ ->
            displayText = text.substring(0, index + 1)
            delay(35)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            displayText,
            color = Color(0xFF71717A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
        if (displayText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .width(1.5.dp)
                    .height(12.dp)
                    .background(Color.Red)
            )
        }
    }
}
