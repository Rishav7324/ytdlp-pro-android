package com.ytdlp.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN ('QUEUED', 'DOWNLOADING', 'PAUSED') ORDER BY createdAt ASC")
    fun getActiveAndQueuedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY completedAt DESC, createdAt DESC")
    fun getCompletedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' AND mediaType = :mediaType ORDER BY completedAt DESC")
    fun getCompletedDownloadsByType(mediaType: MediaType): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: Long): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE status = 'QUEUED' ORDER BY createdAt ASC LIMIT 1")
    suspend fun getNextQueuedDownload(): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity): Long

    @Update
    suspend fun update(download: DownloadEntity)

    @Query("UPDATE downloads SET progress = :progress, speed = :speed, eta = :eta, downloadedBytes = :downloadedBytes, status = :status WHERE id = :id")
    suspend fun updateProgress(
        id: Long,
        progress: Float,
        speed: String,
        eta: String,
        downloadedBytes: Long,
        status: DownloadStatus
    )

    @Query("UPDATE downloads SET status = :status, completedAt = :completedAt, targetPath = :targetPath WHERE id = :id")
    suspend fun markCompleted(id: Long, status: DownloadStatus = DownloadStatus.COMPLETED, completedAt: Long = System.currentTimeMillis(), targetPath: String)

    @Query("UPDATE downloads SET status = :status, errorMessage = :error WHERE id = :id")
    suspend fun markFailed(id: Long, status: DownloadStatus = DownloadStatus.FAILED, error: String)

    @Delete
    suspend fun delete(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()
}
