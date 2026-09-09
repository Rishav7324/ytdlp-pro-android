package com.ytdlp.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ytdlp.app.ui.theme.NovaAqua
import com.ytdlp.app.ui.theme.NovaAquaSoft
import com.ytdlp.app.ui.theme.NovaInk
import com.ytdlp.app.viewmodel.SettingsViewModel
import com.ytdlp.app.viewmodel.UpdateState

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val engineVersion by viewModel.engineVersion.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val customArguments by viewModel.customArguments.collectAsState()
    val embedSubtitles by viewModel.embedSubtitles.collectAsState()
    val useAria2 by viewModel.useAria2.collectAsState()

    var aria2Connections by remember { mutableFloatStateOf(8f) }
    var sponsorBlockEnabled by remember { mutableStateOf(true) }
    var customArgsInput by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }
    if (!initialized) {
        customArgsInput = customArguments
        initialized = true
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateState.Success -> Toast.makeText(context, "yt-dlp: ${state.version}", Toast.LENGTH_LONG).show()
            is UpdateState.Error -> Toast.makeText(context, "yt-dlp update failed: ${state.message}", Toast.LENGTH_LONG).show()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Tune NovaFetch for downloads, playback and engine performance.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SettingsCard {
            SettingHeader(Icons.Default.CloudDownload, "yt-dlp Core Engine", engineVersion)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { viewModel.updateYtDlp() },
                enabled = updateState !is UpdateState.Checking,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (updateState is UpdateState.Checking) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Checking engine…")
                } else {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Check for yt-dlp update")
                }
            }
        }

        SettingsCard {
            SettingHeader(Icons.Default.Bolt, "Aria2 Accelerator", "Faster parallel segment downloads")
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (useAria2) "Enabled" else "Disabled", fontWeight = FontWeight.Bold, color = if (useAria2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                Switch(checked = useAria2, onCheckedChange = viewModel::setUseAria2)
            }
            Spacer(Modifier.height(6.dp))
            Text("Parallel connections: ${aria2Connections.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Slider(value = aria2Connections, onValueChange = { aria2Connections = it }, valueRange = 1f..16f, steps = 14, enabled = useAria2, modifier = Modifier.fillMaxWidth())
        }

        SettingsCard {
            SettingHeader(Icons.Default.Block, "Smart playback", "Optional quality-of-life helpers")
            SettingSwitch("SponsorBlock", "Skip sponsored segments when metadata is available", sponsorBlockEnabled) { sponsorBlockEnabled = it }
            SettingSwitch("Auto-download subtitles", "Embed multilingual subtitles when available", embedSubtitles, viewModel::setEmbedSubtitles)
        }

        SettingsCard {
            SettingHeader(Icons.Default.Security, "Custom yt-dlp arguments", "Advanced users only")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = customArgsInput,
                onValueChange = { customArgsInput = it; viewModel.setCustomArguments(it) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("--embed-chapters --write-thumbnail") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant)
            )
        }

        SettingsCard {
            SettingHeader(Icons.Default.Info, "About NovaFetch", "Version 2.0.0")
            Spacer(Modifier.height(8.dp))
            Text("A free, open-source media downloader and player powered by yt-dlp, FFmpeg, Aria2c and Media3.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, NovaAqua.copy(alpha = 0.7f), RoundedCornerShape(24.dp)).clickable { uriHandler.openUri("https://github.com/Rishav7324") },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                BoxAvatar()
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Built by Rishav Raj", fontWeight = FontWeight.ExtraBold, color = NovaInk)
                    Text("GitHub • @Rishav7324", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Open-source Android developer", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Icon(Icons.Default.OpenInNew, contentDescription = "Open GitHub", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun SettingsCard(content: @Composable Column.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}

@Composable
private fun SettingHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(NovaAquaSoft), contentAlignment = Alignment.Center) { Icon(icon, null, tint = NovaInk) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 10.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun BoxAvatar() {
    Box(Modifier.size(54.dp).clip(CircleShape).background(Brush.linearGradient(listOf(NovaAqua, NovaAquaSoft))), contentAlignment = Alignment.Center) {
        Text("RR", fontWeight = FontWeight.ExtraBold, color = NovaInk)
    }
}
