package com.ytdlp.app

import android.app.Application
import android.util.Log
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.ytdlp.app.data.local.AppDatabase
import com.ytdlp.app.data.preferences.AppPreferences
import com.ytdlp.app.data.repository.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class YtDlpApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var preferences: AppPreferences
        private set
    lateinit var repository: DownloadRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        preferences = AppPreferences(this)
        repository = DownloadRepository(database.downloadDao(), preferences)

        // Initialize yt-dlp, FFmpeg, and Aria2c asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                YoutubeDL.getInstance().init(this@YtDlpApp)
                FFmpeg.getInstance().init(this@YtDlpApp)
                Aria2c.getInstance().init(this@YtDlpApp)
                Log.d("YtDlpApp", "yt-dlp, FFmpeg, and Aria2c successfully initialized")
            } catch (e: Exception) {
                Log.e("YtDlpApp", "Failed to initialize yt-dlp engine", e)
            }
        }
    }

    companion object {
        lateinit var instance: YtDlpApp
            private set
    }
}
