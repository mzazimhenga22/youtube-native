@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

private val keys = listOf(
    listOf("A", "B", "C", "D", "E", "F", "G"),
    listOf("H", "I", "J", "K", "L", "M", "N"),
    listOf("O", "P", "Q", "R", "S", "T", "U"),
    listOf("V", "W", "X", "Y", "Z", "1", "2"),
    listOf("3", "4", "5", "6", "7", "8", "9"),
    listOf("0", "SPACE", "BACKSPACE", "SEARCH")
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvKeyboard(
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(40.dp))
            .padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { key ->
                        val isSpecial = key in listOf("SPACE", "BACKSPACE", "SEARCH")
                        
                        Surface(
                            onClick = {
                                when (key) {
                                    "BACKSPACE" -> onDelete()
                                    "SEARCH" -> onEnter()
                                    "SPACE" -> onKeyPress(" ")
                                    else -> onKeyPress(key.lowercase())
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .height(64.dp)
                                .then(
                                    if (isSpecial) Modifier.width(160.dp)
                                    else Modifier.width(64.dp)
                                ),
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = if (key == "SEARCH") Color(0xFFEF4444) else Color.White.copy(alpha = 0.05f),
                                focusedContainerColor = Color.White,
                                pressedContainerColor = Color.White.copy(alpha = 0.8f)
                            ),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                when (key) {
                                    "BACKSPACE" -> Icon(
                                        Icons.Default.Backspace,
                                        contentDescription = "Backspace",
                                        modifier = Modifier.size(28.dp),
                                        tint = LocalContentColor.current
                                    )
                                    "SPACE" -> Icon(
                                        Icons.Default.SpaceBar,
                                        contentDescription = "Space",
                                        modifier = Modifier.size(28.dp),
                                        tint = LocalContentColor.current
                                    )
                                    "SEARCH" -> Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        modifier = Modifier.size(28.dp),
                                        tint = LocalContentColor.current
                                    )
                                    else -> Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 24.sp
                                        ),
                                        color = LocalContentColor.current
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
