package com.ytdlp.app.engine

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDL.UpdateChannel
import com.yausername.youtubedl_common.SharedPrefsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object YtDlpUpdater {
    private const val TAG = "YtDlpUpdater"
    private const val VERSION_KEY = "dlpVersion"
    private const val VERSION_NAME_KEY = "dlpVersionName"
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 60_000
    private const val USER_AGENT = "NovaFetch/2.0 (Android; yt-dlp updater)"

    /**
     * Version/update must depend only on the yt-dlp core. FFmpeg and Aria2 are
     * optional features and must never block the Update button.
     */
    private fun ensureYtDlpCore(context: Context) {
        YoutubeDL.getInstance().init(context.applicationContext)
    }

    suspend fun getVersion(context: Context): String = withContext(Dispatchers.IO) {
        runCatching {
            ensureYtDlpCore(context)
            SharedPrefsHelper[context.applicationContext, VERSION_NAME_KEY]
                ?.takeIf { it.isNotBlank() }
                ?: SharedPrefsHelper[context.applicationContext, VERSION_KEY]
                    ?.takeIf { it.isNotBlank() }
                ?: "Bundled yt-dlp"
        }.getOrElse { error ->
            Log.e(TAG, "Failed to initialize/read yt-dlp version", error)
            "Engine unavailable"
        }
    }

    suspend fun updateEngine(
        context: Context,
        channel: UpdateChannel = UpdateChannel.STABLE
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val appContext = context.applicationContext
            ensureYtDlpCore(appContext)

            // The upstream library updater relies on a bare URL connection to the
            // GitHub API. On some Android/network combinations that can fail with
            // opaque exceptions. Use an explicit HTTPS connection with a User-Agent,
            // useful timeouts, HTTP error reporting and an atomic file replacement.
            val release = fetchLatestRelease(channel.apiUrl)
            val tag = release.tag
            val name = release.name.ifBlank { tag }
            val oldTag = SharedPrefsHelper[appContext, VERSION_KEY]

            if (tag == oldTag) {
                return@runCatching name
            }

            val binaryUrl = release.binaryUrl
            val baseDir = File(appContext.noBackupFilesDir, YoutubeDL.baseName)
            val ytdlpDir = File(baseDir, YoutubeDL.ytdlpDirName)
            if (!ytdlpDir.exists() && !ytdlpDir.mkdirs()) {
                throw IOException("Cannot create yt-dlp directory: ${ytdlpDir.absolutePath}")
            }

            val tempFile = File.createTempFile("yt-dlp-update-", ".tmp", ytdlpDir)
            try {
                downloadBinary(binaryUrl, tempFile)
                val binary = File(ytdlpDir, YoutubeDL.ytdlpBin)
                Files.move(
                    tempFile.toPath(),
                    binary.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                if (!binary.setExecutable(true, false)) {
                    Log.w(TAG, "Could not explicitly mark yt-dlp executable")
                }
                SharedPrefsHelper.update(appContext, VERSION_KEY, tag)
                SharedPrefsHelper.update(appContext, VERSION_NAME_KEY, name)
                Log.d(TAG, "yt-dlp updated successfully: $tag ($name)")
                name
            } finally {
                if (tempFile.exists()) tempFile.delete()
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                val message = error.message?.trim().orEmpty()
                Log.e(TAG, "yt-dlp stable update failed: ${message.ifBlank { error.javaClass.simpleName }}", error)
                Result.failure(
                    if (message.isNotBlank()) IOException("yt-dlp update failed: $message", error)
                    else IOException("yt-dlp update failed: ${error.javaClass.simpleName}", error)
                )
            }
        )
    }

    private data class Release(
        val tag: String,
        val name: String,
        val binaryUrl: String
    )

    private fun fetchLatestRelease(apiUrl: String): Release {
        val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", USER_AGENT)
        }

        return try {
            val code = connection.responseCode
            if (code !in 200..299) {
                val errorText = runCatching {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                }.getOrNull().orEmpty().replace(Regex("\\s+"), " ").take(240)
                throw IOException(
                    "GitHub API HTTP $code${if (errorText.isNotBlank()) ": $errorText" else ""}"
                )
            }
            val json = connection.inputStream.bufferedReader().use { YoutubeDL.objectMapper.readTree(it) }
            val tag = json["tag_name"]?.asText().orEmpty()
            if (tag.isBlank()) throw IOException("GitHub release response has no tag_name")
            val name = json["name"]?.asText().orEmpty()
            val assets = json["assets"]
            val binaryUrl = assets?.firstOrNull { it["name"]?.asText() == YoutubeDL.ytdlpBin }
                ?.get("browser_download_url")?.asText().orEmpty()
            if (binaryUrl.isBlank()) throw IOException("GitHub release has no yt-dlp binary asset")
            Release(tag, name, binaryUrl)
        } finally {
            connection.disconnect()
        }
    }

    private fun downloadBinary(url: String, target: File) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept", "application/octet-stream")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("yt-dlp download HTTP $code")
            }
            connection.inputStream.use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output, 64 * 1024)
                }
            }
            if (target.length() < 1024) {
                throw IOException("Downloaded yt-dlp binary is unexpectedly small")
            }
        } finally {
            connection.disconnect()
        }
    }
}
