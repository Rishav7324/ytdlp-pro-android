package com.ytdlp.app.ui.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.ytdlp.app.player.MediaPlayerManager
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val activity = context as? Activity

    val playerManager = MediaPlayerManager.getInstance(context)
    val currentMedia by playerManager.currentMedia.collectAsState()
    val isPlaying by playerManager.isPlaying.collectAsState()
    val position by playerManager.currentPosition.collectAsState()
    val duration by playerManager.duration.collectAsState()
    val playbackSpeed by playerManager.playbackSpeed.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    // Gesture Overlay States (Animeko-style)
    var volumeLevel by remember { mutableFloatStateOf(0.5f) }
    var brightnessLevel by remember { mutableFloatStateOf(0.5f) }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    var showBrightnessOverlay by remember { mutableStateOf(false) }

    if (currentMedia == null) return
    val item = currentMedia ?: return

    // Auto-hide controls after 4 seconds
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying && !isLocked) {
            delay(4000)
            showControls = false
        }
    }

    LaunchedEffect(showVolumeOverlay) {
        if (showVolumeOverlay) {
            delay(1500)
            showVolumeOverlay = false
        }
    }

    LaunchedEffect(showBrightnessOverlay) {
        if (showBrightnessOverlay) {
            delay(1500)
            showBrightnessOverlay = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            if (!isLocked) {
                                if (offset.x < size.width / 2) {
                                    playerManager.seekRewind(10000L)
                                } else {
                                    playerManager.seekForward(10000L)
                                }
                            }
                        },
                        onTap = {
                            showControls = !showControls
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        if (!isLocked) {
                            val isLeftSide = change.position.x < size.width / 2
                            if (isLeftSide) {
                                // Brightness Gesture
                                val delta = -dragAmount / 400f
                                brightnessLevel = (brightnessLevel + delta).coerceIn(0.01f, 1.0f)
                                activity?.window?.let { win ->
                                    val lp = win.attributes
                                    lp.screenBrightness = brightnessLevel
                                    win.attributes = lp
                                }
                                showBrightnessOverlay = true
                            } else {
                                // Volume Gesture
                                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val deltaVol = if (dragAmount < 0) 1 else if (dragAmount > 0) -1 else 0
                                val newVol = (currentVol + deltaVol).coerceIn(0, maxVol)
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                volumeLevel = newVol.toFloat() / maxVol.toFloat()
                                showVolumeOverlay = true
                            }
                        }
                    }
                }
        ) {
            // Android ExoPlayer View
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = playerManager.player
                        useController = false
                        this.resizeMode = resizeMode
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { playerView ->
                    playerView.resizeMode = resizeMode
                },
                modifier = Modifier.fillMaxSize()
            )

            // Gesture Overlays (Volume / Brightness)
            if (showVolumeOverlay) {
                Card(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 36.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${(volumeLevel * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showBrightnessOverlay) {
                Card(
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 36.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${(brightnessLevel * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Lock Overlay Icon
            if (isLocked) {
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopStart).padding(20.dp)
                ) {
                    IconButton(
                        onClick = { isLocked = false },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Unlock", tint = Color.White)
                    }
                }
            } else {
                // Controls Overlay
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        // Top Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(onClick = onClose) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.uploader,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Aspect ratio switch
                                IconButton(
                                    onClick = {
                                        resizeMode = when (resizeMode) {
                                            AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = Color.White)
                                }

                                // Playback Speed Menu
                                Box {
                                    IconButton(onClick = { showSpeedMenu = true }) {
                                        Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color.White)
                                    }
                                    DropdownMenu(
                                        expanded = showSpeedMenu,
                                        onDismissRequest = { showSpeedMenu = false }
                                    ) {
                                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { sp ->
                                            DropdownMenuItem(
                                                text = { Text("${sp}x") },
                                                onClick = {
                                                    playerManager.setSpeed(sp)
                                                    showSpeedMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Lock Button
                                IconButton(onClick = { isLocked = true }) {
                                    Icon(Icons.Default.LockOpen, contentDescription = "Lock Screen", tint = Color.White)
                                }
                            }
                        }

                        // Center Controls (Prev, 10s Back, Play/Pause, 10s Forward, Next)
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { playerManager.playPrevious() }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                            }

                            IconButton(
                                onClick = { playerManager.seekRewind(10000L) },
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.FastRewind, contentDescription = "10s Back", tint = Color.White, modifier = Modifier.size(32.dp))
                            }

                            IconButton(
                                onClick = { playerManager.togglePlayPause() },
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            IconButton(
                                onClick = { playerManager.seekForward(10000L) },
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.FastForward, contentDescription = "10s Forward", tint = Color.White, modifier = Modifier.size(32.dp))
                            }

                            IconButton(onClick = { playerManager.playNext() }) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                            }
                        }

                        // Bottom Timeline Bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            val currentPosFloat = position.toFloat().coerceIn(0f, duration.toFloat().coerceAtLeast(1f))
                            Slider(
                                value = currentPosFloat,
                                onValueChange = { playerManager.seekTo(it.toLong()) },
                                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(formatDuration(position), color = Color.White, style = MaterialTheme.typography.labelSmall)
                                Text("${playbackSpeed}x", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(formatDuration(duration), color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
