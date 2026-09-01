package com.ytdlp.app.engine

import android.content.Context
import android.util.Log
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo as YtdlVideoInfo
import com.ytdlp.app.data.local.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

object YtDlpEngine {
    private const val TAG = "YtDlpEngine"
    private val initMutex = Mutex()
    var isInitialized = false
        private set
    var lastInitError: String? = null
        private set

    suspend fun ensureInitialized(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext Result.success(Unit)
        initMutex.withLock {
            if (isInitialized) return@withContext Result.success(Unit)
            try {
                Log.d(TAG, "Initializing yt-dlp, FFmpeg, and Aria2c native libraries...")
                val appContext = context.applicationContext

                try {
                    YoutubeDL.getInstance().init(appContext)
                } catch (e: Exception) {
                    if (e.message?.contains("already initialized", ignoreCase = true) != true) {
                        throw e
                    }
                }

                try {
                    FFmpeg.getInstance().init(appContext)
                } catch (e: Exception) {
                    Log.w(TAG, "FFmpeg init notice: ${e.message}")
                }

                try {
                    Aria2c.getInstance().init(appContext)
                } catch (e: Exception) {
                    Log.w(TAG, "Aria2c init notice: ${e.message}")
                }

                isInitialized = true
                lastInitError = null
                Log.d(TAG, "All engine components successfully initialized")
                Result.success(Unit)
            } catch (e: Throwable) {
                lastInitError = e.message ?: "Failed to extract native binaries"
                Log.e(TAG, "Fatal engine initialization failure", e)
                Result.failure(e)
            }
        }
    }

