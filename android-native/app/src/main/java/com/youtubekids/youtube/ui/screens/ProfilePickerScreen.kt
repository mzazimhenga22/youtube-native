@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.youtubekids.youtube.R
import com.youtubekids.youtube.ui.Profile
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ProfilePickerScreen(
    onProfileSelected: (Profile) -> Unit
) {
    val profiles = listOf(
        Profile("p1", "Gamer Pro", "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=400", "regular"),
        Profile("p2", "Kids Mode", "https://images.unsplash.com/photo-1607746882042-944635dfe10e?w=400", "kids"),
        Profile("p3", "Guest", "https://images.unsplash.com/photo-1544723795-3cj5a26c4293?w=400", "regular")
    )

    var focusedId by remember { mutableStateOf<String?>(null) }
    var showSignInDrawer by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F13)), // Deep cinematic dark background
        contentAlignment = Alignment.Center
    ) {
        // 1. Cinematic Ambient Background
        CinematicBackground(focusedId)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(top = 100.dp)
        ) {
            // Header Section
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(1200)) + slideInVertically(tween(1200)) { -60 },
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Who's watching?",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }
            }

            // Profiles Grid
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                profiles.forEach { profile ->
                    ProfileCard(
                        profile = profile,
                        isDimmed = focusedId != null && focusedId != profile.id,
                        onFocus = { focusedId = profile.id },
                        onClick = { onProfileSelected(profile) }
                    )
                }

                // Add Profile Button
                AddProfileButton(
                    isDimmed = focusedId != null && focusedId != "add",
                    onFocus = { focusedId = "add" },
                    onClick = { showSignInDrawer = true }
                )
            }
        }

        // Auth/Sign-In Drawer (Overlay)
        AnimatedVisibility(
            visible = showSignInDrawer,
            enter = slideInHorizontally(tween(500, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(500)),
            exit = slideOutHorizontally(tween(400, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(400)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            AuthDrawer(
                onClose = { showSignInDrawer = false },
                onAuthSuccess = { profile -> 
                    showSignInDrawer = false
                    onProfileSelected(profile)
                }
            )
        }
    }
}

@Composable
private fun CinematicBackground(focusedId: String?) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_bg")
    
    // Smooth slow rotation for a premium feel
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation1"
    )
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(50000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation2"
    )

    // Dynamic color shifting based on focus
    val isKidsFocused = focusedId == "p2"
    val color1 by animateColorAsState(
        targetValue = if (isKidsFocused) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFFFF0000).copy(alpha = 0.1f),
        animationSpec = tween(1500)
    )
    val color2 by animateColorAsState(
        targetValue = if (isKidsFocused) Color(0xFFFF00AA).copy(alpha = 0.15f) else Color(0xFF4A00E0).copy(alpha = 0.1f),
        animationSpec = tween(1500)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (-300).dp, y = (-200).dp)
                .size(1400.dp)
                .rotate(rotation1)
                .blur(160.dp)
                .background(Brush.radialGradient(listOf(color1, Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .offset(x = 300.dp, y = 200.dp)
                .size(1600.dp)
                .rotate(rotation2)
                .blur(180.dp)
                .background(Brush.radialGradient(listOf(color2, Color.Transparent)))
        )
        // Vignette to darken edges
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(listOf(Color.Transparent, Color(0xFF050505).copy(alpha = 0.8f)), radius = 1200f))
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ProfileCard(
    profile: Profile,
    isDimmed: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    // Smooth, snappy scaling specifically tuned for TV
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.2f else 1.0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    val alpha by animateFloatAsState(
        targetValue = if (isFocused) 1.0f else if (isDimmed) 0.3f else 0.7f,
        animationSpec = tween(300)
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1.0f else 0.0f,
        animationSpec = tween(300)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .onFocusChanged { 
                isFocused = it.isFocused
                if (isFocused) onFocus()
            }
            .alpha(alpha)
    ) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer Glow when focused
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(scale)
                    .alpha(glowAlpha)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .blur(30.dp)
            )

            Surface(
                onClick = onClick,
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale),
                shape = ClickableSurfaceDefaults.shape(CircleShape),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                border = ClickableSurfaceDefaults.border(
                    border = Border(androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.1f))),
                    focusedBorder = Border(androidx.compose.foundation.BorderStroke(6.dp, Color.White))
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = profile.avatar,
                        contentDescription = profile.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay at the bottom of the avatar
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 100f
                            ))
                    )

                    // Kids Mode icon inside the avatar bottom center
                    if (profile.mode == "kids") {
                        Icon(
                            Icons.Default.ChildCare,
                            contentDescription = "Kids Mode",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .size(32.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = profile.name,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.scale(if (isFocused) 1.05f else 1.0f)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AddProfileButton(
    isDimmed: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.2f else 1.0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    val alpha by animateFloatAsState(
        targetValue = if (isFocused) 1.0f else if (isDimmed) 0.3f else 0.7f,
        animationSpec = tween(300)
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1.0f else 0.0f,
        animationSpec = tween(300)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .onFocusChanged { 
                isFocused = it.isFocused
                if (isFocused) onFocus()
            }
            .alpha(alpha)
    ) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(scale)
                    .alpha(glowAlpha)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .blur(30.dp)
            )

            Surface(
                onClick = onClick,
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale),
                shape = ClickableSurfaceDefaults.shape(CircleShape),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color.White.copy(alpha = 0.15f)
                ),
                border = ClickableSurfaceDefaults.border(
                    border = Border(androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.1f))),
                    focusedBorder = Border(androidx.compose.foundation.BorderStroke(6.dp, Color.White))
                )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Add Profile", 
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Add Profile",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.scale(if (isFocused) 1.05f else 1.0f)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AuthDrawer(
    onClose: () -> Unit,
    onAuthSuccess: (Profile) -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)) // Dim the background heavily
    ) {
        // Click outside to close (left invisible overlay)
        Box(modifier = Modifier.fillMaxSize().padding(end = 650.dp)) {
            Surface(
                onClick = onClose, 
                modifier = Modifier.fillMaxSize(), 
                colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.Transparent)
            ) {}
        }

        // Drawer Panel
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(650.dp)
                .align(Alignment.CenterEnd)
                .background(Color(0xFF141416), RoundedCornerShape(topStart = 40.dp, bottomStart = 40.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(topStart = 40.dp, bottomStart = 40.dp))
                .padding(64.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Sign In",
                        color = Color.White,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    )
                    Surface(
                        onClick = onClose,
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.White.copy(alpha = 0.1f), 
                            focusedContainerColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (androidx.tv.material3.LocalContentColor.current == Color.Black) Color.Black else Color.White,
                            modifier = Modifier.padding(16.dp).size(28.dp)
                        )
                    }
                }

                Text(
                    "Sign in with your YouTube account to access your personalized library, history, and tailored recommendations.",
                    color = Color(0xFFA0A0A5),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 34.sp,
                    modifier = Modifier.padding(bottom = 56.dp)
                )

                AuthField(
                    label = "Email Address", 
                    value = email, 
                    icon = Icons.Default.Email,
                    onValueChange = { email = it }
                )
                Spacer(modifier = Modifier.height(28.dp))
                AuthField(
                    label = "Password", 
                    value = if (password.isEmpty()) "" else "••••••••", 
                    icon = Icons.Default.Lock,
                    onValueChange = { password = it }
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color(0xFFFF4D4D),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            error = null
                            scope.launch {
                                try {
                                    val result = auth.signInWithEmailAndPassword(email, password).await()
                                    val user = result.user
                                    if (user != null) {
                                        val newProfile = Profile(
                                            id = user.uid,
                                            name = user.displayName ?: user.email?.substringBefore("@") ?: "User",
                                            avatar = user.photoUrl?.toString() ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
                                            mode = "regular"
                                        )
                                        onAuthSuccess(newProfile)
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: "Authentication failed"
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            error = "Please enter email and password"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    enabled = !isLoading,
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(40.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White, 
                        focusedContainerColor = Color(0xFFE5E5E5)
                    ),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoading) {
                            com.youtubekids.youtube.ui.components.SingularityLoader()
                        } else {
                            Text("CONTINUE", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AuthField(
    label: String, 
    value: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onValueChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth().onFocusChanged { isFocused = it.isFocused }) {
        Text(
            label.uppercase(),
            color = if (isFocused) Color.White else Color(0xFFA0A0A5),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
        )
        Surface(
            onClick = { /* Simulated soft keyboard trigger */ },
            modifier = Modifier.fillMaxWidth(),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.05f),
                focusedContainerColor = Color.White.copy(alpha = 0.15f)
            ),
            border = ClickableSurfaceDefaults.border(
                border = Border(androidx.compose.foundation.BorderStroke(2.dp, Color.Transparent)),
                focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color.White))
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = if (isFocused) Color.White else Color(0xFFA0A0A5), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                if (value.isEmpty()) {
                    Text("Enter $label", color = Color(0xFF606065), fontSize = 24.sp, fontWeight = FontWeight.Normal)
                } else {
                    Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}