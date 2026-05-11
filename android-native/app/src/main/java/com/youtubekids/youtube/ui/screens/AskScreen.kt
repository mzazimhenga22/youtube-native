@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AskScreen(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
    ) {
        // Purple Glow Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF9333EA).copy(alpha = 0.15f), Color.Transparent),
                        radius = 2000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Ask YouTube AI",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Ask anything about what's live, scores, or find exactly what you want to watch.",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(700.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                onClick = { onSearch("") },
                modifier = Modifier.width(700.dp).height(80.dp),
                shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.medium),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color.White.copy(alpha = 0.2f)
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(
                        "Try \"What's the score of the Lakers game?\"",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Surface(
                    onClick = { /* Voice search intent */ },
                    modifier = Modifier.size(80.dp),
                    shape = ClickableSurfaceDefaults.shape(shape = androidx.compose.foundation.shape.CircleShape),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color(0xFF9333EA),
                        focusedContainerColor = Color(0xFFA855F7)
                    ),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SuggestionChip("Live Sports", onSearch)
                    SuggestionChip("Recent Comedies", onSearch)
                    SuggestionChip("Cooking Shows", onSearch)
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SuggestionChip(text: String, onClick: (String) -> Unit) {
    Surface(
        onClick = { onClick(text) },
        shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.medium),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.05f),
            focusedContainerColor = Color.White
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            color = if (LocalContentColor.current == Color.White) Color.Gray else Color.Black
        )
    }
}
