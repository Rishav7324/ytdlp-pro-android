package com.zyvro.app.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.app.PictureInPictureParams
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.zyvro.app.YtDlpApp
import com.zyvro.app.player.MediaPlayerManager
import com.zyvro.app.ui.navigation.AppNavigation
import com.zyvro.app.ui.theme.YtDlpTheme
import com.zyvro.app.ui.theme.resolveThemeMode

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
            val prefs = (application as YtDlpApp).preferences
            val themeMode by prefs.darkThemeMode.collectAsState(initial = "SYSTEM")
            val accent by prefs.accentColor.collectAsState(initial = "TEAL")
            val systemDark = isSystemInDarkTheme()
            val (dark, amoled) = resolveThemeMode(themeMode, systemDark)
            YtDlpTheme(darkTheme = dark, amoled = amoled, accent = accent) {
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

    /** NextPlayer-style continuity: home press while fullscreen video plays -> PiP. */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        runCatching {
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) return
            val manager = MediaPlayerManager.getInstance(this)
            if (manager.isVideoExpanded.value && manager.isPlaying.value) {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            }
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        if (!text.isNullOrBlank()) sharedUrlState = extractUrl(text) ?: text
    }

    private fun extractUrl(text: String): String? =
        Regex("""https?://[^\s]+""").find(text)?.value
}
