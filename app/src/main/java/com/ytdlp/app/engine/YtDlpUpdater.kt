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
        try {
            YoutubeDL.getInstance().version(context) ?: "Unknown"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get yt-dlp version", e)
            "Not initialized"
        }
    }

    suspend fun updateEngine(context: Context, channel: UpdateChannel = UpdateChannel._STABLE): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val status = YoutubeDL.getInstance().updateYoutubeDL(context, channel)
                val newVersion = YoutubeDL.getInstance().version(context) ?: "Updated"
                Log.d(TAG, "yt-dlp update status: $status, version: $newVersion")
                Result.success(newVersion)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update yt-dlp engine", e)
                Result.failure(e)
            }
        }
}
