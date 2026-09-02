package com.ytdlp.app.ui.screens

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ytdlp.app.YtDlpApp
import com.ytdlp.app.data.local.DownloadEntity
import com.ytdlp.app.data.local.MediaType
import com.ytdlp.app.player.MediaPlayerManager
import com.ytdlp.app.ui.components.DownloadItemCard
import com.ytdlp.app.ui.components.FormatSelectionSheet
import com.ytdlp.app.ui.components.VideoPreviewCard
import com.ytdlp.app.ui.components.batch.BatchDownloadModal
import com.ytdlp.app.ui.components.equalizer.EqualizerDialog
import com.ytdlp.app.ui.theme.AccentGreen
import com.ytdlp.app.ui.theme.AccentOrange
import com.ytdlp.app.ui.theme.AccentRed
import com.ytdlp.app.ui.theme.BilibiliBlue
import com.ytdlp.app.ui.theme.InstagramPink
import com.ytdlp.app.ui.theme.PrimaryIndigo
import com.ytdlp.app.ui.theme.RedditOrange
import com.ytdlp.app.ui.theme.SoundCloudOrange
import com.ytdlp.app.ui.theme.TikTokCyan
import com.ytdlp.app.ui.theme.TwitchPurple
import com.ytdlp.app.ui.theme.TwitterBlue
import com.ytdlp.app.ui.theme.YouTubeRed
import com.ytdlp.app.viewmodel.HomeUiState
import com.ytdlp.app.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToQueue: () -> Unit = {},
    onNavigateToBrowser: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val playerManager = remember { MediaPlayerManager.getInstance(context) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()
    val recentDownloads by viewModel.recentDownloads.collectAsState(initial = emptyList())
    val isEngineReady by YtDlpApp.instance.isEngineReady.collectAsState()
    val initError by YtDlpApp.instance.initError.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showBatchModal by remember { mutableStateOf(false) }
    var showEqualizer by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var clipboardDetectedUrl by remember { mutableStateOf<String?>(null) }

    // Clipboard Auto-Detect
    LaunchedEffect(Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
            val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: ""
            if ((text.startsWith("http://") || text.startsWith("https://") || text.contains("youtu") || text.contains("instagram") || text.contains("tiktok") || text.contains("twitter") || text.contains("reddit")) && text != urlInput) {
                clipboardDetectedUrl = text
            }
        }
    }

    val platforms = listOf(
        Triple("YouTube", YouTubeRed, "https://m.youtube.com"),
        Triple("Instagram", InstagramPink, "https://www.instagram.com"),
        Triple("TikTok", TikTokCyan, "https://www.tiktok.com"),
        Triple("X / Twitter", TwitterBlue, "https://x.com"),
        Triple("SoundCloud", SoundCloudOrange, "https://m.soundcloud.com"),
        Triple("Twitch", TwitchPurple, "https://m.twitch.tv"),
        Triple("Reddit", RedditOrange, "https://www.reddit.com")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Audiofy & OnePlayer Studio Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))),
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(26.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "yt-dlp Pro Studio",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Audiofy & OnePlayer Pro Engine",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { showEqualizer = true }) {
                                Icon(Icons.Default.Tune, contentDescription = "Audio Studio FX", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { showBatchModal = true }) {
                                Icon(Icons.Default.PlaylistPlay, contentDescription = "Batch Downloader", tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Downloader Quality Telemetry
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hd, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("True 1080p FHD / 4K UHD", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Equalizer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("320kbps Studio Audio", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Downloader Input Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Download Any Audio / Video", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { viewModel.onUrlChanged(it) },
                        placeholder = { Text("Paste YouTube, Instagram, Reels link...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            if (urlInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        keyboardController?.hide()
                                        viewModel.parseUrl(urlInput)
                                    }
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "Extract & Download", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                keyboardController?.hide()
                                if (urlInput.isNotBlank()) {
                                    viewModel.parseUrl(urlInput)
                                }
                            }
                        )
                    )

                    // Clipboard Detect Floating Chip
                    if (!clipboardDetectedUrl.isNullOrBlank() && urlInput.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboardDetectedUrl?.let {
                                        viewModel.onUrlChanged(it)
                                        viewModel.parseUrl(it)
                                        clipboardDetectedUrl = null
                                    }
                                }
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto-Paste: ${clipboardDetectedUrl?.take(36)}...",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Supported Platforms Horizontal Hub
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                platforms.forEach { (name, color, siteUrl) ->
                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigateToBrowser(siteUrl) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Extraction Status / Result
        item {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Parsing 1080p FHD / 4K UHD Streams...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
                is HomeUiState.Success -> {
                    VideoPreviewCard(
                        videoInfo = state.videoInfo,
                        onConfigureDownload = { showBottomSheet = true }
                    )
                }
                is HomeUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Notice", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
                is HomeUiState.Idle -> {}
            }
        }

        // Recent Downloads Section with Instant Playback
        if (recentDownloads.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Media",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${recentDownloads.size} Items",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(recentDownloads, key = { it.id }) { item ->
                DownloadItemCard(
                    download = item,
                    onCancel = { viewModel.cancelDownload(it) },
                    onDelete = { viewModel.deleteDownload(it) },
                    onPlay = { playerManager.playMedia(item, recentDownloads, openFullscreenIfVideo = (item.mediaType == MediaType.VIDEO)) }
                )
            }
        }
    }

    if (showBottomSheet && uiState is HomeUiState.Success) {
        val info = (uiState as HomeUiState.Success).videoInfo
        FormatSelectionSheet(
            videoInfo = info,
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
            onStartDownload = { formatId, mediaType, audioExt ->
                coroutineScope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                    viewModel.startDownload(info, formatId, mediaType, audioExt, autoStart = true)
                    onNavigateToQueue()
                }
            },
            onQueueDownload = { formatId, mediaType, audioExt ->
                coroutineScope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                    viewModel.startDownload(info, formatId, mediaType, audioExt, autoStart = false)
                }
            }
        )
    }

    if (showBatchModal) {
        BatchDownloadModal(onDismiss = { showBatchModal = false })
    }

    if (showEqualizer) {
        EqualizerDialog(onDismiss = { showEqualizer = false })
    }
}
