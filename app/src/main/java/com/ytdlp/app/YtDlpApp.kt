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

    private val _initError = MutableStateFlow<String?>(null)
    val initError: StateFlow<String?> = _initError.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        preferences = AppPreferences(this)
        repository = DownloadRepository(database.downloadDao(), preferences)

        initEngine()
    }

    fun initEngine() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = YtDlpEngine.ensureInitialized(this@YtDlpApp)
            result.fold(
                onSuccess = {
                    _isEngineReady.value = true
                    _initError.value = null
                    Log.d("YtDlpApp", "Engine initialization completed successfully.")
                },
                onFailure = { err ->
                    _isEngineReady.value = false
                    _initError.value = err.message
                    Log.e("YtDlpApp", "Engine initialization failed", err)
                }
            )
        }
    }

    companion object {
        lateinit var instance: YtDlpApp
            private set
    }
}
