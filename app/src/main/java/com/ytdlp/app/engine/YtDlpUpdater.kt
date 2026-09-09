package com.ytdlp.app.engine

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDL.UpdateChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object YtDlpUpdater {
    private const val TAG = "YtDlpUpdater"

    /**
     * Version/update must depend only on the yt-dlp core. FFmpeg and Aria2 are
     * optional features and must never block the Update button.
     */
    private fun ensureYtDlpCore(context: Context) {
        YoutubeDL.getInstance().init(context.applicationContext)
    }

    suspend fun getVersion(context: Context): String = withContext(Dispatchers.IO) {
        runCatching {
            ensureYtDlpCore(context)
            val version = YoutubeDL.getInstance().version(context)?.takeIf { it.isNotBlank() }
            val name = YoutubeDL.getInstance().versionName(context)?.takeIf { it.isNotBlank() }
            version ?: name ?: "Bundled yt-dlp"
        }.getOrElse { error ->
            Log.e(TAG, "Failed to initialize/read yt-dlp version", error)
            "Engine unavailable"
        }
    }

    suspend fun updateEngine(
        context: Context,
        channel: UpdateChannel = UpdateChannel.STABLE
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            // Do NOT call YtDlpEngine.ensureInitialized() here. That method also
            // initializes FFmpeg/Aria2, and an optional native-library failure must
            // not prevent the core yt-dlp binary from being updated.
            ensureYtDlpCore(context)
            val status = YoutubeDL.getInstance().updateYoutubeDL(context.applicationContext, channel)
            val version = YoutubeDL.getInstance().version(context)?.takeIf { it.isNotBlank() }
            val versionName = YoutubeDL.getInstance().versionName(context)?.takeIf { it.isNotBlank() }
            when (status) {
                YoutubeDL.UpdateStatus.DONE -> version ?: versionName ?: "Updated"
                YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE -> version ?: versionName ?: "Already up to date"
                null -> version ?: versionName ?: "Update completed"
            }.also { result ->
                Log.d(TAG, "yt-dlp stable update status=$status, version=$result")
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Log.e(TAG, "yt-dlp stable update failed", error)
                Result.failure(error)
            }
        )
    }
}
