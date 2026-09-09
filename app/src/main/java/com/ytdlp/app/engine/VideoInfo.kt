package com.ytdlp.app.engine

data class VideoInfo(
    val url: String,
    val id: String,
    val title: String,
    val uploader: String,
    val channelUrl: String = "",
    val thumbnailUrl: String,
    val durationSeconds: Long,
    val viewCount: Long = 0,
    val description: String = "",
    val extractor: String = "",
    val formats: List<DownloadFormat> = emptyList()
)

data class DownloadFormat(
    val formatId: String,
    val extension: String,
    val resolution: String,
    val note: String,
    val isAudioOnly: Boolean = false,
    val fileSizeApprox: Long = 0L,
    val fps: Int? = null,
    val vcodec: String? = null,
    val acodec: String? = null
)
