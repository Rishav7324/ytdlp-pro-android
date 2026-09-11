package com.ytdlp.app.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.ytdlp.app.player.MediaPlayerManager
import com.ytdlp.app.ui.components.equalizer.EqualizerDialog
import com.ytdlp.app.ui.theme.NovaAqua
import com.ytdlp.app.ui.theme.NovaAquaDeep
import com.ytdlp.app.ui.theme.NovaInk
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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

    var selectedTab by remember { mutableIntStateOf(0) }
    var showEqualizer by remember { mutableStateOf(false) }
    var sleepTimerMinutes by remember { mutableIntStateOf(0) }
    var sleepTimerJob by remember { mutableStateOf<Job?>(null) }
    var sleepTimerActive by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val item = media ?: return
    val artScale by animateFloatAsState(if (playing) 1.0f else 0.94f, tween(300), label = "art-scale")

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        sleepTimerMinutes = minutes
        if (minutes > 0) {
            sleepTimerActive = true
            sleepTimerJob = scope.launch {
                delay(minutes * 60 * 1000L)
                manager.pause()
                sleepTimerActive = false
                sleepTimerMinutes = 0
            }
        } else {
            sleepTimerActive = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Drag Handle & Top Action Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(4.5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.KeyboardArrowDown, "Collapse Sheet", Modifier.size(30.dp))
                        }
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NovaAqua,
                            letterSpacing = 1.sp
                        )
                        IconButton(onClick = { showEqualizer = true }) {
                            Icon(Icons.Default.Tune, "Equalizer", tint = NovaAqua)
                        }
                    }
                }
            }

            // Tabs: Player / Queue / Audio Info
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.clip(RoundedCornerShape(18.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Player", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Queue (${queue.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Details", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Tab 0: Main Audio Player View
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        // Liquid Glass Album Art Frame
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .scale(artScale)
                                .shadow(20.dp, RoundedCornerShape(32.dp), ambientColor = NovaAqua)
                                .clip(RoundedCornerShape(32.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(NovaAqua.copy(alpha = 0.2f), Color.White.copy(alpha = 0.05f))
                                    )
                                )
                                .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(32.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.thumbnailUrl.isNotBlank()) {
                                AsyncImage(
                                    model = item.thumbnailUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(76.dp),
                                        tint = NovaAqua
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.uploader.ifBlank { "NovaFetch Audio" },
                            color = NovaAqua,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Timeline Scrubber
                item {
                    val safeDur = duration.coerceAtLeast(1L)
                    Slider(
                        value = position.coerceIn(0L, safeDur).toFloat(),
                        onValueChange = { manager.seekTo(it.toLong()) },
                        valueRange = 0f..safeDur.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = NovaAqua,
                            activeTrackColor = NovaAqua,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(position),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatDuration(duration),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Playback Control Buttons Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { manager.toggleShuffle() }) {
                            Icon(
                                Icons.Default.Shuffle,
                                "Shuffle",
                                tint = if (shuffle) NovaAqua else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { manager.seekRewind(10000) }) {
                            Icon(Icons.Default.Replay10, "Rewind 10 seconds", Modifier.size(32.dp))
                        }

                        // Giant Play/Pause with Spring Physics
                        IconButton(
                            onClick = { manager.togglePlayPause() },
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(NovaAqua)
                                .shadow(12.dp, CircleShape, ambientColor = NovaAqua)
                        ) {
                            Icon(
                                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                modifier = Modifier.size(40.dp),
                                tint = NovaInk
                            )
                        }

                        IconButton(onClick = { manager.seekForward(10000) }) {
                            Icon(Icons.Default.Forward10, "Forward 10 seconds", Modifier.size(32.dp))
                        }

                        IconButton(onClick = { manager.toggleRepeatMode() }) {
                            Icon(
                                imageVector = if (repeat == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Repeat",
                                tint = if (repeat != Player.REPEAT_MODE_OFF) NovaAqua else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Playback Speed Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)) { itemSpeed ->
                            FilterChip(
                                selected = speed == itemSpeed,
                                onClick = { manager.setSpeed(itemSpeed) },
                                label = { Text("${itemSpeed}x", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NovaAqua,
                                    selectedLabelColor = NovaInk
                                )
                            )
                        }
                    }
                }

                // Sleep Timer Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = NovaAqua, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sleep Timer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                if (sleepTimerActive) {
                                    Text("${sleepTimerMinutes}m Active", color = NovaAqua, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                items(listOf(0, 15, 30, 45, 60)) { mins ->
                                    FilterChip(
                                        selected = sleepTimerMinutes == mins && (mins == 0 || sleepTimerActive),
                                        onClick = { startSleepTimer(mins) },
                                        label = { Text(if (mins == 0) "Off" else "${mins}m", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NovaAqua,
                                            selectedLabelColor = NovaInk
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // A-B Loop Controls
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("A–B Loop Section", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = pointA != null,
                                    onClick = { manager.setLoopPointA() },
                                    label = { Text(if (pointA == null) "Set Loop A" else "A: ${formatDuration(pointA ?: 0)}") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = pointB != null,
                                    onClick = { manager.setLoopPointB() },
                                    label = { Text(if (pointB == null) "Set Loop B" else "B: ${formatDuration(pointB ?: 0)}") },
                                    modifier = Modifier.weight(1f)
                                )
                                if (pointA != null || pointB != null) {
                                    IconButton(onClick = { manager.clearAbLoop() }) {
                                        Icon(Icons.Default.Close, "Clear loop", tint = Color.Red.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // Tab 1: Queue Management
                if (queue.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.MusicNote, null, Modifier.size(54.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            Text("Queue is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(queue, key = { it.id }) { qItem ->
                        Card(
                            onClick = { manager.playMedia(qItem, queue, false) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (qItem.id == item.id) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (qItem.id == item.id) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (qItem.id == item.id) NovaAqua else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = qItem.title,
                                        fontWeight = if (qItem.id == item.id) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = qItem.uploader,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 2: Technical Media Details
                item {
                    val file = File(item.targetPath)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetailGlassRow("Title", item.title)
                        DetailGlassRow("Artist / Uploader", item.uploader.ifBlank { "Unknown" })
                        DetailGlassRow("Duration", formatDuration(duration))
                        DetailGlassRow("Playback Speed", "${speed}x")
                        DetailGlassRow("Format / Container", if (item.targetPath.endsWith(".mp3")) "Audio / MP3" else "Media File")
                        DetailGlassRow(
                            "File Size",
                            if (file.exists()) "%.2f MB".format(file.length() / 1048576f) else "Streaming / Temporary"
                        )
                        DetailGlassRow("File Path", item.targetPath.ifBlank { "App Storage" })
                    }
                }
            }
        }

        if (showEqualizer) {
            EqualizerDialog(onDismiss = { showEqualizer = false })
        }
    }
}

@Composable
private fun DetailGlassRow(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp
            )
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.3f),
                fontSize = 13.sp
            )
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