    fun normalizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        if (trimmed.startsWith("youtube.com/") || trimmed.startsWith("www.youtube.com/") ||
            trimmed.startsWith("m.youtube.com/") || trimmed.startsWith("youtu.be/") ||
            trimmed.startsWith("instagram.com/") || trimmed.startsWith("tiktok.com/") ||
            trimmed.startsWith("twitter.com/") || trimmed.startsWith("x.com/") ||
            trimmed.startsWith("reddit.com/") || trimmed.startsWith("bilibili.com/")) {
            return "https://$trimmed"
        }
        val cleanId = trimmed.split("?").firstOrNull() ?: trimmed
        if (cleanId.length == 11 && cleanId.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
            return "https://www.youtube.com/watch?v=$trimmed"
        }
        return "https://$trimmed"
    }

    suspend fun fetchVideoInfo(context: Context, url: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
        try {
            val initRes = ensureInitialized(context)
            if (initRes.isFailure) {
                return@withContext Result.failure(
                    initRes.exceptionOrNull() ?: YoutubeDLException(lastInitError ?: "Failed to initialize engine")
                )
            }

            val normalized = normalizeUrl(url)
            val request = YoutubeDLRequest(normalized)
            request.addOption("--no-mtime")
            request.addOption("--geo-bypass")
            // Bypass YouTube bot verification & precondition checks using mobile client
            request.addOption("--extractor-args", "youtube:player_client=android,ios,web")
            request.addOption("--no-check-certificates")

            val ydlInfo: YtdlVideoInfo = YoutubeDL.getInstance().getInfo(request)
            val formats = parseFormats(ydlInfo)

            val durationVal: Long = when (val d = ydlInfo.duration) {
                is Number -> d.toLong()
                else -> 0L
            }

            val viewCountVal: Long = when (val v = ydlInfo.viewCount) {
                is Number -> v.toLong()
                else -> 0L
            }

            val videoInfo = VideoInfo(
                url = normalized,
                id = ydlInfo.id ?: System.currentTimeMillis().toString(),
                title = ydlInfo.title ?: "Unknown Title",
                uploader = ydlInfo.uploader ?: ydlInfo.extractor ?: "Unknown Creator",
                thumbnailUrl = ydlInfo.thumbnail ?: "",
                durationSeconds = durationVal,
                viewCount = viewCountVal,
                description = ydlInfo.description ?: "",
                extractor = ydlInfo.extractor ?: "",
                formats = formats
            )
            Result.success(videoInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch video info for $url", e)
            Result.failure(e)
        }
    }

    private fun parseFormats(ydlInfo: YtdlVideoInfo): List<DownloadFormat> {
        val list = mutableListOf<DownloadFormat>()

        // Default Presets
        list.add(
            DownloadFormat(
                formatId = "bestvideo+bestaudio/best",
                extension = "mp4",
                resolution = "Best Video (Max Quality)",
                note = "Highest available resolution & audio",
                isAudioOnly = false
            )
        )
        list.add(
            DownloadFormat(
                formatId = "bestvideo[height<=1080]+bestaudio/best[height<=1080]/best",
                extension = "mp4",
                resolution = "1080p Full HD",
                note = "1080p MP4 / Recommended",
                isAudioOnly = false
            )
        )
        list.add(
            DownloadFormat(
                formatId = "bestvideo[height<=720]+bestaudio/best[height<=720]/best",
                extension = "mp4",
                resolution = "720p HD",
                note = "Fast download, good quality",
                isAudioOnly = false
            )
        )
        list.add(
            DownloadFormat(
                formatId = "bestvideo[height<=480]+bestaudio/best[height<=480]/best",
                extension = "mp4",
                resolution = "480p SD",
                note = "Low data usage",
                isAudioOnly = false
            )
        )

        // Audio Presets
        list.add(
            DownloadFormat(
                formatId = "bestaudio/best",
                extension = "mp3",
                resolution = "Audio (MP3 320k)",
                note = "Best quality audio extraction",
                isAudioOnly = true
            )
        )
        list.add(
            DownloadFormat(
                formatId = "bestaudio/best",
                extension = "m4a",
                resolution = "Audio (M4A / AAC)",
                note = "Apple & Android native format",
                isAudioOnly = true
            )
        )
        list.add(
            DownloadFormat(
                formatId = "bestaudio/best",
                extension = "opus",
                resolution = "Audio (OPUS)",
                note = "High efficiency audio codec",
                isAudioOnly = true
            )
        )

        return list
    }

    suspend fun executeDownload(
        context: Context,
        taskId: String,
        url: String,
        outputDir: File,
        mediaType: MediaType,
        formatId: String,
        audioExtension: String = "mp3",
        embedThumbnail: Boolean = true,
        embedSubtitles: Boolean = false,
        useAria2: Boolean = true,
        customArgs: String = "",
        cookiesFile: File? = null,
        onProgress: (progress: Float, speed: String, eta: String, line: String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val initRes = ensureInitialized(context)
            if (initRes.isFailure) {
                return@withContext Result.failure(
                    initRes.exceptionOrNull() ?: YoutubeDLException(lastInitError ?: "Failed to initialize engine")
                )
            }

            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val normalized = normalizeUrl(url)
            val request = YoutubeDLRequest(normalized)
            request.addOption("-o", "${outputDir.absolutePath}/%(title)s.%(ext)s")
            request.addOption("--no-mtime")
            request.addOption("--restrict-filenames")
            request.addOption("--geo-bypass")
            request.addOption("--extractor-args", "youtube:player_client=android,ios,web")
            request.addOption("--no-check-certificates")

            if (mediaType == MediaType.AUDIO) {
                request.addOption("-x")
                request.addOption("--audio-format", audioExtension)
                request.addOption("--audio-quality", "0")
                if (embedThumbnail) {
                    request.addOption("--embed-thumbnail")
                }
                request.addOption("--add-metadata")
            } else {
                request.addOption("-f", formatId)
                if (embedThumbnail) {
                    request.addOption("--embed-thumbnail")
                }
                if (embedSubtitles) {
                    request.addOption("--embed-subs")
                    request.addOption("--all-subs")
                }
                request.addOption("--merge-output-format", "mp4")
            }

            if (useAria2) {
                request.addOption("--external-downloader", "aria2c")
                request.addOption("--external-downloader-args", "aria2c:-j 8 -x 8 -s 8 -k 1M")
            }

            if (cookiesFile != null && cookiesFile.exists()) {
                request.addOption("--cookies", cookiesFile.absolutePath)
            }

            if (customArgs.isNotBlank()) {
                customArgs.trim().split("\\s+".toRegex()).forEach { arg ->
                    if (arg.isNotBlank()) {
                        request.addOption(arg)
                    }
                }
            }

            var lastProgress = 0f
            var lastSpeed = ""
            var lastEta = ""

            YoutubeDL.getInstance().execute(request, taskId) { progress, etaInSeconds, line ->
                val calculatedProgress = if (progress > 0f) progress else lastProgress
                lastProgress = calculatedProgress

                // Parse speed if available in stdout
                if (line.contains("at") && line.contains("/s")) {
                    val match = Regex("""at\s+([0-9.]+[KMG]i?B/s)""").find(line)
                    if (match != null) {
                        lastSpeed = match.groupValues[1]
                    }
                }

                if (etaInSeconds > 0) {
                    val minutes = etaInSeconds / 60
                    val seconds = etaInSeconds % 60
                    lastEta = String.format("%02d:%02d", minutes, seconds)
                }

                onProgress(calculatedProgress, lastSpeed, lastEta, line)
            }

            // Find downloaded file
            val files = outputDir.listFiles()?.sortedByDescending { it.lastModified() }
            val downloadedFile = files?.firstOrNull() ?: outputDir

            Result.success(downloadedFile)
        } catch (e: Exception) {
            Log.e(TAG, "Download execution failed for taskId: $taskId", e)
            Result.failure(e)
        }
    }

    fun cancelDownload(taskId: String) {
        try {
            YoutubeDL.getInstance().destroyProcessById(taskId)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling download $taskId", e)
        }
    }
}
