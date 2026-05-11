@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.ui.AppViewModel

data class SettingsCategory(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val description: String,
    val isAction: Boolean = false
)

data class SettingItem(
    val label: String,
    val value: String,
    val isProfile: Boolean = false,
    val avatar: String? = null,
    val action: String? = null
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: AppViewModel = hiltViewModel()
) {
    val currentProfile by viewModel.currentProfile.collectAsState()
    var activeCategory by remember { mutableStateOf("account") }

    val categories = listOf(
        SettingsCategory("account", "Account", Icons.Default.Person, "Manage your profiles and accounts."),
        SettingsCategory("billing", "Billing", Icons.Default.CreditCard, "View subscriptions and payments."),
        SettingsCategory("privacy", "Privacy", Icons.Default.Shield, "Control history and location settings."),
        SettingsCategory("playback", "Playback", Icons.Default.PlayCircle, "Customize autoplay and captions."),
        SettingsCategory("quality", "Quality", Icons.Default.Monitor, "Select video resolution preferences."),
        SettingsCategory("about", "About", Icons.Default.Help, "System version and diagnostic info."),
        SettingsCategory("logout", "Sign Out", Icons.Default.Logout, "Sign out of your account on this device.", true)
    )

    val settingsContent = mapOf(
        "account" to listOf(
            SettingItem(currentProfile?.name ?: "Guest", currentProfile?.id ?: "Not signed in", isProfile = true, avatar = currentProfile?.avatar),
            SettingItem("Switch Profile", "Change who is watching", action = "switch"),
            SettingItem("Add Account", "Sign in to another YouTube account", action = "add")
        ),
        "playback" to listOf(
            SettingItem("Autoplay", "On (recommended)"),
            SettingItem("Captions", "English (US)"),
            SettingItem("Stats for Nerds", "Off")
        ),
        "quality" to listOf(
            SettingItem("Resolution", "Auto (up to 4K)"),
            SettingItem("Dynamic Range", "HDR10+"),
            SettingItem("Color Depth", "10-bit")
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        // Ambient Decorative Shape
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 200.dp, y = 200.dp)
                .size(600.dp)
                .alpha(0.05f)
                .background(Color.White, CircleShape)
                .blur(100.dp)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            // Left Column: Navigation
            Column(
                modifier = Modifier
                    .width(480.dp)
                    .fillMaxHeight()
                    .padding(top = 80.dp, start = 48.dp, end = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 64.dp, start = 24.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF71717A), modifier = Modifier.size(24.dp))
                    Text(
                        "SETTINGS",
                        color = Color(0xFF71717A),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        CategoryItem(
                            category = cat,
                            isActive = activeCategory == cat.id && !cat.isAction,
                            onFocus = { if (!cat.isAction) activeCategory = cat.id },
                            onClick = { if (cat.isAction) onLogout() }
                        )
                    }
                }
            }

            // Right Column: Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 80.dp, end = 80.dp, start = 48.dp)
            ) {
                val currentCat = categories.find { it.id == activeCategory }
                
                // Section Header
                Column(modifier = Modifier.padding(bottom = 64.dp)) {
                    Text(
                        text = currentCat?.label ?: "",
                        color = Color.White,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        text = currentCat?.description ?: "",
                        color = Color(0xFF71717A),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val content = settingsContent[activeCategory] ?: emptyList()
                    if (content.isNotEmpty()) {
                        items(content) { item ->
                            DetailedSettingItem(item = item, onClick = {
                                if (item.action == "switch" || item.action == "add") {
                                    onLogout() // For now, go back to profile picker
                                }
                            })
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(40.dp))
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF272727), modifier = Modifier.size(64.dp))
                                    Text(
                                        "Advanced features in development",
                                        color = Color(0xFF3F3F46),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(top = 24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CategoryItem(
    category: SettingsCategory,
    isActive: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { 
                isFocused = it.isFocused
                if (isFocused) onFocus()
            },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = if (category.isAction) Color.Red.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.1f)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, if (category.isAction) Color.Red.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f)))
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                category.icon,
                contentDescription = null,
                tint = if (isActive || isFocused) (if (category.isAction) Color.Red else Color.White) else Color(0xFF71717A),
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = category.label,
                color = if (isActive || isFocused) (if (category.isAction) Color.Red else Color.White) else Color(0xFF71717A),
                fontSize = 28.sp,
                fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                modifier = Modifier.padding(start = 24.dp).weight(1f)
            )
            if (isActive) {
                Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DetailedSettingItem(item: SettingItem, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(32.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.05f),
            focusedContainerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.isProfile) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(2.dp)
                ) {
                    AsyncImage(
                        model = item.avatar ?: "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    color = if (isFocused) Color.Black else Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = item.value,
                    color = if (isFocused) Color.Black.copy(alpha = 0.6f) else Color(0xFF71717A),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isFocused) Color.Black else Color(0xFF3F3F46),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
