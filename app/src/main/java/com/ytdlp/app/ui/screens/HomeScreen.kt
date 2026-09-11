package com.ytdlp.app.ui.screens

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ytdlp.app.R
import com.ytdlp.app.YtDlpApp
import com.ytdlp.app.data.local.MediaType
import com.ytdlp.app.player.MediaPlayerManager
import com.ytdlp.app.ui.components.DownloadItemCard
import com.ytdlp.app.ui.components.FormatSelectionSheet
import com.ytdlp.app.ui.components.LiquidGlassCard
import com.ytdlp.app.ui.components.LiquidGlassPill
import com.ytdlp.app.ui.components.VideoPreviewCard
import com.ytdlp.app.ui.components.batch.BatchDownloadModal
import com.ytdlp.app.ui.components.equalizer.EqualizerDialog
import com.ytdlp.app.ui.components.liquidGlass
import com.ytdlp.app.ui.theme.AccentOrange
import com.ytdlp.app.ui.theme.InstagramPink
import com.ytdlp.app.ui.theme.NovaAqua
import com.ytdlp.app.ui.theme.NovaAquaDeep
import com.ytdlp.app.ui.theme.NovaAquaSoft
import com.ytdlp.app.ui.theme.NovaInk
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showBatchModal by remember { mutableStateOf(false) }
    var showEqualizer by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var clipboardDetectedUrl by remember { mutableStateOf<String?>(null) }

    // Safe Clipboard Auto-Detect (protected from SecurityException on Android 10-15)
    LaunchedEffect(Unit) {
        runCatching {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: ""
                if ((text.startsWith("http://") || text.startsWith("https://") || text.contains("youtu") || text.contains("instagram") || text.contains("tiktok") || text.contains("twitter") || text.contains("reddit")) && text != urlInput) {
                    clipboardDetectedUrl = text
                }
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
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
    ) {
        // Ultra iOS Liquid Glass Header Card with Official App Icon
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = 14.dp
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
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .shadow(6.dp, RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "NovaFetch Logo",
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "NovaFetch",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(NovaAqua, NovaAquaDeep)
                                                )
                                            )
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "PRO",
                                            color = NovaInk,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Text(
                                    text = "Ultra Liquid Glass Engine",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { showEqualizer = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .liquidGlass(shape = CircleShape, elevation = 4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Tune,
                                    contentDescription = "Audio Studio FX",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (uiState is HomeUiState.Success) {
                                        showBatchModal = true
                                    } else if (urlInput.isNotBlank()) {
                                        viewModel.parseUrl(urlInput)
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .liquidGlass(shape = CircleShape, elevation = 4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.PlaylistPlay,
                                    contentDescription = "Batch Downloader",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quality Telemetry Glass Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Hd, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("True 1080p / 4K Stream Merge", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Equalizer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("320kbps Studio Audio", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Downloader Input Glass Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                elevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Download Any Video or Audio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { viewModel.onUrlChanged(it) },
                        placeholder = { Text("Paste YouTube, Instagram, TikTok link...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        trailingIcon = {
                            if (urlInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        keyboardController?.hide()
                                        viewModel.parseUrl(urlInput)
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.Download,
                                        contentDescription = "Extract & Download",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
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

                    // Clipboard Detect Floating Glass Chip
                    if (!clipboardDetectedUrl.isNullOrBlank() && urlInput.isBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LiquidGlassPill(
                            onClick = {
                                clipboardDetectedUrl?.let {
                                    viewModel.onUrlChanged(it)
                                    viewModel.parseUrl(it)
                                    clipboardDetectedUrl = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Rounded.ContentPaste,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
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

        // Supported Platforms Horizontal Hub
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                platforms.forEach { (name, color, siteUrl) ->
                    LiquidGlassCard(
                        shape = RoundedCornerShape(18.dp),
                        elevation = 4.dp,
                        onClick = { onNavigateToBrowser(siteUrl) }
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
                                    .shadow(4.dp, CircleShape, ambientColor = color)
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
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Analyzing stream with yt-dlp...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
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
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        tintColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                HomeUiState.Idle -> { }
            }
        }

        // Recent Downloads Section Header
        if (recentDownloads.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToQueue) {
                        Text("View Queue", fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(recentDownloads.take(5), key = { it.id }) { item ->
                DownloadItemCard(
                    download = item,
                    onPause = { },
                    onResume = { },
                    onCancel = { },
                    onPlay = { playerManager.playMedia(item) },
                    onDelete = { }
                )
            }
        }
    }

    // Format Selection Modal Bottom Sheet
    if (showBottomSheet && uiState is HomeUiState.Success) {
        val videoInfo = (uiState as HomeUiState.Success).videoInfo
        FormatSelectionSheet(
            sheetState = sheetState,
            videoInfo = videoInfo,
            onDismiss = { showBottomSheet = false },
            onStartDownload = { formatId, mediaType, audioExt ->
                viewModel.startDownload(videoInfo, formatId, mediaType, audioExt)
                showBottomSheet = false
                onNavigateToQueue()
            }
        )
    }

    // Batch Download Modal
    if (showBatchModal && uiState is HomeUiState.Success) {
        val info = (uiState as HomeUiState.Success).videoInfo
        BatchDownloadModal(
            itemsList = listOf(info),
            onDismiss = { showBatchModal = false },
            onBatchDownload = { selectedItems, formatId, isAudio ->
                selectedItems.forEach { item ->
                    viewModel.startDownload(
                        videoInfo = item,
                        formatId = formatId,
                        mediaType = if (isAudio) MediaType.AUDIO else MediaType.VIDEO,
                        audioExt = "mp3"
                    )
                }
                showBatchModal = false
                onNavigateToQueue()
            }
        )
    }

    // Audio Studio Equalizer Dialog
    if (showEqualizer) {
        EqualizerDialog(onDismiss = { showEqualizer = false })
    }
}
