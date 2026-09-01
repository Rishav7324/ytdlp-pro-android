# yt-dlp Pro for Android

A modern, high-performance Android application powered by **[yt-dlp](https://github.com/yt-dlp/yt-dlp)**, built with **Kotlin** and **Jetpack Compose (Material 3)**.

Designed for personal video and audio downloading with high-speed multi-connection acceleration (Aria2c), embedded FFmpeg processing, background foreground service, and dynamic Material You theming.

---

## ✨ Features

- 🚀 **Powered by yt-dlp Core**: Direct integration with yt-dlp supporting thousands of video platforms (YouTube, Twitter/X, Instagram, TikTok, Reddit, Bilibili, Facebook, Twitch, and more).
- 🎨 **Professional Material 3 UI**: Dynamic Material You theming, fluid bottom sheets, Dark & Light modes, and smooth 60/120 FPS animations.
- ⚡ **Aria2c Acceleration**: Multi-threaded, multi-connection downloading for maximum bandwidth utilization.
- 🎵 **Audio Extraction**: Download in high-quality **MP3 (320kbps)**, **M4A / AAC**, **FLAC**, or **OPUS** with automatic ID3 tagging and embedded album artwork.
- 🎥 **Video Quality Selection**: Choose from **4K (2160p)**, **1440p**, **1080p FHD**, **720p HD**, or **480p SD** with MP4/MKV container merging.
- 📲 **Android Share Sheet Integration**: Share video links directly from the YouTube app, Instagram, browser, or Twitter into yt-dlp Pro to download instantly.
- 🔔 **Foreground Notification Service**: Real-time progress bar, speed indicator (e.g. `8.5 MB/s`), and ETA countdown in the Android status bar.
- 🔄 **In-App yt-dlp Updater**: Update the yt-dlp extractor binary on the fly without needing to rebuild or reinstall the APK.
- 📁 **Media Library**: Manage downloaded videos and songs, open directly with VLC/media players, share files, or delete from disk.
- 🔑 **Authentication & Cookies**: Import Netscape `cookies.txt` format to access private, age-restricted, or membership-only videos.

---

## 🛠️ How to Build the APK on GitHub (Automated CI/CD)

You don't need Android Studio installed locally! GitHub Actions will compile and generate the APK automatically for you in the cloud.

### Step 1: Push this repository to your GitHub account
1. Create a new repository on [GitHub](https://github.com/new) (e.g., `ytdlp-android`).
2. Run the following commands in this directory:
```bash
git init
git add .
git commit -m "Initial commit of yt-dlp Pro Android App"
git branch -M main
git remote add origin https://github.com/<YOUR_USERNAME>/<YOUR_REPO_NAME>.git
git push -u origin main
```

### Step 2: Download your APK from GitHub Actions
1. Go to your GitHub repository in your browser.
2. Click on the **Actions** tab at the top.
3. Click on the latest workflow run named **"Build Android APK"**.
4. Scroll down to the **Artifacts** section and click **`YtDlp-Android-Debug-APK`** to download your APK!
5. Transfer and install the `.apk` file on your Android phone.

---

## 🏷️ Create a Release APK with Version Tags

To automatically create a GitHub Release with the APK attached:
```bash
git tag v1.0.0
git push origin v1.0.0
```
GitHub Actions will automatically build the APK and publish it directly to the **Releases** page of your repository.

---

## 💻 Local Building (Optional)

If you have JDK 21 and the Android SDK installed:

```bash
# Clone the repository
git clone https://github.com/<YOUR_USERNAME>/<YOUR_REPO_NAME>.git
cd ytdlp-android

# Build Debug APK
./gradlew assembleDebug

# The APK will be generated at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📂 Project Architecture

```
ytdlp-android/
├── .github/workflows/build-apk.yml  # Automated GitHub Actions APK Builder
├── app/
│   ├── build.gradle.kts             # Module configuration & dependencies
│   └── src/main/
│       ├── AndroidManifest.xml      # Permissions, Share Sheet Intent & Service
│       └── java/com/ytdlp/app/
│           ├── YtDlpApp.kt          # App lifecycle & Engine initialization
│           ├── data/                # Room DB, Preferences & Repository
│           ├── engine/              # yt-dlp wrapper, FFmpeg & Updater
│           ├── service/             # Foreground Download Service & Notifications
│           ├── ui/
│           │   ├── theme/           # Material 3 Colors & Typography
│           │   ├── components/      # VideoPreviewCard, FormatSheet, DownloadCard
│           │   ├── screens/         # Home, Queue, Library, Settings
│           │   └── MainActivity.kt  # Root Activity & Share intent receiver
│           └── viewmodel/           # Jetpack ViewModels (MVVM)
├── build.gradle.kts                 # Root Gradle build script
└── settings.gradle.kts              # Gradle plugin & repository settings
```

---

## 📄 License
Personal Use. Powered by yt-dlp, FFmpeg, and Aria2c.
