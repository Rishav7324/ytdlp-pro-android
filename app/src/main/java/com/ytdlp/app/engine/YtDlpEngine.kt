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

data class VideoInfo(
    val url: String,
    val id: String,
    val title: String,
    val uploader: String,
    val thumbnailUrl: String,
    val durationSeconds: Long,
    val viewCount: Long = 0,
    val description: String = "",
    val extractor: String = "",
    val formats: List<DownloadFormat> = emptyList()
)

data class DownloadFormat(
    val formatId: String,
    val extension: String,
    val resolution: String,
    val note: String,
    val isAudioOnly: Boolean
)

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
                    Log.w(TAG, "YoutubeDL init returned: ${e.message}")
                }

                try {
                    FFmpeg.getInstance().init(appContext)
                } catch (e: Exception) {
                    Log.w(TAG, "FFmpeg init returned: ${e.message}")
                }

                try {
                    Aria2c.getInstance().init(appContext)
                } catch (e: Exception) {
                    Log.w(TAG, "Aria2c init returned: ${e.message}")
                }

                isInitialized = true
                lastInitError = null
                Log.d(TAG, "yt-dlp engine fully initialized and ready")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error during yt-dlp initialization", e)
                lastInitError = e.message ?: "Unknown initialization error"
                Result.failure(e)
            }
        }
    }

    fun normalizeUrl(rawUrl: String): String {
        var url = rawUrl.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        return url
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
            val ydlInfo: YtdlVideoInfo = YoutubeDL.getInstance().getInfo(normalized)

            val formats = parseFormats(ydlInfo)
            val durationVal = when (val d = ydlInfo.duration) {
                is Number -> d.toLong()
                else -> 0L
            }
            val viewCountVal = when (val v = ydlInfo.viewCount) {
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

        // 4K Ultra HD
        list.add(
            DownloadFormat(
                formatId = "bv*[height<=2160]+ba/b[height<=2160]/best",
                extension = "mp4",
                resolution = "4K 2160p Ultra HD",
                note = "Crisp 4K UHD Video + High Bitrate Audio",
                isAudioOnly = false
            )
        )

        // 1080p Full HD (Recommended)
        list.add(
            DownloadFormat(
                formatId = "bv*[height<=1080]+ba/b[height<=1080]/best",
                extension = "mp4",
                resolution = "1080p Full HD",
                note = "Crisp 1080p FHD Video + High Bitrate Audio (Recommended)",
                isAudioOnly = false
            )
        )

        // 720p HD
        list.add(
            DownloadFormat(
                formatId = "bv*[height<=720]+ba/b[height<=720]/best",
                extension = "mp4",
                resolution = "720p HD",
                note = "Fast download, standard HD quality",
                isAudioOnly = false
            )
        )

        // 480p SD
        list.add(
            DownloadFormat(
                formatId = "bv*[height<=480]+ba/b[height<=480]/best",
                extension = "mp4",
                resolution = "480p SD",
                note = "Low data usage",
                isAudioOnly = false
            )
        )

        // Audio Presets
        list.add(
            DownloadFormat(
                formatId = "ba/b",
                extension = "mp3",
                resolution = "Audio (MP3 320k Studio)",
                note = "Highest Quality 320kbps MP3",
                isAudioOnly = true
            )
        )
        list.add(
            DownloadFormat(
                formatId = "ba/b",
                extension = "m4a",
                resolution = "Audio (M4A / AAC)",
                note = "Apple & Android Native AAC",
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
        useAria2: Boolean = false,
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

            val validDir = if (!outputDir.exists() && !outputDir.mkdirs()) {
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            } else {
                outputDir
            }

            val normalized = normalizeUrl(url)
            val request = YoutubeDLRequest(normalized)
            request.addOption("-o", "${validDir.absolutePath}/%(title)s.%(ext)s")
            request.addOption("--no-mtime")
            request.addOption("--restrict-filenames")
            request.addOption("--geo-bypass")
            request.addOption("--extractor-args", "youtube:player_client=android,ios,web")
            request.addOption("--no-check-certificates")
            request.addOption("--concurrent-fragments", "5")

            if (mediaType == MediaType.AUDIO) {
                request.addOption("-f", "ba/b")
                request.addOption("-x")
                request.addOption("--audio-format", audioExtension)
                request.addOption("--audio-quality", "0")
                if (embedThumbnail) {
                    request.addOption("--embed-thumbnail")
                }
                request.addOption("--add-metadata")
            } else {
                val finalFormat = if (formatId.isNotBlank()) {
                    if (formatId.contains("+") || formatId.contains("bv*") || formatId.contains("bestvideo")) {
                        formatId
                    } else {
                        "$formatId+ba/b/best"
                    }
                } else {
                    "bv*[height<=1080]+ba/b[height<=1080]/best"
                }
                request.addOption("-f", finalFormat)
                if (embedThumbnail) {
                    request.addOption("--embed-thumbnail")
                }
                if (embedSubtitles) {
                    request.addOption("--embed-subs")
                }
                request.addOption("--merge-output-format", "mp4")
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

            val downloadedFile = validDir.listFiles()?.maxByOrNull { it.lastModified() }
            if (downloadedFile != null && downloadedFile.exists()) {
                Result.success(downloadedFile)
            } else {
                Result.failure(YoutubeDLException("Download completed but output file could not be located in $validDir"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Download execution failed for URL: $url", e)
            Result.failure(e)
        }
    }

    fun cancelDownload(taskId: String) {
        try {
            YoutubeDL.getInstance().destroyProcessById(taskId)
            Log.d(TAG, "Cancelled download process for task: $taskId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel process for task: $taskId", e)
        }
    }
}
