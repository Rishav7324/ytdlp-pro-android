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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ytdlp.app.YtDlpApp
import com.ytdlp.app.data.local.MediaType
import com.ytdlp.app.player.MediaPlayerManager
import com.ytdlp.app.ui.components.DownloadItemCard
import com.ytdlp.app.ui.components.FormatSelectionSheet
import com.ytdlp.app.ui.components.VideoPreviewCard
import com.ytdlp.app.ui.theme.AccentCyan
import com.ytdlp.app.ui.theme.AccentGreen
import com.ytdlp.app.ui.theme.AccentOrange
import com.ytdlp.app.ui.theme.AccentPink
import com.ytdlp.app.ui.theme.AccentRed
import com.ytdlp.app.ui.theme.BilibiliBlue
import com.ytdlp.app.ui.theme.InstagramPink
import com.ytdlp.app.ui.theme.PrimaryIndigo
import com.ytdlp.app.ui.theme.RedditOrange
import com.ytdlp.app.ui.theme.SecondaryTeal
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
    val playerManager = MediaPlayerManager.getInstance(context)
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()
    val recentDownloads by viewModel.recentDownloads.collectAsState(initial = emptyList())
    val isEngineReady by YtDlpApp.instance.isEngineReady.collectAsState()
    val initError by YtDlpApp.instance.initError.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
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
        Triple("Reddit", RedditOrange, "https://www.reddit.com"),
        Triple("SoundCloud", SoundCloudOrange, "https://m.soundcloud.com"),
        Triple("Twitch", TwitchPurple, "https://m.twitch.tv"),
        Triple("Bilibili", BilibiliBlue, "https://m.bilibili.com")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Glassmorphic Hero Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    PrimaryIndigo.copy(alpha = 0.2f),
                                    SecondaryTeal.copy(alpha = 0.15f),
                                    AccentPink.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(PrimaryIndigo, SecondaryTeal)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "yt-dlp Pro",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "4K HDR & Studio Audio Suite",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (isEngineReady) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(AccentGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Engine Ready",
                                        color = AccentGreen,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (initError != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(AccentRed.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = AccentRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Init Issue",
                                        color = AccentRed,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(PrimaryIndigo.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = PrimaryIndigo
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Initializing",
                                        color = PrimaryIndigo,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Telemetry Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Downloads", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("${recentDownloads.size} Items", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                            Column {
                                Text("Turbo Engine", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("Aria2 + Native", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                            }
                            Column {
                                Text("Max Resolution", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("4K UHD / 60fps", fontWeight = FontWeight.Bold, color = AccentOrange, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }

            // Clipboard Auto-Detect Notification
            AnimatedVisibility(
                visible = clipboardDetectedUrl != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            clipboardDetectedUrl?.let {
                                viewModel.onUrlChanged(it)
                                viewModel.parseUrl(it)
                                clipboardDetectedUrl = null
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Link Found in Clipboard", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                Text("Tap to analyze & download instantly", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Button(
                            onClick = {
                                clipboardDetectedUrl?.let {
                                    viewModel.onUrlChanged(it)
                                    viewModel.parseUrl(it)
                                    clipboardDetectedUrl = null
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Paste & Go", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // URL Search Box with Glassy Border
            OutlinedTextField(
                value = urlInput,
                onValueChange = { viewModel.onUrlChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("Paste video, playlist, or music link...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (urlInput.isNotBlank()) {
                            IconButton(onClick = { viewModel.onUrlChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                                val item = clipboard.primaryClip?.getItemAt(0)
                                val text = item?.text?.toString() ?: ""
                                if (text.isNotBlank()) {
                                    viewModel.onUrlChanged(text)
                                    viewModel.parseUrl(text)
                                }
                            }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    viewModel.parseUrl()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons (Analyze & In-App Browser)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.parseUrl()
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Extract Media", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        onNavigateToBrowser(urlInput.ifBlank { "https://m.youtube.com" })
                    },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Browser")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Platform Hub Grid Cards
            Text("Supported Platforms", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

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

        // Status or Result Card
        item {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Connecting to yt-dlp extractor...", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Parsing streams, formats, 4K/1080p qualities, and metadata", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Extraction Notice", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
                is HomeUiState.Idle -> {
                    // Idle state
                }
            }
        }

        // Recent Activity Section
        if (recentDownloads.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(recentDownloads.take(5), key = { it.id }) { item ->
                DownloadItemCard(
                    download = item,
                    onCancel = { viewModel.cancelDownload(it) },
                    onDelete = { viewModel.deleteDownload(it) },
                    onPlay = { playerManager.playMedia(it) }
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
}
