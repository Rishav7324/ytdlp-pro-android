package com.ytdlp.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ytdlp.app.data.local.MediaType
import com.ytdlp.app.engine.DownloadFormat
import com.ytdlp.app.engine.VideoInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatSelectionSheet(videoInfo: VideoInfo, sheetState: SheetState, onDismiss: () -> Unit, onStartDownload: (String, MediaType, String) -> Unit, onQueueDownload: (String, MediaType, String) -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    val videos = remember(videoInfo) { videoInfo.formats.filter { !it.isAudioOnly }.filter { it.resolution.endsWith("p") }.sortedByDescending { it.resolution.removeSuffix("p").toIntOrNull() ?: 0 }.distinctBy { it.resolution }.take(8) }
    val best = DownloadFormat("bestvideo+bestaudio/best", "mp4", "Best", "Highest available quality", false)
    val qualities = remember(videos) { listOf(best) + videos }
    val audioOutputs = listOf("mp3", "m4a", "opus", "wav")
    var selectedVideo by remember(qualities) { mutableStateOf(qualities.first().formatId) }
    var selectedAudio by remember { mutableStateOf("mp3") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        LazyColumn(contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            item {
                Text("Download quality", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text(videoInfo.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Video") }, leadingIcon = { Icon(Icons.Default.Videocam, null, Modifier.size(18.dp)) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Audio") }, leadingIcon = { Icon(Icons.Default.Audiotrack, null, Modifier.size(18.dp)) }, modifier = Modifier.weight(1f))
                }
            }
            if (tab == 0) {
                item { Text("Resolution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(qualities, key = { it.formatId }) { format -> QualityCard(format, selectedVideo == format.formatId) { selectedVideo = format.formatId } }
            } else {
                item { Text("Audio output", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(audioOutputs, key = { it }) { ext -> AudioCard(ext, selectedAudio == ext) { selectedAudio = ext } }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f)), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(if (tab == 0) "Best available video + audio" else "High-quality audio extraction", fontWeight = FontWeight.SemiBold)
                            Text("The engine will merge or extract automatically when required.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { val type = if (tab == 0) MediaType.VIDEO else MediaType.AUDIO; onQueueDownload(if (tab == 0) selectedVideo else "bestaudio/best", type, selectedAudio) }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Queue, null); Spacer(Modifier.width(6.dp)); Text("Queue") }
                    Button(onClick = { val type = if (tab == 0) MediaType.VIDEO else MediaType.AUDIO; onStartDownload(if (tab == 0) selectedVideo else "bestaudio/best", type, selectedAudio) }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Download, null); Spacer(Modifier.width(6.dp)); Text("Download") }
                }
            }
        }
    }
}

@Composable
private fun QualityCard(format: DownloadFormat, selected: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Videocam, null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(format.resolution, fontWeight = FontWeight.ExtraBold)
                Text(format.note.ifBlank { "Available stream" }, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(format.extension.uppercase() + (format.fps?.let { " • ${it}fps" } ?: ""), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            if (selected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun AudioCard(extension: String, selected: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Audiotrack, null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(extension.uppercase(), fontWeight = FontWeight.ExtraBold)
                Text(if (extension == "mp3") "Universal • high compatibility" else "High-quality converted audio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
