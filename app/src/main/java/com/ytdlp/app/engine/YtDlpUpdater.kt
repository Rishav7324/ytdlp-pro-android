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
            return@withContext "Unavailable"
        }

        runCatching {
            YoutubeDL.getInstance().version(context)?.takeIf { it.isNotBlank() } ?: "Unknown"
        }.getOrElse { error ->
            Log.e(TAG, "Failed to get yt-dlp version", error)
            "Unavailable"
        }
    }

    suspend fun updateEngine(
        context: Context,
        channel: UpdateChannel = UpdateChannel._STABLE
    ): Result<String> = withContext(Dispatchers.IO) {
        YtDlpEngine.ensureInitialized(context).fold(
            onSuccess = {
                runCatching {
                    val status = YoutubeDL.getInstance().updateYoutubeDL(context, channel)
                    val newVersion = YoutubeDL.getInstance().version(context)?.takeIf { it.isNotBlank() }
                        ?: "Updated"
                    Log.d(TAG, "yt-dlp update status: $status, version: $newVersion")
                    newVersion
                }
            },
            onFailure = { error ->
                Log.e(TAG, "Cannot update yt-dlp because engine initialization failed", error)
                Result.failure(error)
            }
        )
    }
}
