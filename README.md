# PHP Battery Bridge (Android)

Android application that provides battery data to PHP extensions via IPC.

## Architecture

This implements a "Reverse Socket Bridge" pattern (Termux-style):

1. **PHP creates a local socket server** (127.0.0.1 only)
2. **PHP broadcasts Intent** with the port number
3. **Android BroadcastReceiver** receives the intent, reads battery data, and sends JSON back

## Components

- **MainActivity** - Minimal UI to register the app in launcher
- **BatteryReceiver** - Receives broadcasts and returns battery data via socket

## IPC Protocol

Broadcast Intent: `com.chernegasergiy.battery.GET_STATUS` with extra `remote_port`

Response JSON:
```json
{"l":85,"c":true,"s":"charging"}
```

| Field | Description |
|-------|-------------|
| `l` | Battery level (0-100) |
| `c` | Charging status (true/false) |
| `s` | Status string ("charging"/"discharging") |

## Building

### Prerequisites

- Android SDK (ANDROID_HOME)
- Gradle (optional, wrapper included)

### Build APK

```sh
cd android-battery-jni

# Using Gradle wrapper (recommended)
./gradlew assembleDebug

# Or using system Gradle
gradle assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Install on Device

```sh
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Usage

After installing the APK, open the app once to register it in the launcher. The app will listen for broadcasts from PHP extensions.

## License

This library is licensed under the CSSM Unlimited License v2.0 (CSSM-ULv2). See the [LICENSE](LICENSE) file for details.
