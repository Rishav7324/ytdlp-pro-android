package com.ytdlp.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ytdlp.app.ui.theme.NovaAquaSoft
import com.ytdlp.app.ui.theme.NovaInk

data class LegalEntry(val title: String, val subtitle: String, val icon: ImageVector, val sections: List<Pair<String, String>>)

private val entries = listOf(
    LegalEntry("Privacy Policy", "Data, permissions and network requests", Icons.Rounded.PrivacyTip, listOf(
        "Local-first" to "NovaFetch does not require a NovaFetch account and is designed to keep settings, history and app state on your device.",
        "Network requests" to "When you process a URL, NovaFetch and its bundled engines may connect to the requested service. That service may receive your IP address and other request information under its own privacy policy.",
        "Permissions" to "Android permissions are used for networking, notifications, foreground downloads and media access where required. You can revoke supported permissions in Android Settings.",
        "Third parties" to "yt-dlp, FFmpeg, Aria2c, AndroidX, Compose, Media3, Room, DataStore and Coil are third-party components. Their own policies and licenses apply."
    )),
    LegalEntry("Terms of Use", "Rules for responsible use", Icons.Rounded.Gavel, listOf(
        "Lawful use" to "Use NovaFetch only for lawful purposes and only with content you are authorized to access, download, convert or store.",
        "Service terms" to "You are responsible for complying with the terms and policies of every website or service you access through the app.",
        "No guarantee" to "Extractors, formats, codecs and remote services can change or stop working. NovaFetch does not guarantee continued compatibility.",
        "Responsibility" to "You are responsible for the URLs you process, files you save, permissions you grant and consequences of your use."
    )),
    LegalEntry("Disclaimer", "Content rights and software limitations", Icons.Rounded.Info, listOf(
        "Copyright" to "Downloading, converting or redistributing media may be restricted by copyright, licenses, platform terms, privacy rights or local law. NovaFetch does not grant rights to media.",
        "Access controls" to "Do not use NovaFetch to bypass protections or access controls you are not authorized to bypass.",
        "Third-party services" to "The project is not responsible for outages, rate limits, removed content, account restrictions or changes made by remote services.",
        "Liability" to "To the extent permitted by law, the project and contributors are not liable for losses arising from use or inability to use the software."
    )),
    LegalEntry("Copyright & DMCA", "Project and content ownership", Icons.Rounded.Copyright, listOf(
        "Project" to "NovaFetch source is distributed under GPL-3.0-or-later unless a file states otherwise. Third-party notices remain applicable.",
        "Media" to "NovaFetch does not own media, thumbnails, metadata or other content returned by remote services. Rights remain with their respective owners.",
        "Copyright concerns" to "For a genuine copyright concern, provide identification of the work, the material at issue, contact information and a good-faith statement through the project's published support channel. This app cannot adjudicate ownership disputes.",
        "Repository" to "The repository is the authoritative source for project license, contribution and security information."
    )),
    LegalEntry("Open Source Licenses", "Third-party software notices", Icons.Rounded.Code, listOf(
        "NovaFetch" to "GPL-3.0-or-later. See the repository LICENSE file for the complete license text.",
        "yt-dlp" to "Used through an Android integration. yt-dlp is GPL-licensed; its upstream project contains the authoritative license and source information.",
        "FFmpeg & Aria2" to "Used for media processing and optional download acceleration. Their respective licenses and notices apply.",
        "Android libraries" to "AndroidX, Jetpack Compose, Media3, Room, DataStore, Coil, Kotlin and related libraries remain under their respective licenses.",
        "Notice" to "Always consult upstream license files for the exact terms of each dependency."
    ))
)

@Composable
fun LegalScreen(onBack: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val entry = entries.firstOrNull { it.title == selected }
    if (entry != null) { LegalArticle(entry, onBack = { selected = null }); return }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.ArrowBack, "Back", Modifier.size(24.dp).clickable(onClick = onBack), tint = NovaInk)
            Spacer(Modifier.width(14.dp)); Column { Text("Legal & Open Source", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold); Text("NovaFetch • v2.0.1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Spacer(Modifier.height(18.dp))
        entries.forEach { item ->
            Card(Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable { selected = item.title }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).background(NovaAquaSoft, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(item.icon, null, tint = NovaInk) }
                    Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(item.title, fontWeight = FontWeight.Bold); Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Rounded.ChevronRight, "Open", tint = NovaInk)
                }
            }
        }
        Text("These notices are informational. The repository license and third-party license files are authoritative.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
    }
}

@Composable
private fun LegalArticle(entry: LegalEntry, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.ArrowBack, "Back", Modifier.size(24.dp).clickable(onClick = onBack), tint = NovaInk); Spacer(Modifier.width(14.dp)); Text(entry.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold) }
        Spacer(Modifier.height(18.dp))
        entry.sections.forEach { (title, body) ->
            Card(Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(18.dp)) { Text(title, fontWeight = FontWeight.ExtraBold, color = NovaInk); Spacer(Modifier.height(7.dp)); Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }
}
