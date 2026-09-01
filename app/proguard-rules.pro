# Keep youtubedl-android native JNI calls and python components
-keep class com.yausername.youtubedl_android.** { *; }
-dontwarn com.yausername.youtubedl_android.**

# Keep Room database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep models and entities
-keep class com.ytdlp.app.data.local.** { *; }
-keep class com.ytdlp.app.engine.** { *; }
