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

val DownloadEntity.fileSize: Long
    get() = if (downloadedBytes > 0) downloadedBytes else fileSizeApprox

fun DownloadEntity.formattedFileSize(): String {
    val size = fileSize
    return when {
        size >= 1024L * 1024L * 1024L -> String.format(java.util.Locale.US, "%.2f GB", size / (1024.0 * 1024.0 * 1024.0))
        size >= 1024L * 1024L -> String.format(java.util.Locale.US, "%.1f MB", size / (1024.0 * 1024.0))
        size >= 1024L -> "${size / 1024L} KB"
        size > 0 -> "$size B"
        else -> ""
    }
}
