package com.ytdlp.app.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ytdlp.app.data.local.DownloadEntity
import com.ytdlp.app.data.local.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

@OptIn(UnstableApi::class)
class MediaPlayerManager private constructor(context: Context) {

    val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()

    private val _currentMedia = MutableStateFlow<DownloadEntity?>(null)
    val currentMedia: StateFlow<DownloadEntity?> = _currentMedia.asStateFlow()

    private val _queue = MutableStateFlow<List<DownloadEntity>>(emptyList())
    val queue: StateFlow<List<DownloadEntity>> = _queue.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _isVideoExpanded = MutableStateFlow(false)
    val isVideoExpanded: StateFlow<Boolean> = _isVideoExpanded.asStateFlow()

    private val _isAudioSheetOpen = MutableStateFlow(false)
    val isAudioSheetOpen: StateFlow<Boolean> = _isAudioSheetOpen.asStateFlow()

    // A-B Loop
    private val _loopPointA = MutableStateFlow<Long?>(null)
    val loopPointA: StateFlow<Long?> = _loopPointA.asStateFlow()

    private val _loopPointB = MutableStateFlow<Long?>(null)
    val loopPointB: StateFlow<Long?> = _loopPointB.asStateFlow()

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) startProgressTracking() else stopProgressTracking()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    _duration.value = player.duration.coerceAtLeast(0L)
                    AudioFxManager.instance.initAudioEffects(player.audioSessionId)
                } else if (state == Player.STATE_ENDED) {
                    playNext()
                }
            }

            override fun onRepeatModeChanged(repeat: Int) {
                _repeatMode.value = repeat
            }
        })
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val pos = player.currentPosition.coerceAtLeast(0L)
                _currentPosition.value = pos
                _duration.value = player.duration.coerceAtLeast(0L)

                // A-B Loop Check
                val a = _loopPointA.value
                val b = _loopPointB.value
                if (a != null && b != null && b > a) {
                    if (pos >= b) {
                        player.seekTo(a)
                    }
                }

                delay(200)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
    }

    fun playMedia(entity: DownloadEntity, playlist: List<DownloadEntity> = emptyList(), openFullscreenIfVideo: Boolean = true) {
        val file = File(entity.targetPath)
        val uri = if (entity.targetPath.startsWith("content://")) {
            Uri.parse(entity.targetPath)
        } else if (file.exists()) {
            Uri.fromFile(file)
        } else if (entity.url.startsWith("http://") || entity.url.startsWith("https://")) {
            Uri.parse(entity.url)
        } else {
            return
        }

        _currentMedia.value = entity
        if (playlist.isNotEmpty()) {
            _queue.value = playlist
        } else if (!_queue.value.contains(entity)) {
            _queue.value = listOf(entity) + _queue.value
        }

        clearAbLoop()
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        _playbackSpeed.value = 1.0f
        player.playbackParameters = PlaybackParameters(1.0f)

        if (entity.mediaType == MediaType.VIDEO) {
            if (openFullscreenIfVideo) {
                _isVideoExpanded.value = true
            }
        } else {
            _isAudioSheetOpen.value = true
        }
    }

    fun playNext() {
        val q = _queue.value
        val current = _currentMedia.value ?: return
        val idx = q.indexOfFirst { it.id == current.id }
        if (idx != -1 && idx + 1 < q.size) {
            playMedia(q[idx + 1], q, openFullscreenIfVideo = false)
        } else if (_repeatMode.value == Player.REPEAT_MODE_ALL && q.isNotEmpty()) {
            playMedia(q[0], q, openFullscreenIfVideo = false)
        }
    }

    fun playPrevious() {
        val q = _queue.value
        val current = _currentMedia.value ?: return
        val idx = q.indexOfFirst { it.id == current.id }
        if (idx > 0) {
            playMedia(q[idx - 1], q, openFullscreenIfVideo = false)
        } else {
            seekTo(0L)
        }
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        if (_isShuffleEnabled.value) {
            _queue.value = _queue.value.shuffled()
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L)))
        _currentPosition.value = positionMs
    }

    fun seekForward(deltaMs: Long = 10000L) {
        seekTo(player.currentPosition + deltaMs)
    }

    fun seekRewind(deltaMs: Long = 10000L) {
        seekTo(player.currentPosition - deltaMs)
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        player.playbackParameters = PlaybackParameters(speed)
    }

    fun toggleRepeatMode() {
        val nextMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = nextMode
        _repeatMode.value = nextMode
    }

    // A-B Loop Functions
    fun setLoopPointA() {
        _loopPointA.value = player.currentPosition
    }

    fun setLoopPointB() {
        if (_loopPointA.value != null && player.currentPosition > _loopPointA.value!!) {
            _loopPointB.value = player.currentPosition
        }
    }

    fun clearAbLoop() {
        _loopPointA.value = null
        _loopPointB.value = null
    }

    fun setVideoExpanded(expanded: Boolean) {
        _isVideoExpanded.value = expanded
    }

    fun setAudioSheetOpen(open: Boolean) {
        _isAudioSheetOpen.value = open
    }

    fun closePlayer() {
        player.stop()
        _currentMedia.value = null
        _isVideoExpanded.value = false
        _isAudioSheetOpen.value = false
        clearAbLoop()
        AudioFxManager.instance.release()
        stopProgressTracking()
    }

    companion object {
        @Volatile
        private var instance: MediaPlayerManager? = null

        fun getInstance(context: Context): MediaPlayerManager {
            return instance ?: synchronized(this) {
                instance ?: MediaPlayerManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
