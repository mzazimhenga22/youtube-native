# YouTube Native (Android TV)

A sophisticated YouTube TV client clone built for Android TV using modern Android development practices.

## 🚀 Features

- **Profile Management**: Multi-profile support with distinct "Regular" and "Kids" modes.
- **Immersive Kids Mode**: A specialized, colorful UI for children with animated backgrounds and simplified navigation.
- **Smart Navigation**: Sidebar-based navigation optimized for TV D-pad controllers.
- **Seamless Playback**: High-performance video playback using Media3 (ExoPlayer), including support for HLS and DASH.
- **Global Playback State**: Watch videos while browsing other sections with an integrated MiniPlayer.
- **Library & History**: Track watch history and manage liked videos using local Room database.
- **Search & Discovery**: Robust search functionality and category-based browsing (Trending, Music, Movies).
- **Ambient Mode**: Beautiful screensaver mode that activates during inactivity.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose for TV](https://developer.android.com/tv/compose) (Material 3)
- **Architecture**: MVVM with Clean Architecture principles
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
- **Asynchronous Flow**: Kotlin Coroutines & Flow
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & OkHttp
- **Serialization**: [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- **Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Media**: [Media3 ExoPlayer](https://developer.android.com/guide/topics/media/media3)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Authentication**: [Firebase Auth](https://firebase.google.com/docs/auth)

## 🏗 Building

To build the project, ensure you have the Android SDK configured.

### Debug Build
```bash
cd android-native
./gradlew assembleDebug
```

### Release Build
```bash
cd android-native
./gradlew assembleRelease
```

The APKs will be available in `android-native/app/build/outputs/apk/`.
