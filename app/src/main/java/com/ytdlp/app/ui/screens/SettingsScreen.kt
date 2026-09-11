package com.ytdlp.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ytdlp.app.ui.components.LiquidGlassCard
import com.ytdlp.app.ui.components.LiquidGlassPill
import com.ytdlp.app.ui.components.liquidGlass
import com.ytdlp.app.ui.theme.NovaAqua
import com.ytdlp.app.ui.theme.NovaAquaDeep
import com.ytdlp.app.ui.theme.NovaAquaSoft
import com.ytdlp.app.ui.theme.NovaInk
import com.ytdlp.app.viewmodel.SettingsViewModel
import com.ytdlp.app.viewmodel.UpdateState

@Composable
fun SettingsScreen(
    onOpenLegal: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val engineVersion by viewModel.engineVersion.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val customArguments by viewModel.customArguments.collectAsState()
    val embedSubtitles by viewModel.embedSubtitles.collectAsState()
    val useAria2 by viewModel.useAria2.collectAsState()
    val aria2Connections by viewModel.aria2Connections.collectAsState()
    val sponsorBlockEnabled by viewModel.sponsorBlockEnabled.collectAsState()

    var customArgsInput by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }
    if (!initialized) {
        customArgsInput = customArguments
        initialized = true
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateState.Success -> Toast.makeText(context, "yt-dlp updated to: ${state.version}", Toast.LENGTH_LONG).show()
            is UpdateState.Error -> Toast.makeText(context, "yt-dlp update failed: ${state.message}", Toast.LENGTH_LONG).show()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Ultra iOS configuration, engine acceleration and legal info.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        // Group 1: yt-dlp Engine
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            elevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                SettingHeader(Icons.Rounded.CloudDownload, "yt-dlp Core Engine", "Version: $engineVersion")
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.updateYtDlp() },
                    enabled = updateState !is UpdateState.Checking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (updateState is UpdateState.Checking) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(10.dp))
                        Text("Checking GitHub Releases...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Check for yt-dlp Engine Update", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Group 2: Aria2 Accelerator
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            elevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                SettingHeader(Icons.Rounded.Bolt, "Aria2 Acceleration", "Multi-segment parallel socket downloading")
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (useAria2) "Enabled" else "Disabled",
                        fontWeight = FontWeight.Bold,
                        color = if (useAria2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(checked = useAria2, onCheckedChange = { viewModel.setUseAria2(it) })
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Parallel connection count: $aria2Connections",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = aria2Connections.toFloat(),
                    onValueChange = { viewModel.setAria2Connections(it.toInt()) },
                    valueRange = 1f..16f,
                    steps = 14,
                    enabled = useAria2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Group 3: Smart Processing
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            elevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                SettingHeader(Icons.Rounded.Block, "Smart Post-Processing", "Automated sponsor skip and subtitles")
                Spacer(Modifier.height(12.dp))
                SettingSwitch(
                    title = "SponsorBlock Removal",
                    subtitle = "Skip sponsored segments automatically via metadata",
                    checked = sponsorBlockEnabled,
                    onCheckedChange = { viewModel.setSponsorBlockEnabled(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
                SettingSwitch(
                    title = "Auto-Embed Subtitles",
                    subtitle = "Embed multilingual subtitle tracks if available",
                    checked = embedSubtitles,
                    onCheckedChange = { viewModel.setEmbedSubtitles(it) }
                )
            }
        }

        // Group 4: Custom Arguments
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            elevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                SettingHeader(Icons.Rounded.Code, "Custom yt-dlp Flags", "Advanced parameter overrides")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = customArgsInput,
                    onValueChange = {
                        customArgsInput = it
                        viewModel.setCustomArguments(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5,
                    shape = RoundedCornerShape(18.dp),
                    placeholder = { Text("--embed-chapters --write-thumbnail") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
            }
        }

        // Group 5: Legal & Open Source
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            elevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                SettingHeader(Icons.Rounded.Description, "Legal & Compliance", "Licenses, DMCA, disclaimer and terms")
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onOpenLegal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Rounded.Policy, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Legal & Open Source Notices", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Group 6: Author / GitHub Card
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            elevation = 6.dp,
            onClick = { uriHandler.openUri("https://github.com/Rishav7324/ytdlp-pro-android") }
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NovaAqua, NovaAquaDeep))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("RR", fontWeight = FontWeight.Black, color = NovaInk, fontSize = 16.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Maintained by Rishav Raj", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                    Text("GitHub • @Rishav7324", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text("NovaFetch Ultra iOS Edition", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Rounded.OpenInNew, contentDescription = "Open GitHub", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(110.dp))
    }
}

@Composable
private fun SettingHeader(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .liquidGlass(shape = RoundedCornerShape(14.dp), elevation = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
