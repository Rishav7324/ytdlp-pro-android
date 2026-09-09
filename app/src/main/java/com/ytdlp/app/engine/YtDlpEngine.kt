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

    @Volatile
    var isInitialized = false
        private set
    var lastInitError: String? = null
        private set

    suspend fun ensureInitialized(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext Result.success(Unit)
        initMutex.withLock {
            if (isInitialized) return@withContext Result.success(Unit)
            runCatching {
                val appContext = context.applicationContext
                YoutubeDL.getInstance().init(appContext)
                FFmpeg.getInstance().init(appContext)
                Aria2c.getInstance().init(appContext)
                isInitialized = true
                lastInitError = null
            }.fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { error ->
                    lastInitError = error.message ?: "Unknown initialization error"
                    Log.e(TAG, "Engine initialization failed", error)
                    Result.failure(error)
                }
            )
        }
    }

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
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Log.e(TAG, "Metadata extraction failed for $url", error)
                Result.failure(error)
            }
        )
    }

    private fun mapFormats(info: YtdlVideoInfo): List<DownloadFormat> {
        val formats = info.formats.orEmpty()
        if (formats.isEmpty()) return emptyList()

        return formats
            .mapNotNull { format ->
                val id = format.formatId.orEmpty()
                if (id.isBlank()) return@mapNotNull null
                val height = (format.height as? Number)?.toInt() ?: 0
                val audioOnly = height <= 0
                val extension = format.ext.orEmpty().ifBlank { "media" }
                val resolution = if (audioOnly) "Audio" else "${height}p"
                val noteParts = buildList {
                    format.vcodec.orEmpty().takeIf { it.isNotBlank() }?.let(::add)
                    format.acodec.orEmpty().takeIf { it.isNotBlank() }?.let(::add)
                }
                DownloadFormat(
                    formatId = id,
                    extension = extension,
                    resolution = resolution,
                    note = noteParts.joinToString(" • ").ifBlank { "Available stream" },
                    isAudioOnly = audioOnly,
                    fileSizeApprox = 0L,
                    fps = (format.fps as? Number)?.toInt(),
                    vcodec = format.vcodec,
                    acodec = format.acodec
                )
            }
            .distinctBy { it.formatId }
            .sortedWith(compareByDescending<DownloadFormat> { !it.isAudioOnly }.thenByDescending { it.resolution.removeSuffix("p").toIntOrNull() ?: 0 })
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
        runCatching {
            ensureInitialized(context).getOrThrow()

            val validDir = outputDir.takeIf { it.exists() || it.mkdirs() }
                ?: context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir
            val normalized = normalizeUrl(url)
            val outputTemplate = "${validDir.absolutePath}/%(title).180B [%(id)s].%(ext)s"
            val request = YoutubeDLRequest(normalized).apply {
                addOption("-o", outputTemplate)
                addOption("--no-mtime")
                addOption("--restrict-filenames")
                addOption("--newline")
                addOption("--no-playlist")
                addOption("--concurrent-fragments", if (useAria2) "8" else "4")
            }

            if (mediaType == MediaType.AUDIO) {
                request.addOption("-f", "ba/b")
                request.addOption("-x")
                request.addOption("--audio-format", audioExtension)
                request.addOption("--audio-quality", "0")
                request.addOption("--add-metadata")
                if (embedThumbnail) request.addOption("--embed-thumbnail")
            } else {
                val selectedFormat = formatId.ifBlank { "bv*[height<=1080]+ba/b[height<=1080]/best" }
                val normalizedFormat = if (selectedFormat.contains("+") || selectedFormat.contains("/") || selectedFormat.contains("[")) {
                    selectedFormat
                } else {
                    "$selectedFormat+ba/b"
                }
                request.addOption("-f", normalizedFormat)
                request.addOption("--merge-output-format", "mp4")
                if (embedThumbnail) request.addOption("--embed-thumbnail")
                if (embedSubtitles) request.addOption("--embed-subs")
            }

            if (cookiesFile?.exists() == true) request.addOption("--cookies", cookiesFile.absolutePath)

            if (customArgs.isNotBlank()) {
                customArgs.trim()
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                    .forEach(request::addOption)
            }

            var lastSpeed = ""
            var lastEta = ""

            YoutubeDL.getInstance().execute(request, taskId) { progress, etaSeconds, line ->
                val safeProgress = progress.coerceIn(0f, 100f)
                Regex("""(?:at|speed)\s+([0-9.]+(?:KiB|MiB|GiB|KB|MB|GB)/s)""")
                    .find(line)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.let { lastSpeed = it }
                if (etaSeconds > 0) lastEta = formatEta(etaSeconds)
                onProgress(safeProgress, lastSpeed, lastEta, line)
            }

            val downloadedFile = validDir.listFiles()
                ?.filter { file ->
                    file.isFile &&
                        !file.name.endsWith(".part", ignoreCase = true) &&
                        !file.name.endsWith(".ytdl", ignoreCase = true) &&
                        !file.name.endsWith(".temp", ignoreCase = true)
                }
                ?.maxByOrNull(File::lastModified)

            downloadedFile?.takeIf(File::exists)
                ?: throw YoutubeDLException("Download completed but the output file could not be resolved")
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Log.e(TAG, "Download execution failed for $url", error)
                Result.failure(error)
            }
        )
    }

    private fun formatEta(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        else String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun cancelDownload(taskId: String) {
        runCatching { YoutubeDL.getInstance().destroyProcessById(taskId) }
            .onFailure { Log.w(TAG, "Failed to cancel task $taskId", it) }
    }
}
