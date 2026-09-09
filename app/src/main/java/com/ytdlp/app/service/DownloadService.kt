package com.ytdlp.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ytdlp.app.YtDlpApp
import com.ytdlp.app.data.local.DownloadStatus
import com.ytdlp.app.data.repository.DownloadRepository
import com.ytdlp.app.engine.YtDlpEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class DownloadService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var currentJob: Job? = null
    private var currentDownloadId: Long? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1L)
                if (downloadId != -1L) processNextOrSpecific(downloadId)
            }
            ACTION_CANCEL_DOWNLOAD -> {
                val downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1L)
                if (downloadId != -1L) cancelDownload(downloadId)
            }
        }
        return START_NOT_STICKY
    }

    private fun processNextOrSpecific(downloadId: Long) {
        if (currentJob?.isActive == true) {
            Log.d(TAG, "Download service is already processing; task $downloadId remains queued.")
            return
        }
        currentJob = serviceScope.launch { processDownload(downloadId) }
    }

    private suspend fun processDownload(downloadId: Long) {
        val repository = YtDlpApp.instance.repository
        val download = repository.getDownloadById(downloadId)
        if (download == null) {
            finishOrStartNext(repository)
            return
        }

        currentDownloadId = download.id
        val initialNotification = NotificationHelper.buildForegroundNotification(
            this@DownloadService,
            download.title,
            0,
            "",
            ""
        ).build()
        startForeground(NotificationHelper.NOTIFICATION_ID_FOREGROUND, initialNotification)

        val cookiesFile = try {
            val cookiesContent = repository.preferences.cookiesContent.first()
            if (cookiesContent.isBlank()) null else File(cacheDir, "cookies.txt").also { it.writeText(cookiesContent) }
        } catch (error: Exception) {
            Log.w(TAG, "Could not prepare cookies file", error)
            null
        }

        try {
            val targetDir = File(repository.preferences.downloadPath.first())
            val embedThumbnail = repository.preferences.embedThumbnail.first()
            val embedSubtitles = repository.preferences.embedSubtitles.first()
            val useAria2 = repository.preferences.useAria2.first()
            val customArgs = repository.preferences.customArguments.first()
            val taskId = "download_${download.id}"

            val result = YtDlpEngine.executeDownload(
                context = this@DownloadService,
                taskId = taskId,
                url = download.url,
                outputDir = targetDir,
                mediaType = download.mediaType,
                formatId = download.formatId,
                embedThumbnail = embedThumbnail,
                embedSubtitles = embedSubtitles,
                useAria2 = useAria2,
                customArgs = customArgs,
                cookiesFile = cookiesFile
            ) { progress, speed, eta, _ ->
                val progressPercent = progress.toInt().coerceIn(0, 100)
                serviceScope.launch {
                    runCatching {
                        repository.updateProgress(
                            id = download.id,
                            progress = progress,
                            speed = speed,
                            eta = eta,
                            downloadedBytes = 0L,
                            status = DownloadStatus.DOWNLOADING
                        )
                    }.onFailure { Log.w(TAG, "Failed to persist progress", it) }
                }

                runCatching {
                    NotificationHelper.buildForegroundNotification(
                        this@DownloadService,
                        download.title,
                        progressPercent,
                        speed,
                        eta
                    ).build().also { notification ->
                        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        nm.notify(NotificationHelper.NOTIFICATION_ID_FOREGROUND, notification)
                    }
                }.onFailure { Log.w(TAG, "Failed to update download notification", it) }
            }

            result.fold(
                onSuccess = { file ->
                    runCatching {
                        val publicFile = com.ytdlp.app.util.StorageHelper.exportToPublicStorage(
                            context = this@DownloadService,
                            srcFile = file,
                            mediaType = download.mediaType,
                            title = download.title
                        )
                        repository.markCompleted(download.id, publicFile.absolutePath)
                        NotificationHelper.showCompletedNotification(
                            this@DownloadService,
                            download.id.toInt(),
                            download.title,
                            publicFile.absolutePath
                        )
                    }.onFailure { error ->
                        repository.markFailed(download.id, error.message ?: "Could not save downloaded file")
                    }
                },
                onFailure = { error ->
                    repository.markFailed(download.id, error.message ?: "Unknown download error")
                }
            )
        } finally {
            cookiesFile?.delete()
            currentDownloadId = null
        }

        finishOrStartNext(repository)
    }

    private fun finishOrStartNext(repository: DownloadRepository) {
        serviceScope.launch {
            val next = repository.activeAndQueuedDownloads.first().firstOrNull {
                it.status == DownloadStatus.QUEUED
            }
            if (next != null) {
                currentJob = serviceScope.launch { processDownload(next.id) }
            } else {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun cancelDownload(downloadId: Long) {
        YtDlpEngine.cancelDownload("download_$downloadId")
        serviceScope.launch {
            runCatching {
                YtDlpApp.instance.repository.updateProgress(
                    id = downloadId,
                    progress = 0f,
                    speed = "",
                    eta = "",
                    downloadedBytes = 0L,
                    status = DownloadStatus.CANCELLED
                )
            }.onFailure { Log.w(TAG, "Failed to mark download cancelled", it) }
        }
        if (currentDownloadId == downloadId) {
            currentJob?.cancel()
            currentDownloadId = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "DownloadService"
        const val ACTION_START_DOWNLOAD = "com.ytdlp.app.ACTION_START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.ytdlp.app.ACTION_CANCEL_DOWNLOAD"
        const val EXTRA_DOWNLOAD_ID = "extra_download_id"

        fun startDownload(context: Context, downloadId: Long) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun cancelDownload(context: Context, downloadId: Long) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            }
            context.startService(intent)
        }
    }
}
