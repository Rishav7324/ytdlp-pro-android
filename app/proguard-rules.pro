# youtubedl-android uses reflection/JNI and dynamically loads bundled
# Python/yt-dlp/FFmpeg/Aria2 components. Keep the full vendor namespace in
# release builds so R8 cannot break runtime initialization or extraction.
-keep class com.yausername.** { *; }
-dontwarn com.yausername.**

# Apache Commons Compress is used by the bundled yt-dlp runtime.
-keep class org.apache.commons.compress.archivers.zip.** { *; }
-dontwarn org.apache.commons.compress.**

# Keep Room database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep app engine/model classes that are accessed by callbacks/reflection.
-keep class com.ytdlp.app.engine.** { *; }
-keep class com.ytdlp.app.data.local.** { *; }
