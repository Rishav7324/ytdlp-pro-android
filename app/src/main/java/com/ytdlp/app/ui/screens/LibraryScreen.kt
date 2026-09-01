package com.ytdlp.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ytdlp.app.data.local.DownloadEntity
import com.ytdlp.app.data.local.MediaType
import com.ytdlp.app.data.scanner.LocalMediaScanner
import com.ytdlp.app.player.MediaPlayerManager
import com.ytdlp.app.ui.components.DownloadItemCard
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
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Media Library",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${displayList.size} files available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                if (activeTab == 1) {
                    IconButton(onClick = { refreshLocalMedia() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan Device Media")
                    }
                } else if (completedList.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearAllCompleted() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Completed", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Library Source Switcher (Downloads vs Device Storage)
        TabRow(
            selectedTabIndex = activeTab,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Downloads (${completedList.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Device Music & Video", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            placeholder = { Text("Search title, album or creator...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = currentFilter == LibraryFilter.ALL,
                onClick = { viewModel.setFilter(LibraryFilter.ALL) },
                label = { Text("All") }
            )
            FilterChip(
                selected = currentFilter == LibraryFilter.VIDEOS,
                onClick = { viewModel.setFilter(LibraryFilter.VIDEOS) },
                label = { Text("Videos") }
            )
            FilterChip(
                selected = currentFilter == LibraryFilter.AUDIO,
                onClick = { viewModel.setFilter(LibraryFilter.AUDIO) },
                label = { Text("Audio") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (activeTab == 0) Icons.Default.FolderOpen else Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (activeTab == 0) "No downloads yet" else "No local media scanned",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (activeTab == 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { refreshLocalMedia() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Scan Device Media")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayList, key = { it.id }) { item ->
                    DownloadItemCard(
                        download = item,
                        onCancel = {},
                        onDelete = { viewModel.deleteDownload(it) },
                        onPlay = { playerManager.playMedia(item, displayList) },
                        onShare = { shareMediaFile(context, item) }
                    )
                }
            }
        }
    }
}

private fun shareMediaFile(context: Context, item: DownloadEntity) {
    if (item.targetPath.isBlank()) return
    val file = File(item.targetPath)
    if (!file.exists()) return

    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val mimeType = if (item.mediaType == MediaType.VIDEO) "video/*" else "audio/*"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share file"))
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to share file: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
