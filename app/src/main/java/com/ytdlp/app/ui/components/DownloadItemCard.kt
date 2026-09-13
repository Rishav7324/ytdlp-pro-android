package com.ytdlp.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.ytdlp.app.data.local.DownloadEntity
import com.ytdlp.app.data.local.DownloadStatus
import com.ytdlp.app.data.local.MediaType
import com.ytdlp.app.ui.theme.AccentGreen
import com.ytdlp.app.ui.theme.AccentRed
import com.ytdlp.app.ui.theme.NovaCyan
import com.ytdlp.app.ui.theme.NovaCyanDeep
import java.io.File

@Composable
fun DownloadItemCard(
    download: DownloadEntity,
    onCancel: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onPlay: ((DownloadEntity) -> Unit)? = null,
    onShare: ((DownloadEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Media Thumbnail with VideoFrameDecoder support & rich gradient fallback
            MediaThumbnailView(download = download)

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = download.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                when (download.status) {
                    DownloadStatus.DOWNLOADING -> {
                        val animatedProgress by animateFloatAsState(
                            targetValue = (download.progress / 100f).coerceIn(0f, 1f),
                            animationSpec = AppleSmoothSpringSpec,
                            label = "progress-anim"
                        )

                        // Liquid Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(NovaCyan, NovaCyanDeep)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${download.progress.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = NovaCyanDeep
                            )
                            if (download.speed.isNotBlank()) {
                                Text(
                                    text = download.speed,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (download.eta.isNotBlank()) {
                                Text(
                                    text = "ETA ${download.eta}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    DownloadStatus.QUEUED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Queued for download...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DownloadStatus.COMPLETED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Downloaded",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentGreen,
                                fontWeight = FontWeight.Bold
                            )
                            if (download.fileSize > 0) {
                                Text(
                                    text = " • ${download.formattedFileSize()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    DownloadStatus.FAILED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Error,
                                contentDescription = null,
                                tint = AccentRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = download.errorMessage ?: "Download Failed",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentRed,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    DownloadStatus.CANCELLED -> {
                        Text(
                            text = "Cancelled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    DownloadStatus.PAUSED -> {
                        Text(
                            text = "Paused",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (download.status == DownloadStatus.DOWNLOADING || download.status == DownloadStatus.QUEUED) {
                    IconButton(
                        onClick = { onCancel(download.id) },
                        modifier = Modifier
                            .size(38.dp)
                            .liquidGlass(shape = CircleShape, elevation = 2.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Cancel",
                            tint = AccentRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else if (download.status == DownloadStatus.COMPLETED) {
                    if (onPlay != null) {
                        IconButton(
                            onClick = { onPlay(download) },
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(8.dp, CircleShape, spotColor = NovaCyanDeep)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(NovaCyan, NovaCyanDeep)
                                    )
                                )
                        ) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = Color(0xFF041724),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    if (onShare != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onShare(download) },
                            modifier = Modifier
                                .size(38.dp)
                                .liquidGlass(shape = CircleShape, elevation = 2.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = { onDelete(download.id) },
                        modifier = Modifier
                            .size(38.dp)
                            .liquidGlass(shape = CircleShape, elevation = 2.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Resilient Media Thumbnail with hardware video frame decoding and jewel gradient fallbacks.
 */
@Composable
private fun MediaThumbnailView(
    download: DownloadEntity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isVideo = download.mediaType == MediaType.VIDEO
    val isDark = isSystemInDarkTheme()

    val imageModel = remember(download.thumbnailUrl, download.targetPath) {
        when {
            download.thumbnailUrl.isNotBlank() -> download.thumbnailUrl
            download.targetPath.isNotBlank() -> File(download.targetPath)
            else -> null
        }
    }

    val request = remember(imageModel) {
        if (imageModel != null) {
            ImageRequest.Builder(context)
                .data(imageModel)
                .apply {
                    if (isVideo) {
                        videoFrameMillis(1500)
                    }
                }
                .crossfade(true)
                .build()
        } else null
    }

    val fallbackGradient = if (isVideo) {
        Brush.linearGradient(
            listOf(
                Color(0xFF0077B6),
                Color(0xFF00B4D8),
                Color(0xFF90E0EF)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xFF6A0DAD),
                Color(0xFF9D4EDD),
                Color(0xFFC77DFF)
            )
        )
    }

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .size(68.dp)
            .shadow(6.dp, shape, spotColor = if (isVideo) Color(0xFF00B4D8).copy(alpha = 0.35f) else Color(0xFF9D4EDD).copy(alpha = 0.35f))
            .clip(shape)
            .background(fallbackGradient)
            .border(
                1.dp,
                if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.85f),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Fallback icon behind the AsyncImage
        Icon(
            imageVector = if (isVideo) Icons.Rounded.PlayCircle else Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(32.dp)
        )

        if (request != null) {
            AsyncImage(
                model = request,
                contentDescription = download.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
