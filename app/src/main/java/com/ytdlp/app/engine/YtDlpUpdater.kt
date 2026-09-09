package com.ytdlp.app.engine

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDL.UpdateChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object YtDlpUpdater {
    private const val TAG = "YtDlpUpdater"

    suspend fun getVersion(context: Context): String = withContext(Dispatchers.IO) {
        val initResult = YtDlpEngine.ensureInitialized(context)
        if (initResult.isFailure) {
            val error = initResult.exceptionOrNull()
            Log.e(TAG, "Cannot read yt-dlp version because engine initialization failed", error)
            return@withContext "Engine unavailable"
        }

        runCatching {
            val version = YoutubeDL.getInstance().version(context)?.takeIf { it.isNotBlank() }
            val name = YoutubeDL.getInstance().versionName(context)?.takeIf { it.isNotBlank() }
            version ?: name ?: "Bundled yt-dlp"
        }.getOrElse { error ->
            Log.e(TAG, "Failed to get yt-dlp version", error)
            "Bundled yt-dlp"
        }
    }

    suspend fun updateEngine(
        context: Context,
        channel: UpdateChannel = UpdateChannel.STABLE
    ): Result<String> = withContext(Dispatchers.IO) {
        YtDlpEngine.ensureInitialized(context).fold(
            onSuccess = {
                runCatching {
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
            },
            onFailure = { error ->
                Log.e(TAG, "Cannot update yt-dlp because engine initialization failed", error)
                Result.failure(error)
            }
        )
    }
}
