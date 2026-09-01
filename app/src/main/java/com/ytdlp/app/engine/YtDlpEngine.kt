package com.ytdlp.app.engine

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo as YtdlVideoInfo
import com.ytdlp.app.data.local.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object YtDlpEngine {
    private const val TAG = "YtDlpEngine"

    suspend fun fetchVideoInfo(url: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
        try {
            val ydlInfo: YtdlVideoInfo = YoutubeDL.getInstance().getInfo(url)
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
                url = url,
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
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val request = YoutubeDLRequest(url)
            request.addOption("-o", "${outputDir.absolutePath}/%(title)s.%(ext)s")
            request.addOption("--no-mtime")
            request.addOption("--restrict-filenames")

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
