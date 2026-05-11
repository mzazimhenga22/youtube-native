@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.youtubekids.youtube.data.model.Video

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DescriptionPanel(
    video: Video,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(450.dp)
            .background(Color(0xFF0F0F0F))
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Description",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Surface(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp),
                    shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
                    colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                video.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(video.views, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(16.dp))
                Text(video.publishedAt ?: "", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Channel Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.DarkGray)) {
                    // Avatar placeholder
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(video.channel, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("1.2M subscribers", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                video.description ?: "No description available.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
            )

            Spacer(modifier = Modifier.height(48.dp))

            // AI Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E), MaterialTheme.shapes.large)
                    .padding(24.dp)
            ) {
                Text(
                    "ABOUT THIS VIDEO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "This content was created using AI tools to enhance visual fidelity and sound design. Learn more about YouTube's AI disclosure policies.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
