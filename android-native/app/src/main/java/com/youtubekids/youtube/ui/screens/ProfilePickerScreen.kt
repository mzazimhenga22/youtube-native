@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
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
        Profile("p1", "Gamer Pro", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400", "regular"),
        Profile("p2", "Kids Mode", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400", "kids"),
        Profile("p3", "Guest", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400", "regular")
    )

    var focusedId by remember { mutableStateOf<String?>(null) }
    var showSignInDrawer by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505)),
        contentAlignment = Alignment.Center
    ) {
        // 1. Ambient Dynamic Background
        AmbientBackground()

        // 2. Kids Mode Mascot Pop-out
        AnimatedVisibility(
            visible = focusedId == "p2",
            enter = fadeIn(tween(1000)) + scaleIn(tween(800), initialScale = 0.8f),
            exit = fadeOut(tween(500)) + scaleOut(tween(500), targetScale = 0.8f),
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.kids_bg),
                contentDescription = null,
                modifier = Modifier
                    .size(600.dp)
                    .alpha(0.2f)
                    .rotate(-15f),
                contentScale = ContentScale.Fit
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Header Section
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(1200)) + slideInVertically(tween(1200)) { -40 },
                modifier = Modifier.padding(bottom = 120.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Who's Watching?",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 96.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 104.sp,
                            letterSpacing = (-3).sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.width(128.dp).height(4.dp).background(Color.White.copy(alpha = 0.2f), CircleShape))
                }
            }

            // Profiles Grid
            Row(
                modifier = Modifier.padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                profiles.forEachIndexed { index, profile ->
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

        // 3. Premium Brand Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(100.dp))
                .padding(horizontal = 40.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Red, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "YOUTUBE",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 12.sp
                )
            }
        }

        // 4. Auth/Sign-In Drawer
        AnimatedVisibility(
            visible = showSignInDrawer,
            enter = slideInHorizontally(tween(500)) { it } + fadeIn(),
            exit = slideOutHorizontally(tween(300)) { it } + fadeOut()
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
private fun AmbientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(1200.dp)
                .rotate(rotation)
                .scale(1.5f)
                .alpha(pulse)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
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
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.onFocusChanged { 
            isFocused = it.isFocused
            if (isFocused) onFocus()
        }
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(224.dp),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.14f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.White
            ),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(androidx.compose.foundation.BorderStroke(8.dp, Color.White))
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = profile.avatar,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(if (isDimmed && !isFocused) 0.45f else 1f),
                    contentScale = ContentScale.Crop
                )
                
                if (profile.mode == "kids") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(48.dp)
                            .background(Color(0xFFFFD600), CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color.Black)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = profile.name,
            color = if (isFocused) Color.White else Color(0xFF71717A),
            fontSize = 36.sp,
            fontWeight = FontWeight.Black
        )
        
        AnimatedVisibility(visible = isFocused) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (profile.mode == "kids") "KIDS ACCOUNT" else "ADMIN",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.onFocusChanged { 
            isFocused = it.isFocused
            if (isFocused) onFocus()
        }
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(224.dp),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.14f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = if (isFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                focusedContainerColor = Color.White.copy(alpha = 0.1f)
            ),
            border = ClickableSurfaceDefaults.border(
                border = Border(androidx.compose.foundation.BorderStroke(4.dp, if (isFocused) Color.White else Color(0xFF27272A))),
                focusedBorder = Border(androidx.compose.foundation.BorderStroke(4.dp, Color.White))
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = null, 
                    tint = if (isFocused) Color.White else Color(0xFF3F3F46),
                    modifier = Modifier.size(80.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Add Profile",
            color = if (isFocused) Color.White else Color(0xFF3F3F46),
            fontSize = 36.sp,
            fontWeight = FontWeight.Black
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
            .background(Color.Black.copy(alpha = 0.6f))
    ) {
        // Click outside to close
        Box(modifier = Modifier.fillMaxSize().padding(end = 600.dp)) {
            Surface(onClick = onClose, modifier = Modifier.fillMaxSize(), colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent)) {}
        }

        Box(
            modifier = Modifier.fillMaxHeight().width(600.dp).align(Alignment.CenterEnd).background(Color(0xFF0F0F0F))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(64.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Sign In",
                        color = Color.White,
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-2).sp
                    )
                    Surface(
                        onClick = onClose,
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.05f), focusedContainerColor = Color.White)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = if (androidx.tv.material3.LocalContentColor.current == Color.Black) Color.Black else Color.White,
                            modifier = Modifier.padding(16.dp).size(32.dp)
                        )
                    }
                }

                Text(
                    "Sign in with your YouTube account to access your personalized library, history, and more.",
                    color = Color(0xFF71717A),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                // Auth Fields with simplistic simulation for TV (no soft keyboard handling here for brevity)
                AuthField(
                    label = "Email Address", 
                    value = email, 
                    icon = Icons.Default.Email,
                    onValueChange = { email = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
                AuthField(
                    label = "Password", 
                    value = if (password.isEmpty()) "" else "••••••••", 
                    icon = Icons.Default.Lock,
                    onValueChange = { password = it }
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color.Red,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

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
                    modifier = Modifier.fillMaxWidth().height(96.dp),
                    enabled = !isLoading,
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(48.dp)),
                    colors = ClickableSurfaceDefaults.colors(containerColor = Color.White, focusedContainerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoading) {
                            com.youtubekids.youtube.ui.components.SingularityLoader()
                        } else {
                            Text("CONTINUE", color = Color.Black, fontSize = 30.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Black, modifier = Modifier.size(32.dp))
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
            color = Color(0xFF71717A),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
        )
        Surface(
            onClick = { /* In real TV app, this would trigger a soft keyboard or virtual keypad */ 
                // For demo/dev purposes, we'll allow mock entry via hard keyboard if available
            },
            modifier = Modifier.fillMaxWidth(),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.05f),
                focusedContainerColor = Color.White.copy(alpha = 0.1f)
            ),
            border = ClickableSurfaceDefaults.border(
                border = Border(androidx.compose.foundation.BorderStroke(2.dp, if (isFocused) Color.White else Color.Transparent))
            )
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = if (isFocused) Color.White else Color(0xFF71717A), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                if (value.isEmpty()) {
                    Text("Enter $label", color = Color(0xFF3F3F46), fontSize = 24.sp, fontWeight = FontWeight.Medium)
                } else {
                    Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
