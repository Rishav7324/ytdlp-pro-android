package com.ytdlp.app.ui.components.equalizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EqualizerDialog(
    onDismiss: () -> Unit
) {
    var bassLevel by remember { mutableFloatStateOf(50f) }
    var vocalLevel by remember { mutableFloatStateOf(50f) }
    var trebleLevel by remember { mutableFloatStateOf(50f) }
    var sleepTimerMinutes by remember { mutableIntStateOf(0) }
    var selectedPreset by remember { mutableStateOf("Default") }

    val presets = listOf("Default", "Bass Boost", "Vocal", "Rock", "Pop", "Electronic")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Audio Equalizer & FX",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(3).forEach { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = {
                                selectedPreset = preset
                                when (preset) {
                                    "Bass Boost" -> { bassLevel = 90f; vocalLevel = 40f; trebleLevel = 50f }
                                    "Vocal" -> { bassLevel = 30f; vocalLevel = 95f; trebleLevel = 70f }
                                    else -> { bassLevel = 50f; vocalLevel = 50f; trebleLevel = 50f }
                                }
                            },
                            label = { Text(preset, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sliders
                Text("Bass Boost (${bassLevel.toInt()}%)", style = MaterialTheme.typography.labelSmall)
                Slider(
                    value = bassLevel,
                    onValueChange = { bassLevel = it; selectedPreset = "Custom" },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                )

                Text("Vocal Clarity (${vocalLevel.toInt()}%)", style = MaterialTheme.typography.labelSmall)
                Slider(
                    value = vocalLevel,
                    onValueChange = { vocalLevel = it; selectedPreset = "Custom" },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary)
                )

                Text("Treble (${trebleLevel.toInt()}%)", style = MaterialTheme.typography.labelSmall)
                Slider(
                    value = trebleLevel,
                    onValueChange = { trebleLevel = it; selectedPreset = "Custom" },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.tertiary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sleep Timer Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⏲️ Music Sleep Timer",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0 to "Off", 15 to "15m", 30 to "30m", 60 to "1h").forEach { (mins, label) ->
                                FilterChip(
                                    selected = sleepTimerMinutes == mins,
                                    onClick = { sleepTimerMinutes = mins },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Apply")
            }
        }
    )
}
