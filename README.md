# PHP Battery Bridge (Android)

[![Build APK](https://github.com/ChernegaSergiy/android-battery-bridge/actions/workflows/build.yml/badge.svg)](https://github.com/ChernegaSergiy/android-battery-bridge/actions/workflows/build.yml)

A specialized Android application that exposes real-time battery information to PHP extensions via IPC. It implements a Reverse Socket Bridge pattern where PHP creates a local socket server and receives battery data (level, charging status, health, temperature, voltage, technology) in JSON format. This bridge is designed for PHP applications running on Android that need to monitor or react to battery state changes.

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
{"l":85,"c":1,"h":1,"t":25,"v":4200,"tech":"Li-ion"}
```

| Field | Description |
|-------|-------------|
| `l` | Battery level (0-100) |
| `c` | Charging status (1/0) |
| `h` | Health status |
| `t` | Temperature (tenths of degree Celsius) |
| `v` | Voltage (mV) |
| `tech` | Battery technology string |

## Building

### Prerequisites

- Android SDK (ANDROID_HOME)
- Gradle (optional, wrapper included)

### Build APK

```sh
cd android-battery-bridge

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

## Contributing

Contributions are welcome and appreciated! Here's how you can contribute:

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please make sure to update tests as appropriate and adhere to the existing coding style.

## License

This library is licensed under the CSSM Unlimited License v2.0 (CSSM-ULv2). See the [LICENSE](LICENSE) file for details.
