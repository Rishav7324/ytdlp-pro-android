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
 * Main launcher activity.
 *
 * Runtime permissions are requested by the feature that actually needs them,
 * rather than during startup. This keeps the first launch reliable on Android 13+.
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

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        if (!sharedText.isNullOrBlank()) {
            sharedUrlState = extractUrl(sharedText) ?: sharedText
        }
    }

    private fun extractUrl(text: String): String? =
        Regex("""https?://[^\\s]+""").find(text)?.value
}
