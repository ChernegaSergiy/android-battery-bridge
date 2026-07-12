# PHP Battery Bridge (Android)

[![Build APK](https://github.com/ChernegaSergiy/android-battery-bridge/actions/workflows/build.yml/badge.svg)](https://github.com/ChernegaSergiy/android-battery-bridge/actions/workflows/build.yml)

A specialized Android application that exposes real-time battery information to PHP extensions via IPC. It implements a simple TCP Socket Server and sends battery data (level, charging status, health, temperature, voltage, technology) in JSON format when a client connects. This bridge is designed for PHP applications running on Android (like Termux) that need to monitor or react to battery state changes.

## Architecture

This implements a direct TCP Server approach:

1. **Android App runs a local TCP server** on port `8765` (127.0.0.1 only)
2. **PHP connects** to `127.0.0.1:8765`
3. **Android App sends** the battery data in JSON format and immediately closes the connection.

## Components

- **MainActivity** - Minimal UI to register the app in launcher
- **BatteryService** - A background service that hosts the TCP server and provides battery data.
- **BatteryReceiver** - (Legacy) Receives broadcasts and returns battery data via reverse socket connection.

## IPC Protocol

Simply connect via TCP to `127.0.0.1` on port `8765`.

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

- Android SDK (`ANDROID_HOME`)
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

After installing the APK, open the app once to register it in the launcher. The app will start a background service listening for TCP connections on port 8765.

This bridge is primarily designed to be consumed by the [battery_info PHP extension](https://github.com/ChernegaSergiy/battery-php-ext), which will automatically connect to this port to read the device's battery status when running in CLI environments like Termux.

## Contributing

Contributions are welcome and appreciated! Here's how you can contribute:

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please make sure to update tests as appropriate and adhere to the existing coding style.

## License

This project is licensed under the CSSM Unlimited License v2.0 (CSSM-ULv2). See the [LICENSE](LICENSE) file for details.
