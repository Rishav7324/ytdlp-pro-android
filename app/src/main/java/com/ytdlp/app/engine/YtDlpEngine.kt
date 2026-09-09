package com.ytdlp.app.engine

import android.content.Context
import android.os.Environment
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

data class EngineFormat(
    val formatId: String,
    val extension: String,
    val resolution: String,
    val note: String,
    val isAudioOnly: Boolean,
    val fileSizeApprox: Long = 0L,
    val fps: Int? = null,
    val vcodec: String? = null,
    val acodec: String? = null
)

object YtDlpEngine {
    private const val TAG = "YtDlpEngine"
    private val initMutex = Mutex()
    @Volatile var isInitialized = false
        private set
    @Volatile var isAria2Initialized = false
        private set
    @Volatile var lastInitError: String? = null
        private set

    suspend fun ensureInitialized(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext Result.success(Unit)
        initMutex.withLock {
            if (isInitialized) return@withContext Result.success(Unit)
            runCatching {
                val appContext = context.applicationContext
                YoutubeDL.getInstance().init(appContext)
                FFmpeg.getInstance().init(appContext)
                runCatching {
                    Aria2c.getInstance().init(appContext)
                    isAria2Initialized = true
                }.onFailure {
                    isAria2Initialized = false
                    Log.w(TAG, "Aria2 unavailable; using yt-dlp downloader", it)
                }
                isInitialized = true
                lastInitError = null
            }.fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { error ->
                    lastInitError = error.message ?: "Engine initialization failed"
                    Log.e(TAG, "Engine initialization failed", error)
                    Result.failure(error)
                }
            )
        }
    }

    /**
     * Updates the bundled yt-dlp executable to the latest stable release.
     * The actual binary update is owned by youtubedl-android's updater; this
     * method intentionally does not just re-run init().
     */
    suspend fun updateEngine(context: Context): Result<String> =
        YtDlpUpdater.updateEngine(context)

    fun normalizeUrl(rawUrl: String): String {
        val url = rawUrl.trim()
        return if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
    }

    suspend fun fetchVideoInfo(context: Context, url: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
        runCatching {
            ensureInitialized(context).getOrThrow()
            val normalized = normalizeUrl(url)
            val info = YoutubeDL.getInstance().getInfo(normalized)
            val videoId = info.id.orEmpty().ifBlank { System.currentTimeMillis().toString() }
            VideoInfo(
                url = normalized,
                id = videoId,
                title = info.title.orEmpty().ifBlank { "Untitled media" },
                uploader = info.uploader.orEmpty().ifBlank { info.extractor.orEmpty().ifBlank { "Unknown creator" } },
                channelUrl = "",
                thumbnailUrl = info.thumbnail.orEmpty(),
                durationSeconds = (info.duration as? Number)?.toLong() ?: 0L,
                viewCount = (info.viewCount as? Number)?.toLong() ?: 0L,
                description = info.description.orEmpty(),
                extractor = info.extractor.orEmpty(),
                formats = mapFormats(info)
            )
        }.fold({ Result.success(it) }) { error ->
            Log.e(TAG, "Metadata extraction failed for $url", error)
            Result.failure(error)
        }
    }

    private fun mapFormats(info: YtdlVideoInfo): List<DownloadFormat> = info.formats.orEmpty().mapNotNull { format ->
        val id = format.formatId.orEmpty()
        if (id.isBlank()) return@mapNotNull null
        val height = (format.height as? Number)?.toInt() ?: 0
        val audioOnly = height <= 0
        DownloadFormat(id, format.ext.orEmpty().ifBlank { "media" }, if (audioOnly) "Audio" else "${height}p", listOfNotNull(format.vcodec, format.acodec).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Available stream" }, audioOnly, 0L, (format.fps as? Number)?.toInt(), format.vcodec, format.acodec)
    }.distinctBy { it.formatId }.sortedWith(compareByDescending<DownloadFormat> { !it.isAudioOnly }.thenByDescending { it.resolution.removeSuffix("p").toIntOrNull() ?: 0 })

    suspend fun executeDownload(context: Context, taskId: String, url: String, outputDir: File, mediaType: MediaType, formatId: String, audioExtension: String = "mp3", embedThumbnail: Boolean = true, embedSubtitles: Boolean = false, useAria2: Boolean = false, customArgs: String = "", cookiesFile: File? = null, onProgress: (Float, String, String, String) -> Unit): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            ensureInitialized(context).getOrThrow()
            val validDir = outputDir.takeIf { it.exists() || it.mkdirs() } ?: context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            val normalized = normalizeUrl(url)
            val request = YoutubeDLRequest(normalized).apply {
                addOption("-o", "${validDir.absolutePath}/%(title).180B [%(id)s].%(ext)s")
                addOption("--no-mtime"); addOption("--restrict-filenames"); addOption("--newline"); addOption("--no-playlist")
            }
            if (useAria2 && isAria2Initialized) request.addOption("--downloader", "libaria2c.so") else request.addOption("--concurrent-fragments", "4")
            if (mediaType == MediaType.AUDIO) {
                request.addOption("-f", "ba/b"); request.addOption("-x"); request.addOption("--audio-format", audioExtension); request.addOption("--audio-quality", "0"); request.addOption("--add-metadata")
                if (embedThumbnail) request.addOption("--embed-thumbnail")
            } else {
                val selected = formatId.ifBlank { "bv*[height<=1080]+ba/b[height<=1080]/best" }
                request.addOption("-f", if (selected.contains("+") || selected.contains("/") || selected.contains("[")) selected else "$selected+ba/b")
                request.addOption("--merge-output-format", "mp4")
                if (embedThumbnail) request.addOption("--embed-thumbnail")
                if (embedSubtitles) request.addOption("--embed-subs")
            }
            if (cookiesFile?.exists() == true) request.addOption("--cookies", cookiesFile.absolutePath)
            if (customArgs.isNotBlank()) customArgs.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.forEach(request::addOption)
            var speed = ""; var eta = ""; var resolved: File? = null
            YoutubeDL.getInstance().execute(request, taskId) { progress, etaSeconds, line ->
                Regex("""(?:at|speed)\s+([0-9.]+(?:KiB|MiB|GiB|KB|MB|GB)/s)""").find(line)?.groupValues?.getOrNull(1)?.let { speed = it }
                if (etaSeconds > 0) eta = formatEta(etaSeconds)
                val candidate = File(line.trim())
                if (candidate.isAbsolute && candidate.parentFile?.absolutePath == validDir.absolutePath && candidate.isFile) resolved = candidate
                onProgress(progress.coerceIn(0f, 100f), speed, eta, line)
            }
            resolved?.takeIf(File::exists) ?: validDir.listFiles()?.filter { it.isFile && !it.name.endsWith(".part", true) && !it.name.endsWith(".ytdl", true) && !it.name.endsWith(".temp", true) }?.maxByOrNull(File::lastModified) ?: throw YoutubeDLException("Download completed but output file was not found")
        }.fold({ Result.success(it) }) { error -> Log.e(TAG, "Download failed for $url", error); Result.failure(error) }
    }

    private fun formatEta(seconds: Long): String {
        val h = seconds / 3600; val m = (seconds % 3600) / 60; val s = seconds % 60
        return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
    }
    fun cancelDownload(taskId: String) = runCatching { YoutubeDL.getInstance().destroyProcessById(taskId) }.onFailure { Log.w(TAG, "Failed to cancel task $taskId", it) }
}
