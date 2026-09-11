package com.ytdlp.app.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.ytdlp.app.player.MediaPlayerManager
import com.ytdlp.app.ui.components.equalizer.EqualizerDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerSheet(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val manager = MediaPlayerManager.getInstance(context)
    val media by manager.currentMedia.collectAsState()
    val queue by manager.queue.collectAsState()
    val playing by manager.isPlaying.collectAsState()
    val position by manager.currentPosition.collectAsState()
    val duration by manager.duration.collectAsState()
    val speed by manager.playbackSpeed.collectAsState()
    val repeat by manager.repeatMode.collectAsState()
    val shuffle by manager.isShuffleEnabled.collectAsState()
    val pointA by manager.loopPointA.collectAsState()
    val pointB by manager.loopPointB.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var equalizer by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val item = media ?: return
    val artScale by animateFloatAsState(if (playing) 1f else .96f, tween(280), label = "art-scale")

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), dragHandle = null) {
        LazyColumn(modifier = Modifier.fillMaxWidth().navigationBarsPadding(), contentPadding = PaddingValues(18.dp, 8.dp, 18.dp, 26.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.KeyboardArrowDown, "Collapse", Modifier.size(30.dp)) }
                    Text("NOW PLAYING", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { equalizer = true }) { Icon(Icons.Default.Tune, "Equalizer", tint = MaterialTheme.colorScheme.primary) }
                }
            }
            item {
                TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f), modifier = Modifier.clip(RoundedCornerShape(16.dp))) {
                    Tab(tab == 0, { tab = 0 }, text = { Text("Player") })
                    Tab(tab == 1, { tab = 1 }, text = { Text("Queue ${queue.size}") })
                    Tab(tab == 2, { tab = 2 }, text = { Text("Details") })
                }
            }
            if (tab == 0) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Card(modifier = Modifier.size(250.dp).scale(artScale), shape = RoundedCornerShape(30.dp), elevation = CardDefaults.cardElevation(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            if (item.thumbnailUrl.isNotBlank()) AsyncImage(item.thumbnailUrl, item.title, Modifier.fillMaxWidth(), contentScale = ContentScale.Crop)
                            else Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(Icons.Default.MusicNote, null, Modifier.size(82.dp), tint = MaterialTheme.colorScheme.primary) }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(item.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(item.uploader.ifBlank { "NovaFetch Audio" }, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                item {
                    val safeDuration = duration.coerceAtLeast(1L)
                    Slider(value = position.coerceIn(0L, safeDuration).toFloat(), onValueChange = { manager.seekTo(it.toLong()) }, valueRange = 0f..safeDuration.toFloat(), modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(formatDuration(position), style = MaterialTheme.typography.labelSmall); Text(formatDuration(duration), style = MaterialTheme.typography.labelSmall) }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { manager.toggleShuffle() }) { Icon(Icons.Default.Shuffle, "Shuffle", tint = if (shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                        IconButton(onClick = { manager.seekRewind(10000) }) { Icon(Icons.Default.SkipPrevious, "Back 10 seconds", Modifier.size(30.dp)) }
                        IconButton(onClick = { manager.togglePlayPause() }, Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)) { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Play", Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onPrimary) }
                        IconButton(onClick = { manager.seekForward(10000) }) { Icon(Icons.Default.SkipNext, "Forward 10 seconds", Modifier.size(30.dp)) }
                        IconButton(onClick = { manager.toggleRepeatMode() }) { Icon(if (repeat == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat, "Repeat", tint = if (repeat != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(.75f, 1f, 1.25f, 1.5f, 2f).forEach { value -> FilterChip(selected = speed == value, onClick = { manager.setSpeed(value) }, label = { Text("${value}x", fontSize = 11.sp) }, modifier = Modifier.weight(1f)) }
                    }
                }
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f))) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("A–B loop", fontWeight = FontWeight.Bold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(pointA != null, { manager.setLoopPointA() }, label = { Text(if (pointA == null) "Set A" else "A ${formatDuration(pointA ?: 0)}") }, modifier = Modifier.weight(1f))
                                FilterChip(pointB != null, { manager.setLoopPointB() }, label = { Text(if (pointB == null) "Set B" else "B ${formatDuration(pointB ?: 0)}") }, modifier = Modifier.weight(1f))
                                IconButton(onClick = { manager.clearAbLoop() }) { Icon(Icons.Default.Close, "Clear loop") }
                            }
                        }
                    }
                }
            } else if (tab == 1) {
                items(queue, key = { it.id }) { q ->
                    Card(onClick = { manager.playMedia(q, queue, false) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (q.id == item.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f))) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (q.id == item.id) Icons.Default.GraphicEq else Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) { Text(q.title, fontWeight = if (q.id == item.id) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(q.uploader, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        }
                    }
                }
            } else {
                item {
                    val file = File(item.targetPath)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow("Container", if (item.targetPath.endsWith(".mp3")) "MP3" else "Media file")
                        DetailRow("Duration", formatDuration(duration))
                        DetailRow("Playback", "${speed}x")
                        DetailRow("File size", if (file.exists()) "%.2f MB".format(file.length() / 1048576f) else "Unavailable")
                        DetailRow("Location", item.targetPath.ifBlank { "Phone storage" })
                    }
                }
            }
        }
        if (equalizer) EqualizerDialog(onDismiss = { equalizer = false })
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(value, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.2f))
        }
    }
}

private fun formatDuration(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
