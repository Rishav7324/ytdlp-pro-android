package com.ytdlp.app.ui.components.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ytdlp.app.player.AudioFxManager

@Composable
fun EqualizerDialog(
    onDismiss: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(true) }
    var bassBoost by remember { mutableFloatStateOf(0.5f) }
    var virtualizer by remember { mutableFloatStateOf(0.3f) }
    var volumeGain by remember { mutableFloatStateOf(0f) }

    val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val bandLevels = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f) }

    val presets = listOf("Flat", "Bass Boost", "Vocal", "Rock", "Pop", "Electronic")
    var selectedPreset by remember { mutableStateOf("Bass Boost") }

    var selectedTimerMinutes by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Audio Studio FX", fontWeight = FontWeight.Bold)
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it }
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Presets
                Text("Sound Profile Presets", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presets) { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = {
                                selectedPreset = preset
                                when (preset) {
                                    "Flat" -> {
                                        bassBoost = 0f
                                        virtualizer = 0f
                                        for (i in bandLevels.indices) bandLevels[i] = 0f
                                    }
                                    "Bass Boost" -> {
                                        bassBoost = 0.8f
                                        virtualizer = 0.3f
                                        bandLevels[0] = 6f
                                        bandLevels[1] = 4f
                                        bandLevels[2] = 0f
                                        bandLevels[3] = 0f
                                        bandLevels[4] = 2f
                                    }
                                    "Vocal" -> {
                                        bassBoost = 0.2f
                                        virtualizer = 0.2f
                                        bandLevels[0] = -2f
                                        bandLevels[1] = 1f
                                        bandLevels[2] = 5f
                                        bandLevels[3] = 4f
                                        bandLevels[4] = 2f
                                    }
                                    "Rock" -> {
                                        bassBoost = 0.6f
                                        virtualizer = 0.4f
                                        bandLevels[0] = 5f
                                        bandLevels[1] = 3f
                                        bandLevels[2] = -1f
                                        bandLevels[3] = 3f
                                        bandLevels[4] = 5f
                                    }
                                    "Pop" -> {
                                        bassBoost = 0.4f
                                        virtualizer = 0.3f
                                        bandLevels[0] = -1f
                                        bandLevels[1] = 2f
                                        bandLevels[2] = 5f
                                        bandLevels[3] = 2f
                                        bandLevels[4] = -1f
                                    }
                                    "Electronic" -> {
                                        bassBoost = 0.9f
                                        virtualizer = 0.6f
                                        bandLevels[0] = 6f
                                        bandLevels[1] = 3f
                                        bandLevels[2] = 0f
                                        bandLevels[3] = 2f
                                        bandLevels[4] = 6f
                                    }
                                }
                                AudioFxManager.instance.setBassBoostStrength((bassBoost * 1000).toInt().toShort())
                                AudioFxManager.instance.setVirtualizerStrength((virtualizer * 1000).toInt().toShort())
                            },
                            label = { Text(preset, fontSize = 11.sp) }
                        )
                    }
                }

                // Enhancers: Bass Boost & 3D Surround
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Extra Bass Boost", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("${(bassBoost * 100).toInt()}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = bassBoost,
                            onValueChange = {
                                bassBoost = it
                                AudioFxManager.instance.setBassBoostStrength((it * 1000).toInt().toShort())
                            },
                            enabled = isEnabled
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3D Spatial Virtualizer", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("${(virtualizer * 100).toInt()}%", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = virtualizer,
                            onValueChange = {
                                virtualizer = it
                                AudioFxManager.instance.setVirtualizerStrength((it * 1000).toInt().toShort())
                            },
                            enabled = isEnabled
                        )
                    }
                }

                // 5-Band Graphic Equalizer
                Text("5-Band Frequency Response", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    bandFrequencies.forEachIndexed { index, freq ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(freq, modifier = Modifier.width(60.dp), style = MaterialTheme.typography.labelSmall)
                            Slider(
                                value = bandLevels[index],
                                onValueChange = {
                                    bandLevels[index] = it
                                    AudioFxManager.instance.setBandLevel(index.toShort(), (it * 100).toInt().toShort())
                                },
                                valueRange = -10f..10f,
                                modifier = Modifier.weight(1f),
                                enabled = isEnabled
                            )
                            Text(
                                text = "${if (bandLevels[index] > 0) "+" else ""}${bandLevels[index].toInt()}dB",
                                modifier = Modifier.width(44.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Sleep Timer
                Text("Music Sleep Timer", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0 to "Off", 15 to "15m", 30 to "30m", 45 to "45m", 60 to "1h").forEach { (mins, label) ->
                        FilterChip(
                            selected = selectedTimerMinutes == mins,
                            onClick = { selectedTimerMinutes = mins },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Apply & Close")
            }
        }
    )
}
