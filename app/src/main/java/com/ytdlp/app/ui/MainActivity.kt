package com.ytdlp.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ytdlp.app.ui.navigation.AppNavigation
import com.ytdlp.app.ui.theme.YtDlpTheme

/**
 * Main launcher activity. Feature-specific permissions are requested only
 * when required, keeping first launch reliable across Android versions.
 */
class MainActivity : ComponentActivity() {
    private var sharedUrlState: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)
        setContent {
            YtDlpTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(sharedUrl = sharedUrlState)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        if (!text.isNullOrBlank()) sharedUrlState = extractUrl(text) ?: text
    }

    private fun extractUrl(text: String): String? =
        Regex("""https?://[^\s]+""").find(text)?.value
}
