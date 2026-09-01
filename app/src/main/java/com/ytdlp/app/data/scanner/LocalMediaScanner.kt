package com.ytdlp.app.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.ytdlp.app.data.local.DownloadEntity
import com.ytdlp.app.data.local.DownloadStatus
import com.ytdlp.app.data.local.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocalMediaScanner {

    suspend fun scanLocalAudio(context: Context): List<DownloadEntity> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<DownloadEntity>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val title = c.getString(titleCol) ?: "Unknown Song"
                    val artist = c.getString(artistCol) ?: "Unknown Artist"
                    val durationMs = c.getLong(durationCol)
                    val path = c.getString(dataCol) ?: ""
                    val albumId = c.getLong(albumIdCol)

                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    audioList.add(
                        DownloadEntity(
                            id = id + 1000000L,
                            url = path,
                            title = title,
                            uploader = artist,
                            thumbnailUrl = artworkUri,
                            durationSeconds = durationMs / 1000,
                            formatId = "local_audio",
                            mediaType = MediaType.AUDIO,
                            status = DownloadStatus.COMPLETED,
                            progress = 100f,
                            targetPath = path
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        audioList
    }

    suspend fun scanLocalVideos(context: Context): List<DownloadEntity> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<DownloadEntity>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val title = c.getString(titleCol) ?: "Unknown Video"
                    val artist = c.getString(artistCol) ?: "Local Storage"
                    val durationMs = c.getLong(durationCol)
                    val path = c.getString(dataCol) ?: ""

                    val videoUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    videoList.add(
                        DownloadEntity(
                            id = id + 2000000L,
                            url = path,
                            title = title,
                            uploader = artist,
                            thumbnailUrl = videoUri,
                            durationSeconds = durationMs / 1000,
                            formatId = "local_video",
                            mediaType = MediaType.VIDEO,
                            status = DownloadStatus.COMPLETED,
                            progress = 100f,
                            targetPath = path
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        videoList
    }
}
