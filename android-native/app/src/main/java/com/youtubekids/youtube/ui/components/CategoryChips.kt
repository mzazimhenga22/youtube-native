@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

data class Category(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

val CATEGORIES = listOf(
    Category("Recommended", Icons.Default.AutoAwesome, Color(0xFFFF0055)),
    Category("Trending", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF00AAFF)),
    Category("Gaming", Icons.Default.Gamepad, Color(0xFF00FF99)),
    Category("Music", Icons.Default.MusicNote, Color(0xFF7700FF)),
    Category("Movies", Icons.Default.Movie, Color(0xFFFF9900)),
    Category("Tech", Icons.Default.Computer, Color(0xFF00FFFF)),
    Category("News", Icons.Default.Newspaper, Color(0xFFFFCC00)),
    Category("Cooking", Icons.Default.Restaurant, Color(0xFFFF66CC)),
    Category("Nature", Icons.Default.Park, Color(0xFF33FF33))
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(CATEGORIES) { category ->
            val isSelected = selectedCategory == category.label

            Surface(
                onClick = { onCategorySelected(category.label) },
                modifier = Modifier.height(40.dp),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.06f),
                    focusedContainerColor = Color.White
                ),
                border = ClickableSurfaceDefaults.border(
                    border = Border(
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.08f)
                        )
                    ),
                    focusedBorder = Border(
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        category.icon,
                        contentDescription = null,
                        tint = if (isSelected) category.color else category.color.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = category.label,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
