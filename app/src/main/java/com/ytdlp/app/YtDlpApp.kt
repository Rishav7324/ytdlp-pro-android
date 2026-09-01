package com.ytdlp.app

import android.app.Application
import android.util.Log
import com.ytdlp.app.data.local.AppDatabase
import com.ytdlp.app.data.preferences.AppPreferences
import com.ytdlp.app.data.repository.DownloadRepository
import com.ytdlp.app.engine.YtDlpEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class YtDlpApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var preferences: AppPreferences
        private set
    lateinit var repository: DownloadRepository
        private set

    private val _isEngineReady = MutableStateFlow(false)
    val isEngineReady: StateFlow<Boolean> = _isEngineReady.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        preferences = AppPreferences(this)
        repository = DownloadRepository(database.downloadDao(), preferences)

        // Initialize yt-dlp, FFmpeg, and Aria2c on startup
        CoroutineScope(Dispatchers.IO).launch {
            try {
                YtDlpEngine.ensureInitialized(this@YtDlpApp)
                _isEngineReady.value = true
                Log.d("YtDlpApp", "Engine initialization completed successfully.")
            } catch (e: Exception) {
                Log.e("YtDlpApp", "Failed to initialize engine on startup", e)
            }
        }
    }

    companion object {
        lateinit var instance: YtDlpApp
            private set
    }
}
