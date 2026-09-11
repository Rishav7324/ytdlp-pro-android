package com.ytdlp.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ytdlp.app.data.local.DownloadEntity
import com.ytdlp.app.data.local.MediaType
import com.ytdlp.app.data.scanner.LocalMediaScanner
import com.ytdlp.app.player.MediaPlayerManager
import com.ytdlp.app.ui.components.AppleSpringSpec
import com.ytdlp.app.ui.components.DownloadItemCard
import com.ytdlp.app.ui.components.LiquidGlassCard
import com.ytdlp.app.ui.components.LiquidGlassPill
import com.ytdlp.app.ui.components.liquidGlass
import com.ytdlp.app.viewmodel.LibraryFilter
import com.ytdlp.app.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val playerManager = MediaPlayerManager.getInstance(context)
    val completedList by viewModel.completedDownloads.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Downloads, 1: Device Storage
    val localDeviceMedia = remember { mutableStateListOf<DownloadEntity>() }
    var isScanning by remember { mutableStateOf(false) }

    fun refreshLocalMedia() {
        scope.launch {
            isScanning = true
            val audio = LocalMediaScanner.scanLocalAudio(context)
            val videos = LocalMediaScanner.scanLocalVideos(context)
            localDeviceMedia.clear()
            localDeviceMedia.addAll(audio + videos)
            isScanning = false
        }
    }

    LaunchedEffect(activeTab) {
        if (activeTab == 1 && localDeviceMedia.isEmpty()) {
            refreshLocalMedia()
        }
    }

    val displayList = if (activeTab == 0) completedList else localDeviceMedia.filter { item ->
        val matchesFilter = when (currentFilter) {
            LibraryFilter.ALL -> true
            LibraryFilter.VIDEOS -> item.mediaType == MediaType.VIDEO
            LibraryFilter.AUDIO -> item.mediaType == MediaType.AUDIO
        }
        val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.uploader.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Media Library",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${displayList.size} files available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (activeTab == 1) {
                    IconButton(
                        onClick = { refreshLocalMedia() },
                        modifier = Modifier
                            .size(38.dp)
                            .liquidGlass(shape = CircleShape, elevation = 2.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = "Scan Device Media",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else if (completedList.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearAllCompleted() },
                        modifier = Modifier
                            .size(38.dp)
                            .liquidGlass(shape = CircleShape, elevation = 2.dp)
                    ) {
                        Icon(
                            Icons.Rounded.DeleteSweep,
                            contentDescription = "Clear Completed",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // iOS Liquid Glass Segmented Control
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Downloads Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (activeTab == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent
                        )
                        .clickable { activeTab = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Downloads (${completedList.size})",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        fontWeight = if (activeTab == 0) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (activeTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Device Storage Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (activeTab == 1) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent
                        )
                        .clickable { activeTab = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Device Storage",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        fontWeight = if (activeTab == 1) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (activeTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar with Liquid Glass
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            placeholder = { Text("Search songs, videos or creators...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Pills
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiquidGlassPill(
                isSelected = currentFilter == LibraryFilter.ALL,
                onClick = { viewModel.setFilter(LibraryFilter.ALL) }
            ) {
                Text(
                    text = "All",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            LiquidGlassPill(
                isSelected = currentFilter == LibraryFilter.VIDEOS,
                onClick = { viewModel.setFilter(LibraryFilter.VIDEOS) }
            ) {
                Icon(
                    Icons.Rounded.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Videos",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            LiquidGlassPill(
                isSelected = currentFilter == LibraryFilter.AUDIO,
                onClick = { viewModel.setFilter(LibraryFilter.AUDIO) }
            ) {
                Icon(
                    Icons.Rounded.Audiotrack,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Audio",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(26.dp),
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .liquidGlass(shape = CircleShape, elevation = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (activeTab == 0) Icons.Rounded.FolderOpen else Icons.Rounded.PhoneAndroid,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (activeTab == 0) "No Downloads Yet" else "No Device Media Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (activeTab == 0) "Downloaded videos and audio will appear here." else "Ensure storage permission is granted to scan local files.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                items(displayList, key = { it.id }) { item ->
                    DownloadItemCard(
                        download = item,
                        onCancel = { },
                        onDelete = { id -> viewModel.deleteDownload(id) },
                        onPlay = { playerManager.playMedia(item) },
                        onShare = { shareMedia(context, item) }
                    )
                }
            }
        }
    }
}

private fun shareMedia(context: Context, item: DownloadEntity) {
    try {
        val file = File(item.targetPath)
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist on disk", Toast.LENGTH_SHORT).show()
            return
        }
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val mime = if (item.mediaType == MediaType.VIDEO) "video/*" else "audio/*"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share ${item.title}"))
    } catch (e: Exception) {
        Toast.makeText(context, "Share failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
