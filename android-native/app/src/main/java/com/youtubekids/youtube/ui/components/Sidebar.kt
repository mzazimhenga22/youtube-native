@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

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

@OptIn(ExperimentalTvMaterial3Api::class)
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
        targetValue = if (isFocused) 240.dp else 80.dp,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "sidebarWidth"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(width)
            .onFocusChanged { isFocused = it.hasFocus }
            .padding(vertical = 40.dp)
            .padding(horizontal = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(32.dp)
                ),
            horizontalAlignment = if (isFocused) Alignment.Start else Alignment.CenterHorizontally
        ) {
            // Profile Avatar (Left only)
            if (side == "left") {
                Surface(
                    onClick = { onNavigate("settings") },
                    modifier = Modifier
                        .size(56.dp)
                        .padding(bottom = 24.dp),
                    shape = ClickableSurfaceDefaults.shape(CircleShape),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                    colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF181818))
                ) {
                    if (currentProfile?.avatar != null) {
                        AsyncImage(
                            model = currentProfile.avatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                }
                
                // Divider
                Box(modifier = Modifier
                    .height(2.dp)
                    .width(if (isFocused) 40.dp else 24.dp)
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(vertical = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Navigation Items
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    val isSelected = selectedRoute == item.route || (item.route == "home" && selectedRoute == "")
                    
                    Surface(
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (isSelected) Color.White.copy(alpha = 0.15f) else Color(0xFF181818),
                            focusedContainerColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = if (isFocused) 20.dp else 0.dp, vertical = 14.dp),
                            horizontalArrangement = if (isFocused) (if (side == "right") Arrangement.End else Arrangement.Start) else Arrangement.Center
                        ) {
                            if (side == "right" && isFocused) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) Color.White else Color.Black,
                                    modifier = Modifier.padding(end = 16.dp),
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(26.dp),
                                tint = if (isSelected) Color.White else (if (isFocused) Color.Black else Color.White.copy(alpha = 0.6f))
                            )

                            if (side == "left" && isFocused) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) Color.White else Color.Black,
                                    modifier = Modifier.padding(start = 16.dp),
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
