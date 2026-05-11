@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

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

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        // Ambient blob
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 200.dp, y = 200.dp)
                .size(400.dp)
                .alpha(0.03f)
                .background(Color.White, CircleShape)
                .blur(80.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp) // Clear floating header
        ) {
            // ── Left: Category Nav ──
            Column(
                modifier = Modifier
                    .width(340.dp)
                    .fillMaxHeight()
                    .padding(start = 80.dp, end = 16.dp, top = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp, start = 12.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "SETTINGS",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

            // ── Right: Details ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 24.dp, end = 80.dp, top = 20.dp)
            ) {
                val currentCat = categories.find { it.id == activeCategory }

                // Section header
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = currentCat?.label ?: "",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = currentCat?.description ?: "",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val content = settingsContent[activeCategory] ?: emptyList()
                    if (content.isNotEmpty()) {
                        items(content) { item ->
                            DetailedSettingItem(item = item, onClick = {
                                if (item.action == "switch" || item.action == "add") {
                                    onLogout()
                                }
                            })
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Coming soon",
                                        color = Color.White.copy(alpha = 0.2f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
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
            .height(48.dp)
            .onFocusChanged {
                isFocused = it.isFocused
                if (isFocused) onFocus()
            },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = if (category.isAction) Color.Red.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.08f)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (category.isAction) Color.Red.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                category.icon,
                contentDescription = null,
                tint = when {
                    category.isAction && (isActive || isFocused) -> Color.Red
                    isActive || isFocused -> Color.White
                    else -> Color.White.copy(alpha = 0.25f)
                },
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = category.label,
                color = when {
                    category.isAction && (isActive || isFocused) -> Color.Red
                    isActive || isFocused -> Color.White
                    else -> Color.White.copy(alpha = 0.4f)
                },
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (isActive && !category.isAction) {
                Box(modifier = Modifier.size(5.dp).background(Color.White, CircleShape))
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
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.04f),
            focusedContainerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.isProfile) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    AsyncImage(
                        model = item.avatar ?: "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    color = if (isFocused) Color.Black else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.value,
                    color = if (isFocused) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isFocused) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
