package com.zyvro.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zyvro.app.YtDlpApp
import com.zyvro.app.data.local.DownloadEntity
import com.zyvro.app.data.local.MediaType
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
    AUDIO,
    FAVORITES,
    TOP
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
                LibraryFilter.FAVORITES -> item.isFavorite
                LibraryFilter.TOP -> item.playCount > 0
            }
            val matchesQuery = query.isBlank() || item.title.contains(query, ignoreCase = true) || item.uploader.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }.let { list ->
            if (filter == LibraryFilter.TOP) list.sortedByDescending { it.playCount } else list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritesCount: StateFlow<Int> = repository.favorites
        .combine(kotlinx.coroutines.flow.flowOf(Unit)) { list, _ -> list.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val playlists = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val prefs = (application as YtDlpApp).preferences
    val deviceFavorites = prefs.deviceFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleDeviceFavorite(path: String) {
        viewModelScope.launch { prefs.toggleDeviceFavorite(path) }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch { repository.toggleFavorite(id) }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.createPlaylist(name) }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch { repository.deletePlaylist(id) }
    }

    fun addToPlaylist(playlistId: Long, downloadId: Long) {
        viewModelScope.launch { repository.addSongToPlaylist(playlistId, downloadId) }
    }

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
