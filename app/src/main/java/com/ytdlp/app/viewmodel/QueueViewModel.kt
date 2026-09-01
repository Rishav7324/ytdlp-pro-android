package com.ytdlp.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ytdlp.app.YtDlpApp
import com.ytdlp.app.data.local.DownloadEntity
import com.ytdlp.app.service.DownloadService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QueueViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as YtDlpApp).repository

    val activeQueue: StateFlow<List<DownloadEntity>> = repository.activeAndQueuedDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cancelDownload(downloadId: Long) {
        DownloadService.cancelDownload(getApplication(), downloadId)
    }

    fun deleteDownload(downloadId: Long) {
        viewModelScope.launch {
            repository.deleteDownload(downloadId)
        }
    }
}
