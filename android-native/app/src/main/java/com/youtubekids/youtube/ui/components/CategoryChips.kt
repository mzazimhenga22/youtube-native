@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    Category("Trending", Icons.Default.TrendingUp, Color(0xFF00AAFF)),
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
        modifier = modifier.padding(vertical = 24.dp),
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(CATEGORIES) { category ->
            val isSelected = selectedCategory == category.label
            
            Surface(
                onClick = { onCategorySelected(category.label) },
                modifier = Modifier.wrapContentSize(),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(32.dp)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        category.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.Black else category.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = category.label,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
