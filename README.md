# NovaFetch

<p align="center">
  <strong>NovaFetch</strong><br>
  A modern, open-source Android media downloader powered by yt-dlp, FFmpeg, and Aria2c.
</p>

<p align="center">
  <a href="https://github.com/Rishav7324/ytdlp-pro-android/actions"><img src="https://img.shields.io/github/actions/workflow/status/Rishav7324/ytdlp-pro-android/build-apk.yml?branch=main&label=CI" alt="CI"></a>
  <a href="https://github.com/Rishav7324/ytdlp-pro-android/releases"><img src="https://img.shields.io/github/v/release/Rishav7324/ytdlp-pro-android?display_name=tag" alt="Release"></a>
  <a href="https://github.com/Rishav7324/ytdlp-pro-android/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-GPL--3.0--or--later-blue" alt="License"></a>
  <a href="https://github.com/Rishav7324/ytdlp-pro-android/stargazers"><img src="https://img.shields.io/github/stars/Rishav7324/ytdlp-pro-android" alt="Stars"></a>
</p>

NovaFetch is built with **Kotlin**, **Jetpack Compose**, **Material 3**, **yt-dlp**, **FFmpeg**, **Aria2c**, **Media3**, **Room**, and **DataStore**. The project is designed around a clean Android architecture, responsive UI, background downloads, and a polished liquid-glass inspired visual system.

> **Open source:** NovaFetch is licensed under **GNU GPL v3 or later**. Third-party components remain subject to their own licenses.

## ✨ Highlights

- 🎬 Download video and audio through yt-dlp.
- 🎚️ Quality and format selection with video/audio format metadata.
- 🎵 Audio extraction with configurable output formats.
- ⚡ Optional Aria2c acceleration for concurrent downloading.
- 🧰 FFmpeg-powered merging and audio processing.
- 📥 Background download queue with foreground-service progress.
- ▶️ Media3-based audio/video playback.
- 📚 Local media library.
- 🔗 Android share-sheet support.
- 🍪 User-controlled `cookies.txt` import support.
- 🔄 In-app yt-dlp engine update support.
- 🌗 Light/dark themes with a Nova aqua visual system.
- 🫧 Floating glass-style navigation and animated interactions.
- ♿ Accessibility-conscious Compose UI and responsive layouts.

## 🧱 Tech stack

| Area | Technology |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + repository/data layers |
| Navigation | Navigation Compose |
| Downloads | yt-dlp |
| Processing | FFmpeg |
| Acceleration | Aria2c |
| Playback | AndroidX Media3 |
| Persistence | Room + DataStore |
| Images | Coil |
| Build | Gradle Kotlin DSL |
| CI/CD | GitHub Actions |
| JDK | 21 |
| Android | minSdk 26 / targetSdk 35 |

## 🏗️ Architecture

```text
app/src/main/java/com/ytdlp/app/
├── data/
│   ├── local/          # Room database and DAOs
│   ├── preferences/    # DataStore preferences
│   └── repository/     # Application data boundary
├── engine/              # yt-dlp / FFmpeg / Aria2c integration
├── player/              # Media playback state and controls
├── service/             # Foreground download service
├── ui/
│   ├── browser/         # In-app browser
│   ├── components/      # Reusable Compose components
│   ├── navigation/      # Navigation graph and floating navigation
│   ├── player/          # Audio/video player UI
│   ├── screens/         # Home, Queue, Library, Settings
│   └── theme/           # Colors, shapes, typography, theme
├── viewmodel/            # Screen state and user actions
└── YtDlpApp.kt           # Application bootstrap
```

## 🚀 Build locally

### Requirements

- Android Studio with a recent Android SDK
- JDK 21
- Android SDK Platform 35
- Git

### Clone

```bash
git clone https://github.com/Rishav7324/ytdlp-pro-android.git
cd ytdlp-pro-android
```

### Debug build

```bash
./gradlew clean
./gradlew assembleDebug
```

APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Release build

```bash
./gradlew assembleRelease
```

The repository CI builds both Debug and Release variants automatically.

## 🤖 GitHub Actions

Every push to the development branch can run the Android build workflow. The pipeline performs a clean build, compiles Debug and Release APKs, and uploads the generated APKs as workflow artifacts.

Versioned releases can be created from tags such as:

```bash
git tag v2.0.0
git push origin v2.0.0
```

## 🔐 Security

Never commit:

- API keys
- signing keys or passwords
- private cookies
- authentication tokens
- personal access tokens
- private media or credentials

See [SECURITY.md](SECURITY.md) for vulnerability reporting.

## 🤝 Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

Good first contributions include:

- UI/UX improvements
- accessibility fixes
- playback improvements
- download reliability fixes
- tests
- documentation
- performance improvements

## 📋 Project status

NovaFetch is under active development. Features and internal APIs may change between releases. Release artifacts should be treated as experimental until a stable release is explicitly published.

## ⚖️ License

NovaFetch is free and open-source software licensed under the **GNU General Public License v3 or later**. See [LICENSE](LICENSE).

NovaFetch integrates third-party open-source software including yt-dlp, FFmpeg, Aria2c, AndroidX, Jetpack Compose, Room, DataStore, Media3, and Coil. Their respective licenses and notices continue to apply.

## 👤 Maintainer

**Rishav Raj** — [@Rishav7324](https://github.com/Rishav7324)

- GitHub: https://github.com/Rishav7324
- Repository: https://github.com/Rishav7324/ytdlp-pro-android

## 🙏 Acknowledgements

NovaFetch would not exist without the open-source projects that power it, especially:

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [FFmpeg](https://ffmpeg.org/)
- [Aria2](https://github.com/aria2/aria2)
- [AndroidX](https://developer.android.com/jetpack/androidx)
- [Jetpack Compose](https://developer.android.com/develop/ui/compose)
- [Media3](https://developer.android.com/media/media3)
- [Coil](https://github.com/coil-kt/coil)

See the project dependency manifests for the exact versions used by each build.
