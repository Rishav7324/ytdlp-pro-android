package com.ytdlp.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ytdlp.app.data.local.MediaType
import com.ytdlp.app.engine.DownloadFormat
import com.ytdlp.app.engine.VideoInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatSelectionSheet(
    videoInfo: VideoInfo,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onStartDownload: (formatId: String, mediaType: MediaType, audioExt: String) -> Unit,
    onQueueDownload: (formatId: String, mediaType: MediaType, audioExt: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Video, 1: Audio
    val videoFormats = remember(videoInfo) { videoInfo.formats.filter { !it.isAudioOnly } }
    val audioFormats = remember(videoInfo) { videoInfo.formats.filter { it.isAudioOnly } }

    var selectedVideoFormat by remember {
        mutableStateOf(videoFormats.firstOrNull()?.formatId ?: "bestvideo+bestaudio/best")
    }
    var selectedAudioFormat by remember {
        mutableStateOf(audioFormats.firstOrNull()?.extension ?: "mp3")
    }

    var embedThumbnail by remember { mutableStateOf(true) }
    var embedSubtitles by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Download Options",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Video (MP4)") },
                    icon = { Icon(Icons.Default.Videocam, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Audio Only") },
                    icon = { Icon(Icons.Default.Audiotrack, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                Text(
                    text = "Resolution / Quality",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    videoFormats.forEach { fmt ->
                        FilterChip(
                            selected = selectedVideoFormat == fmt.formatId,
                            onClick = { selectedVideoFormat = fmt.formatId },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(fmt.resolution, fontWeight = FontWeight.Bold)
                                    Text(fmt.note, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Audio Codec / Quality",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    audioFormats.forEach { fmt ->
                        FilterChip(
                            selected = selectedAudioFormat == fmt.extension,
                            onClick = { selectedAudioFormat = fmt.extension },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(fmt.resolution, fontWeight = FontWeight.Bold)
                                    Text(fmt.note, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Embed Video Thumbnail", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = embedThumbnail, onCheckedChange = { embedThumbnail = it })
            }

            if (selectedTab == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Embed Subtitles", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = embedSubtitles, onCheckedChange = { embedSubtitles = it })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val mediaType = if (selectedTab == 0) MediaType.VIDEO else MediaType.AUDIO
                        val formatId = if (selectedTab == 0) selectedVideoFormat else "bestaudio/best"
                        onQueueDownload(formatId, mediaType, selectedAudioFormat)
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Queue, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Queue")
                }

                Button(
                    onClick = {
                        val mediaType = if (selectedTab == 0) MediaType.VIDEO else MediaType.AUDIO
                        val formatId = if (selectedTab == 0) selectedVideoFormat else "bestaudio/best"
                        onStartDownload(formatId, mediaType, selectedAudioFormat)
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
