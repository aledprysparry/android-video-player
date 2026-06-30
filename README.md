# Video Player (Android)

A clean, basic local video player for Android, built on **Media3 / ExoPlayer**. It scans
the device's video library via `MediaStore`, lists each clip with a thumbnail, duration
and size, and plays the selected video full-screen with standard transport controls.
It also registers as an "Open with" handler for `video/*` files from other apps.

> This is an original app built with Google's Media3 player engine. It is **not** affiliated
> with VideoLAN / VLC and does not bundle libVLC.

## Features

- Device video library (newest first) via `MediaStore`, no raw storage paths
- Runtime permission handling (`READ_MEDIA_VIDEO` on Android 13+, falls back on older)
- Media3 player with play/pause, seek bar, fast-forward/rewind, full-screen, buffering spinner
- Immersive playback (system bars hidden), keep-screen-on, rotation-safe state restore
- Thumbnails via Glide; pull-to-refresh; empty + permission-needed states
- Material 3 theming, day/night aware

## Tech

| | |
|---|---|
| Language | Kotlin |
| UI | Android Views + ViewBinding + Material 3 |
| Playback | androidx.media3 (ExoPlayer) 1.4.1 |
| Min / Target SDK | 26 / 34 |
| Gradle / AGP / Kotlin | 8.9 / 8.5.2 / 1.9.24 |

## Build in the cloud (no local Android tooling)

Push this repo to GitHub. The workflow at `.github/workflows/android.yml` runs on every push
to `main` (and on demand via **Actions → Build APK → Run workflow**). It:

1. Sets up JDK 17 + Gradle 8.9
2. Generates the Gradle wrapper
3. Runs `./gradlew assembleDebug`
4. Uploads the result as an artifact named **`video-player-debug`**

Download the artifact from the workflow run, unzip it, and you'll have `app-debug.apk`.

### Install the APK on a phone

1. Copy `app-debug.apk` to your Android device.
2. Enable "Install unknown apps" for your file manager / browser when prompted.
3. Tap the APK to install. Grant the video permission on first launch.

> The APK is a **debug** build (debug-signed). Fine for personal sideloading. For Play Store
> distribution you'd add a release signing config and a `assembleRelease` step.

## Build locally (optional)

If you later install Android Studio / the SDK:

```bash
# first time only — creates the wrapper jar
gradle wrapper --gradle-version 8.9
./gradlew assembleDebug
```

Open the folder in Android Studio and hit Run to deploy to an emulator or device.

## Project layout

```
app/src/main/
  java/com/aledparry/player/
    MainActivity.kt      # library screen: permission + MediaStore list
    PlayerActivity.kt    # Media3 playback screen
    VideoRepository.kt   # MediaStore query
    VideoAdapter.kt      # RecyclerView list + Glide thumbnails
    VideoItem.kt         # data model
    Format.kt            # duration/size formatting
  res/layout/            # activity_main, activity_player, item_video
  AndroidManifest.xml
.github/workflows/android.yml   # cloud APK build
```
