package com.ytdlp.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ytdlp.app.engine.VideoInfo

@Composable
fun VideoPreviewCard(
    videoInfo: VideoInfo,
    onConfigureDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialThemeShape,
        colors = CardDefaults.cardColors(containerColor = MaterialThemeCardColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(208.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialThemeSurfaceColor)
            ) {
                if (videoInfo.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = videoInfo.thumbnailUrl,
                        contentDescription = videoInfo.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(208.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                                )
                            )
                    )
                } else {
                    val pulse = rememberInfiniteTransition(label = "preview-placeholder")
                    val alpha = pulse.animateFloat(
                        initialValue = 0.45f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                        label = "placeholder-alpha"
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth().height(208.dp).alpha(alpha.value),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(40.dp))
                    }
                }

                if (videoInfo.durationSeconds > 0) {
                    val minutes = videoInfo.durationSeconds / 60
                    val seconds = videoInfo.durationSeconds % 60
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.82f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            color = Color.White,
                            style = MaterialThemeTypography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = videoInfo.title,
                style = MaterialThemeTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialThemePrimary)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(videoInfo.uploader, style = MaterialThemeTypography.bodyMedium, color = MaterialThemeSecondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (videoInfo.extractor.isNotBlank()) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(videoInfo.extractor.uppercase(), fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialThemePrimary.copy(alpha = 0.12f),
                            labelColor = MaterialThemePrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            if (videoInfo.viewCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialThemeSecondaryText)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("${compactNumber(videoInfo.viewCount)} views", style = MaterialThemeTypography.labelSmall, color = MaterialThemeSecondaryText)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onConfigureDownload,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialThemePrimary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(9.dp))
                Text("Choose quality", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun compactNumber(value: Long): String = when {
    value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000f)
    value >= 1_000 -> String.format("%.1fK", value / 1_000f)
    else -> value.toString()
}

// Aliases keep the component concise while relying on the app's Material theme at runtime.
private val MaterialThemeShape = RoundedCornerShape(22.dp)
private val MaterialThemeCardColor: Color @Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
private val MaterialThemeSurfaceColor: Color @Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.surface
private val MaterialThemePrimary: Color @Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val MaterialThemeSecondaryText: Color @Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
private val MaterialThemeTypography: androidx.compose.material3.Typography @Composable get() = androidx.compose.material3.MaterialTheme.typography
