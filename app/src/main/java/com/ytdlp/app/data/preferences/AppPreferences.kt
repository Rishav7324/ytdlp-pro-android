package com.ytdlp.app.data.preferences

import android.content.Context
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ytdlp_settings")

class AppPreferences(private val context: Context) {

    companion object {
        val KEY_DOWNLOAD_PATH = stringPreferencesKey("download_path")
        val KEY_DEFAULT_VIDEO_QUALITY = stringPreferencesKey("default_video_quality")
        val KEY_DEFAULT_AUDIO_FORMAT = stringPreferencesKey("default_audio_format")
        val KEY_EMBED_THUMBNAIL = booleanPreferencesKey("embed_thumbnail")
        val KEY_EMBED_SUBTITLES = booleanPreferencesKey("embed_subtitles")
        val KEY_USE_ARIA2 = booleanPreferencesKey("use_aria2")
        val KEY_CONCURRENT_DOWNLOADS = intPreferencesKey("concurrent_downloads")
        val KEY_DARK_THEME_MODE = stringPreferencesKey("dark_theme_mode")
        val KEY_CUSTOM_ARGUMENTS = stringPreferencesKey("custom_arguments")
        val KEY_COOKIES_CONTENT = stringPreferencesKey("cookies_content")
    }

    private val defaultDownloadDir: String
        get() {
            val extDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            return extDir?.absolutePath ?: context.filesDir.absolutePath
        }

    val downloadPath: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DOWNLOAD_PATH] ?: defaultDownloadDir
    }

    val defaultVideoQuality: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_VIDEO_QUALITY] ?: "best"
    }

    val defaultAudioFormat: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_AUDIO_FORMAT] ?: "mp3"
    }

    val embedThumbnail: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMBED_THUMBNAIL] ?: true
    }

    val embedSubtitles: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMBED_SUBTITLES] ?: false
    }

    val useAria2: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_USE_ARIA2] ?: false
    }

    val concurrentDownloads: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_CONCURRENT_DOWNLOADS] ?: 2
    }

    val darkThemeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_THEME_MODE] ?: "SYSTEM"
    }

    val customArguments: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_ARGUMENTS] ?: ""
    }

    val cookiesContent: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_COOKIES_CONTENT] ?: ""
    }

    suspend fun setDownloadPath(path: String) {
        context.dataStore.edit { it[KEY_DOWNLOAD_PATH] = path }
    }

    suspend fun setDefaultVideoQuality(quality: String) {
        context.dataStore.edit { it[KEY_DEFAULT_VIDEO_QUALITY] = quality }
    }

    suspend fun setDefaultAudioFormat(format: String) {
        context.dataStore.edit { it[KEY_DEFAULT_AUDIO_FORMAT] = format }
    }

    suspend fun setEmbedThumbnail(enabled: Boolean) {
        context.dataStore.edit { it[KEY_EMBED_THUMBNAIL] = enabled }
    }

    suspend fun setEmbedSubtitles(enabled: Boolean) {
        context.dataStore.edit { it[KEY_EMBED_SUBTITLES] = enabled }
    }

    suspend fun setUseAria2(enabled: Boolean) {
        context.dataStore.edit { it[KEY_USE_ARIA2] = enabled }
    }

    suspend fun setConcurrentDownloads(count: Int) {
        context.dataStore.edit { it[KEY_CONCURRENT_DOWNLOADS] = count }
    }

    suspend fun setDarkThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_DARK_THEME_MODE] = mode }
    }

    suspend fun setCustomArguments(args: String) {
        context.dataStore.edit { it[KEY_CUSTOM_ARGUMENTS] = args }
    }

    suspend fun setCookiesContent(cookies: String) {
        context.dataStore.edit { it[KEY_COOKIES_CONTENT] = cookies }
    }
}
