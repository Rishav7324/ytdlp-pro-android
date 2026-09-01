package com.ytdlp.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ytdlp.app.YtDlpApp
import com.ytdlp.app.data.local.DownloadEntity
import com.ytdlp.app.data.local.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class LibraryFilter {
    ALL,
    VIDEOS,
    AUDIO
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as YtDlpApp).repository

    private val _filter = MutableStateFlow(LibraryFilter.ALL)
    val filter: StateFlow<LibraryFilter> = _filter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val completedDownloads: StateFlow<List<DownloadEntity>> = combine(
        repository.completedDownloads,
        _filter,
        _searchQuery
    ) { downloads, filter, query ->
        downloads.filter { item ->
            val matchesFilter = when (filter) {
                LibraryFilter.ALL -> true
                LibraryFilter.VIDEOS -> item.mediaType == MediaType.VIDEO
                LibraryFilter.AUDIO -> item.mediaType == MediaType.AUDIO
            }
            val matchesQuery = query.isBlank() || item.title.contains(query, ignoreCase = true) || item.uploader.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: LibraryFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteDownload(id: Long, deleteFileFromDisk: Boolean = true) {
        viewModelScope.launch {
            val item = repository.getDownloadById(id)
            if (deleteFileFromDisk && item?.targetPath?.isNotBlank() == true) {
                try {
                    val f = File(item.targetPath)
                    if (f.exists()) f.delete()
                } catch (e: Exception) {
                    // Ignore
                }
            }
            repository.deleteDownload(id)
        }
    }

    fun clearAllCompleted() {
        viewModelScope.launch {
            repository.clearCompleted()
        }
    }
}
