package com.ytdlp.app

import android.app.Application
import android.util.Log
import com.ytdlp.app.data.local.AppDatabase
import com.ytdlp.app.data.preferences.AppPreferences
import com.ytdlp.app.data.repository.DownloadRepository
import com.ytdlp.app.engine.YtDlpEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class YtDlpApp : Application() {
    val database: AppDatabase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AppDatabase.getInstance(this) }
    val preferences: AppPreferences by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AppPreferences(this) }
    val repository: DownloadRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { DownloadRepository(database.downloadDao(), preferences) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _isEngineReady = MutableStateFlow(false)
    val isEngineReady: StateFlow<Boolean> = _isEngineReady.asStateFlow()
    private val _initError = MutableStateFlow<String?>(null)
    val initError: StateFlow<String?> = _initError.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Keep process startup free of Room and native yt-dlp/FFmpeg/Aria2 work.
        // Heavy components are created only when a feature actually needs them.
    }

    fun initEngine() {
        appScope.launch {
            val result = YtDlpEngine.ensureInitialized(this@YtDlpApp)
            result.fold(
                onSuccess = { _isEngineReady.value = true; _initError.value = null },
                onFailure = { error ->
                    _isEngineReady.value = false
                    _initError.value = error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
                    Log.e("YtDlpApp", "yt-dlp engine initialization failed", error)
                }
            )
        }
    }

    suspend fun updateEngine(): Result<Unit> = YtDlpEngine.updateEngine(this).map { }

    companion object {
        lateinit var instance: YtDlpApp
            private set
    }
}
