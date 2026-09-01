package com.ytdlp.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ytdlp.app.ui.theme.AccentGreen
import com.ytdlp.app.viewmodel.SettingsViewModel
import com.ytdlp.app.viewmodel.UpdateState

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val downloadPath by viewModel.downloadPath.collectAsState()
    val embedThumbnail by viewModel.embedThumbnail.collectAsState()
    val embedSubtitles by viewModel.embedSubtitles.collectAsState()
    val useAria2 by viewModel.useAria2.collectAsState()
    val customArgs by viewModel.customArguments.collectAsState()
    val cookiesContent by viewModel.cookiesContent.collectAsState()
    val engineVersion by viewModel.engineVersion.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Settings & Engine",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure yt-dlp core, Aria2c accelerator, and network bypass",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // yt-dlp Engine Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "yt-dlp Core Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Version: $engineVersion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { viewModel.updateYtDlp() },
                            enabled = updateState !is UpdateState.Checking,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (updateState is UpdateState.Checking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(4.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.padding(2.dp))
                                Text("Check Update")
                            }
                        }
                    }

                    if (updateState is UpdateState.Success) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Updated successfully to ${(updateState as UpdateState.Success).version}!",
                            color = AccentGreen,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (updateState is UpdateState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (updateState as UpdateState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Download & Engine Optimization
        item {
            Text(
                text = "Engine & Acceleration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Aria2c Multi-Stream Engine", fontWeight = FontWeight.SemiBold)
                            Text("Accelerate downloads with 8 parallel connections", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Switch(checked = useAria2, onCheckedChange = { viewModel.setUseAria2(it) })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Embed Thumbnail Art", fontWeight = FontWeight.SemiBold)
                            Text("Attach artwork cover to MP4/MP3 files", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Switch(checked = embedThumbnail, onCheckedChange = { viewModel.setEmbedThumbnail(it) })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Embed Subtitle Tracks", fontWeight = FontWeight.SemiBold)
                            Text("Auto-embed all available subtitle tracks into video", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Switch(checked = embedSubtitles, onCheckedChange = { viewModel.setEmbedSubtitles(it) })
                    }
                }
            }
        }

        // Advanced yt-dlp CLI Options & Cookies
        item {
            Text(
                text = "Bypass & Authentication",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = customArgs,
                        onValueChange = { viewModel.setCustomArguments(it) },
                        label = { Text("Custom yt-dlp Arguments") },
                        placeholder = { Text("--geo-bypass --user-agent 'Mozilla/5.0'") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = cookiesContent,
                        onValueChange = { viewModel.setCookiesContent(it) },
                        label = { Text("Cookies (Netscape format cookies.txt)") },
                        placeholder = { Text("# Paste Netscape cookies here for private/age-restricted videos...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 5
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
