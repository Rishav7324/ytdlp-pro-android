package com.ytdlp.app.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.util.Rational
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.ytdlp.app.player.MediaPlayerManager
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(onClose: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val audio = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val manager = MediaPlayerManager.getInstance(context)
    val media by manager.currentMedia.collectAsState()
    val playing by manager.isPlaying.collectAsState()
    val position by manager.currentPosition.collectAsState()
    val duration by manager.duration.collectAsState()
    val speed by manager.playbackSpeed.collectAsState()
    val loopA by manager.loopPointA.collectAsState()
    val loopB by manager.loopPointB.collectAsState()

    var controls by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }
    var landscape by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var more by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(.5f) }
    var brightness by remember { mutableFloatStateOf(.5f) }
    var hud by remember { mutableStateOf<String?>(null) }

    val item = media ?: return

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.window?.let { WindowCompat.getInsetsController(it, it.decorView).show(WindowInsetsCompat.Type.systemBars()) }
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(controls, playing, locked) {
        if (controls && playing && !locked) { delay(3500); controls = false }
    }
    LaunchedEffect(hud) { if (hud != null) { delay(900); hud = null } }

    fun toggleLandscape() {
        landscape = !landscape
        activity?.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (landscape) { controller.hide(WindowInsetsCompat.Type.systemBars()); controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE }
            else controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Surface(Modifier.fillMaxSize(), color = Color.Black) {
        Box(
            Modifier.fillMaxSize()
                .pointerInput(locked) {
                    detectTapGestures(
                        onDoubleTap = { offset -> if (!locked) { if (offset.x < size.width / 2) { manager.seekRewind(10000); hud = "−10 sec" } else { manager.seekForward(10000); hud = "+10 sec" } } },
                        onTap = { controls = !controls }
                    )
                }
                .pointerInput(locked) {
                    detectVerticalDragGestures { change, drag ->
                        if (!locked) {
                            if (change.position.x < size.width / 2) {
                                brightness = (brightness - drag / 500f).coerceIn(.05f, 1f)
                                activity?.window?.attributes = activity?.window?.attributes?.apply { screenBrightness = brightness }
                                hud = "Brightness ${(brightness * 100).toInt()}%"
                            } else {
                                val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                                val current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val next = (current + if (drag < 0) 1 else -1).coerceIn(0, max)
                                audio.setStreamVolume(AudioManager.STREAM_MUSIC, next, 0)
                                volume = next.toFloat() / max
                                hud = "Volume ${(volume * 100).toInt()}%"
                            }
                        }
                    }
                }
        ) {
            AndroidView(
                factory = { ctx -> PlayerView(ctx).apply { player = manager.player; useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT; layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) } },
                update = { it.resizeMode = resizeMode },
                modifier = Modifier.fillMaxSize()
            )

            if (hud != null) HudBubble(hud ?: "")

            if (locked) {
                AnimatedVisibility(controls, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp)) {
                    IconButton(onClick = { locked = false; controls = true }, Modifier.size(52.dp).clip(CircleShape).background(Color.Black.copy(alpha = .65f))) { Icon(Icons.Default.Lock, "Unlock", tint = Color.White) }
                }
            } else {
                AnimatedVisibility(controls, enter = fadeIn(tween(180)), exit = fadeOut(tween(150)), modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxWidth().height(150.dp).align(Alignment.TopCenter).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .82f), Color.Transparent))))
                        Box(Modifier.fillMaxWidth().height(190.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .92f)))))

                        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onClose() }) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                            Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                                Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(item.uploader.ifBlank { "NovaFetch Video" }, color = Color.LightGray, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { manager.setVideoExpanded(false); manager.setAudioSheetOpen(true) }) { Icon(Icons.Default.Headphones, "Background audio", tint = Color.White) }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) IconButton(onClick = { runCatching { activity?.enterPictureInPictureMode(PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()) } }) { Icon(Icons.Default.PictureInPicture, "Picture in picture", tint = Color.White) }
                            IconButton(onClick = { more = true }) { Icon(Icons.Default.MoreVert, "More", tint = Color.White) }
                            DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                                DropdownMenuItem(text = { Text("Speed ${speed}x") }, leadingIcon = { Icon(Icons.Default.Speed, null) }, onClick = { manager.setSpeed(if (speed >= 2f) .75f else speed + .25f); more = false })
                                DropdownMenuItem(text = { Text("Fit / Fill / Zoom") }, leadingIcon = { Icon(Icons.Default.AspectRatio, null) }, onClick = { resizeMode = when (resizeMode) { AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FILL; AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM; else -> AspectRatioFrameLayout.RESIZE_MODE_FIT }; more = false })
                                DropdownMenuItem(text = { Text(if (landscape) "Exit landscape" else "Landscape") }, leadingIcon = { Icon(Icons.Default.ScreenRotation, null) }, onClick = { toggleLandscape(); more = false })
                                DropdownMenuItem(text = { Text("Set loop A") }, onClick = { manager.setLoopPointA(); more = false })
                                DropdownMenuItem(text = { Text("Set loop B") }, onClick = { manager.setLoopPointB(); more = false })
                                DropdownMenuItem(text = { Text(if (loopA != null || loopB != null) "Clear A–B loop" else "A–B loop ready") }, onClick = { manager.clearAbLoop(); more = false })
                                DropdownMenuItem(text = { Text("Lock controls") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, onClick = { locked = true; controls = false; more = false })
                            }
                        }

                        Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 14.dp, vertical = 14.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(formatDuration(position), color = Color.White, style = MaterialTheme.typography.labelSmall)
                                Spacer(Modifier.width(8.dp))
                                Slider(value = position.coerceIn(0L, duration.coerceAtLeast(1L)).toFloat(), onValueChange = { manager.seekTo(it.toLong()) }, valueRange = 0f..duration.coerceAtLeast(1L).toFloat(), modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(8.dp))
                                Text(formatDuration(duration), color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { manager.seekRewind(10000); hud = "−10 sec" }) { Icon(Icons.Default.FastRewind, "Back 10 seconds", tint = Color.White) }
                                IconButton(onClick = { manager.togglePlayPause() }, Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)) { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Play", Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onPrimary) }
                                IconButton(onClick = { manager.seekForward(10000); hud = "+10 sec" }) { Icon(Icons.Default.FastForward, "Forward 10 seconds", tint = Color.White) }
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { resizeMode = when (resizeMode) { AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM; else -> AspectRatioFrameLayout.RESIZE_MODE_FIT } }) { Icon(if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) Icons.Default.Fullscreen else Icons.Default.FullscreenExit, "Resize", tint = Color.White) }
                                Text("${speed}x", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                                items(listOf(.75f, 1f, 1.25f, 1.5f, 2f)) { value -> FilterChip(selected = speed == value, onClick = { manager.setSpeed(value) }, label = { Text("${value}x", fontSize = 11.sp) }) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HudBubble(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(Modifier.clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(alpha = .72f)).padding(horizontal = 22.dp, vertical = 14.dp)) {
            Text(text, color = Color.White, fontWeight = FontWeight.ExtraBold)
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
