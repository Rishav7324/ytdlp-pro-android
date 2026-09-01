package com.ytdlp.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ytdlp.app.YtDlpApp
import com.ytdlp.app.data.local.DownloadEntity
import com.ytdlp.app.data.local.DownloadStatus
import com.ytdlp.app.data.local.MediaType
import com.ytdlp.app.engine.VideoInfo
import com.ytdlp.app.engine.YtDlpEngine
import com.ytdlp.app.service.DownloadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Idle : HomeUiState
    object Loading : HomeUiState
    data class Success(val videoInfo: VideoInfo) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as YtDlpApp).repository

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    val recentDownloads = repository.allDownloads

    fun onUrlChanged(newUrl: String) {
        _urlInput.value = newUrl
    }

    fun parseUrl(url: String? = null) {
        val targetUrl = (url ?: _urlInput.value).trim()
        if (targetUrl.isBlank()) return

        _urlInput.value = targetUrl
        _uiState.value = HomeUiState.Loading

        viewModelScope.launch {
            val result = YtDlpEngine.fetchVideoInfo(targetUrl)
            result.fold(
                onSuccess = { info ->
                    _uiState.value = HomeUiState.Success(info)
                },
                onFailure = { error ->
                    _uiState.value = HomeUiState.Error(error.message ?: "Failed to parse video info")
                }
            )
        }
    }

    fun startDownload(
        videoInfo: VideoInfo,
        formatId: String,
        mediaType: MediaType,
        audioExt: String,
        autoStart: Boolean = true
    ) {
        viewModelScope.launch {
            val download = DownloadEntity(
                url = videoInfo.url,
                title = videoInfo.title,
                uploader = videoInfo.uploader,
                thumbnailUrl = videoInfo.thumbnailUrl,
                durationSeconds = videoInfo.durationSeconds,
                formatId = formatId,
                mediaType = mediaType,
                status = DownloadStatus.QUEUED
            )

            val downloadId = repository.enqueueDownload(download)
            if (autoStart) {
                DownloadService.startDownload(getApplication(), downloadId)
            }
        }
    }

    fun cancelDownload(downloadId: Long) {
        DownloadService.cancelDownload(getApplication(), downloadId)
    }

    fun deleteDownload(downloadId: Long) {
        viewModelScope.launch {
            repository.deleteDownload(downloadId)
        }
    }

    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }
}
