package com.ytdlp.app.util

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import com.ytdlp.app.data.local.MediaType
import java.io.File

object StorageHelper {
    private const val TAG = "StorageHelper"

    fun getPublicMediaDirectory(mediaType: MediaType): File {
        val folderName = if (mediaType == MediaType.AUDIO) {
            Environment.DIRECTORY_MUSIC
        } else {
            Environment.DIRECTORY_DOWNLOADS
        }
        val publicDir = File(Environment.getExternalStoragePublicDirectory(folderName), "yt-dlp")
        if (!publicDir.exists()) {
            publicDir.mkdirs()
        }
        return publicDir
    }

    fun exportToPublicStorage(
        context: Context,
        srcFile: File,
        mediaType: MediaType,
        title: String
    ): File {
        try {
            val publicDir = getPublicMediaDirectory(mediaType)
            val destFile = File(publicDir, srcFile.name)

            if (srcFile.absolutePath != destFile.absolutePath && srcFile.exists()) {
                Log.d(TAG, "Copying downloaded file from ${srcFile.absolutePath} to ${destFile.absolutePath}")
                srcFile.copyTo(destFile, overwrite = true)
            }

            // Trigger Android Media Scanner for immediate visibility in Music, Gallery & Files apps
            val mimeType = if (mediaType == MediaType.AUDIO) "audio/*" else "video/*"
            MediaScannerConnection.scanFile(
                context.applicationContext,
                arrayOf(destFile.absolutePath, publicDir.absolutePath),
                arrayOf(mimeType, null)
            ) { path, uri ->
                Log.d(TAG, "File successfully indexed in Android MediaStore: $path -> $uri")
            }

            return if (destFile.exists()) destFile else srcFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy to public directory, using source file directly", e)
            return srcFile
        }
    }
}
