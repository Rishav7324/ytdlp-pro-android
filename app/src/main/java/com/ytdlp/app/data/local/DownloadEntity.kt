package com.ytdlp.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class MediaType {
    VIDEO,
    AUDIO
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String,
    val uploader: String = "",
    val thumbnailUrl: String = "",
    val durationSeconds: Long = 0,
    val formatId: String = "best",
    val formatNote: String = "Best Quality",
    val mediaType: MediaType = MediaType.VIDEO,
    val targetPath: String = "",
    val fileSizeApprox: Long = 0,
    val progress: Float = 0f,
    val speed: String = "",
    val eta: String = "",
    val downloadedBytes: Long = 0,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
