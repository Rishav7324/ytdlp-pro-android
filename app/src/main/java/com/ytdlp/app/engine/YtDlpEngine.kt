package com.ytdlp.app.engine

import android.content.Context
import android.util.Log
import com.yaedd.youtubedl_android.YoutubeDL
import com.yaedd.youtubedl_android.YoutubeDLException
import com.yaedd.youtubedl_android.YoutubeDLRequest
import com.yaedd.youtubedl_android.YoutubeDLResponse
import com.ytdlp.app.data.local.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

data class VideoInfo(
    val id: String,
    val title: String,
    val uploader: String,
    val durationSeconds: Long,
    val thumbnailUrl: String,
    val webpageUrl: String,
    val formats: List<DownloadFormat>,
    val viewCount: Long = 0,
    val uploadDate: String = "",
    val description: String = ""
)

data class DownloadFormat(
    val formatId: String,
    val extension: String,
    val resolution: String,
    val note: String,
    val isAudioOnly: Boolean,
    val filesize: Long = 0,
    val formatNote: String = "",
    val qualityRank: Int = 0
)

object YtDlpEngine {

    private const val TAG = "YtDlpEngine"
    private var isInitialized = false
    private var lastInitError: String? = null

    suspend fun ensureInitialized(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext Result.success(Unit)

        try {
            YoutubeDL.getInstance().init(context.applicationContext)
            isInitialized = true
            Log.d(TAG, "yt-dlp engine initialized successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize yt-dlp engine", e)
            lastInitError = e.message
            Result.failure(e)
        }
    }

    suspend fun extractInfo(context: Context, url: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
        try {
            val initRes = ensureInitialized(context)
            if (initRes.isFailure) {
                return@withContext Result.failure(
                    initRes.exceptionOrNull() ?: YoutubeDLException(lastInitError ?: "Failed to initialize engine")
                )
            }

            val normalized = normalizeUrl(url)
            val request = YoutubeDLRequest(normalized)
            request.addOption("--dump-single-json")
            request.addOption("--no-playlist")
            request.addOption("--no-check-certificates")
            request.addOption("--geo-bypass")
            request.addOption("--extractor-args", "youtube:player_client=android,ios,web")

            val response: YoutubeDLResponse = YoutubeDL.getInstance().execute(request)
            val output = response.out

            if (output.isNullOrBlank()) {
                return@withContext Result.failure(YoutubeDLException("Empty metadata response from extractor"))
            }

            val json = JSONObject(output)
            val videoInfo = parseVideoInfoJson(json, normalized)
            Result.success(videoInfo)
        } catch (e: YoutubeDLException) {
            Log.e(TAG, "yt-dlp extraction error for URL: $url", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during extraction: $url", e)
            Result.failure(e)
        }
    }

    private fun normalizeUrl(rawUrl: String): String {
        var url = rawUrl.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        return url
    }

    private fun parseVideoInfoJson(json: JSONObject, rawUrl: String): VideoInfo {
        val id = json.optString("id", System.currentTimeMillis().toString())
        val title = json.optString("title", "Untitled Media")
        val uploader = json.optString("uploader", json.optString("channel", "Unknown Creator"))
        val duration = json.optLong("duration", 0L)
        val webpageUrl = json.optString("webpage_url", rawUrl)
        val viewCount = json.optLong("view_count", 0L)
        val uploadDate = json.optString("upload_date", "")
        val description = json.optString("description", "")

        var thumbnailUrl = json.optString("thumbnail", "")
        if (thumbnailUrl.isBlank()) {
            val thumbnailsArray = json.optJSONArray("thumbnails")
            if (thumbnailsArray != null && thumbnailsArray.length() > 0) {
                thumbnailUrl = thumbnailsArray.getJSONObject(thumbnailsArray.length() - 1).optString("url", "")
            }
        }

        val formats = buildHighQualityPresets()

        return VideoInfo(
            id = id,
            title = title,
            uploader = uploader,
            durationSeconds = duration,
            thumbnailUrl = thumbnailUrl,
            webpageUrl = webpageUrl,
            formats = formats,
            viewCount = viewCount,
            uploadDate = uploadDate,
            description = description
        )
    }

    private fun buildHighQualityPresets(): List<DownloadFormat> {
        val list = mutableListOf<DownloadFormat>()

        // 4K Ultra HD
        list.add(
            DownloadFormat(
                formatId = "bv*[height<=2160]+ba/b[height<=2160]/best",
                extension = "mp4",
                resolution = "4K 2160p Ultra HD",
                note = "Crisp 4K UHD Video + High Bitrate Audio",
                isAudioOnly = false,
                qualityRank = 2160
            )
        )

        // 1080p Full HD
        list.add(
            DownloadFormat(
                formatId = "bv*[height<=1080]+ba/b[height<=1080]/best",
                extension = "mp4",
                resolution = "1080p Full HD",
                note = "Crisp 1080p FHD Video + High Bitrate Audio (Recommended)",
                isAudioOnly = false,
                qualityRank = 1080
            )
        )

        // 720p HD
        list.add(
            DownloadFormat(
                formatId = "bv*[height<=720]+ba/b[height<=720]/best",
                extension = "mp4",
                resolution = "720p HD",
                note = "Standard HD / Fast Download",
                isAudioOnly = false,
                qualityRank = 720
            )
        )

        // 480p SD
        list.add(
            DownloadFormat(
                formatId = "bv*[height<=480]+ba/b[height<=480]/best",
                extension = "mp4",
                resolution = "480p SD",
                note = "Low data usage",
                isAudioOnly = false,
                qualityRank = 480
            )
        )

        // Audio Presets
        list.add(
            DownloadFormat(
                formatId = "ba/b",
                extension = "mp3",
                resolution = "Audio (MP3 320k Studio)",
                note = "Highest Quality 320kbps MP3",
                isAudioOnly = true,
                qualityRank = 320
            )
        )
        list.add(
            DownloadFormat(
                formatId = "ba/b",
                extension = "m4a",
                resolution = "Audio (M4A / AAC)",
                note = "Crystal Clear AAC Audio",
                isAudioOnly = true,
                qualityRank = 256
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
                // Ensure High-Definition Video Stream + Audio stream merging
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
