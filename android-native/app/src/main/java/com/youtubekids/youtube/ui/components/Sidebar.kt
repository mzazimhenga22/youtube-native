@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.ui.Profile

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val id: String
)

@Composable
fun Sidebar(
    side: String = "left",
    selectedRoute: String,
    currentProfile: Profile? = null,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val leftItems = listOf(
        NavigationItem("Home", Icons.Default.Home, "home", "home"),
        NavigationItem("Shorts", Icons.Default.PlayArrow, "shorts", "shorts"),
        NavigationItem("Music", Icons.Default.MusicNote, "music", "music"),
        NavigationItem("Live", Icons.Default.LiveTv, "live", "live"),
        NavigationItem("Movies", Icons.Default.Movie, "movies", "movies")
    )

    val rightItems = listOf(
        NavigationItem("Search", Icons.Default.Search, "search", "search"),
        NavigationItem("Ask AI", Icons.Default.AutoAwesome, "ask", "ask"),
        NavigationItem("Subscriptions", Icons.Default.Subscriptions, "subscriptions", "subscriptions"),
        NavigationItem("Library", Icons.Default.VideoLibrary, "library", "library"),
        NavigationItem("Settings", Icons.Default.Settings, "settings", "settings")
    )

    val items = if (side == "left") leftItems else rightItems

    var isFocused by remember { mutableStateOf(false) }
    val width by animateDpAsState(
        targetValue = if (isFocused) 220.dp else 64.dp,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "sidebarWidth"
    )

    val bgAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.85f else 0.4f,
        animationSpec = tween(300),
        label = "bgAlpha"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 16.dp else 0.dp,
        animationSpec = tween(300),
        label = "shadow"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(width)
            .onFocusChanged { isFocused = it.hasFocus }
            .padding(vertical = 40.dp, horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF1A1A1A).copy(alpha = bgAlpha),
                            Color(0xFF0A0A0A).copy(alpha = bgAlpha + 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .border(
                    1.dp,
                    if (isFocused) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f),
                    RoundedCornerShape(32.dp)
                )
                .padding(vertical = 24.dp, horizontal = 8.dp),
            horizontalAlignment = if (isFocused) Alignment.Start else Alignment.CenterHorizontally
        ) {
            // Profile Avatar (Left sidebar only)
            if (side == "left") {
                Box(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .then(if (isFocused) Modifier.padding(start = 12.dp) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        onClick = { onNavigate("settings") },
                        modifier = Modifier.size(48.dp),
                        shape = ClickableSurfaceDefaults.shape(CircleShape),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.2f),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color(0xFF2A2A2A),
                            focusedContainerColor = Color.White
                        ),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(
                                androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                            )
                        ),
                        glow = ClickableSurfaceDefaults.glow(
                            focusedGlow = Glow(
                                elevationColor = Color.White.copy(alpha = 0.2f),
                                elevation = 12.dp
                            )
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (currentProfile?.avatar != null) {
                                AsyncImage(
                                    model = currentProfile.avatar,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (androidx.tv.material3.LocalContentColor.current == Color.Black) Color.Black else Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Divider
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .then(if (isFocused) Modifier.padding(horizontal = 16.dp) else Modifier.padding(horizontal = 12.dp))
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.White.copy(alpha = 0.1f), Color.Transparent)
                            )
                        )
                )
            }

            // Navigation Items
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    val isSelected = selectedRoute == item.route || (item.route == "home" && selectedRoute == "")
                    var isItemFocused by remember { mutableStateOf(false) }

                    Surface(
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .onFocusChanged { isItemFocused = it.isFocused },
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                            focusedContainerColor = Color.White
                        ),
                        glow = ClickableSurfaceDefaults.glow(
                            focusedGlow = Glow(
                                elevationColor = Color.White.copy(alpha = 0.15f),
                                elevation = 10.dp
                            )
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = if (isFocused) 16.dp else 0.dp),
                            horizontalArrangement = if (isFocused) {
                                if (side == "right") Arrangement.End else Arrangement.Start
                            } else Arrangement.Center
                        ) {
                            if (side == "right" && isFocused) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isItemFocused) Color.Black else Color.White,
                                    modifier = Modifier.padding(end = 16.dp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Icon with aura when item is selected but not focused
                            Box(contentAlignment = Alignment.Center) {
                                if (isSelected && !isItemFocused) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                    )
                                }
                                Icon(
                                    item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isItemFocused) Color.Black
                                           else if (isSelected) Color.White
                                           else Color.White.copy(alpha = 0.4f)
                                )
                            }

                            if (side == "left" && isFocused) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isItemFocused) Color.Black else Color.White,
                                    modifier = Modifier.padding(start = 16.dp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
