package com.ytdlp.app.data.repository

import com.ytdlp.app.data.local.DownloadDao
import com.ytdlp.app.data.local.DownloadEntity
import com.ytdlp.app.data.local.DownloadStatus
import com.ytdlp.app.data.local.MediaType
import com.ytdlp.app.data.preferences.AppPreferences
import kotlinx.coroutines.flow.Flow

class DownloadRepository(
    private val downloadDao: DownloadDao,
    val preferences: AppPreferences
) {
    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()
    val activeAndQueuedDownloads: Flow<List<DownloadEntity>> = downloadDao.getActiveAndQueuedDownloads()
    val completedDownloads: Flow<List<DownloadEntity>> = downloadDao.getCompletedDownloads()

    fun getCompletedDownloadsByType(mediaType: MediaType): Flow<List<DownloadEntity>> {
        return downloadDao.getCompletedDownloadsByType(mediaType)
    }

    suspend fun getDownloadById(id: Long): DownloadEntity? {
        return downloadDao.getDownloadById(id)
    }

    suspend fun enqueueDownload(download: DownloadEntity): Long {
        return downloadDao.insert(download)
    }

    suspend fun updateProgress(
        id: Long,
        progress: Float,
        speed: String,
        eta: String,
        downloadedBytes: Long,
        status: DownloadStatus = DownloadStatus.DOWNLOADING
    ) {
        downloadDao.updateProgress(id, progress, speed, eta, downloadedBytes, status)
    }

    suspend fun markCompleted(id: Long, targetPath: String) {
        downloadDao.markCompleted(id = id, targetPath = targetPath)
    }

    suspend fun markFailed(id: Long, error: String) {
        downloadDao.markFailed(id = id, error = error)
    }

    suspend fun deleteDownload(id: Long) {
        downloadDao.deleteById(id)
    }

    suspend fun clearCompleted() {
        downloadDao.clearCompleted()
    }
}
