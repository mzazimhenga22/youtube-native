@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.youtubekids.youtube.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.youtubekids.youtube.data.model.Video

@Composable
fun StatsForNerds(
    video: Video,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Surface(
            onClick = onClose,
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
            colors = ClickableSurfaceDefaults.colors(containerColor = Color.Black.copy(alpha = 0.85f), focusedContainerColor = Color.Black)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stats for Nerds",
                        color = Color.Red,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                StatRow("Video ID", video.id)
                StatRow("Resolution", "3840x2160@60 (4K HDR)")
                StatRow("Codec", "vp09.02.51.10.01.01.01.01.00 (248) / opus (251)")
                StatRow("Bitrate", "24.5 Mbps")
                StatRow("Buffer Health", "124.5s")
                StatRow("Network Activity", "5.2 MB/s")
                StatRow("Connection Speed", "85.4 Mbps")
                StatRow("Dropped Frames", "0 / 14500")
                StatRow("Audio Language", "en-US (Primary)")
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.width(400.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
