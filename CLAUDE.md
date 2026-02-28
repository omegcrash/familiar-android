# Familiar Android — GrapheneOS Mobile App

- **Package**: `com.omegcrash.familiar` v1.4.0 (versionCode 9)
- **Language**: Kotlin + Jetpack Compose
- **Min SDK**: 26 | **Target SDK**: 35 | **Compile SDK**: 35
- **Java**: 17
- **Python bridge**: Chaquopy 16.0.0 (Python 3.11)
- **License**: MIT

## Structure

```
app/src/main/
  java/com/omegcrash/familiar/
    FamiliarApp.kt          # Application class
    MainActivity.kt         # Single-activity entry point
    service/                # FamiliarService (foreground service)
    python/                 # PythonBridge (Chaquopy interop)
    ui/
      theme/                # Material3 theme
      screens/              # Compose screens
      components/           # Reusable composables
      navigation/           # Nav graph
    data/                   # DataStore, repositories
    notifications/          # Notification channels
  python/
    start_familiar.py       # Python entry point for Chaquopy
  res/                      # Drawables, values, XML resources
app/src/test/               # Unit tests (JUnit)
build.gradle.kts            # App-level build config
```

## Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run Android instrumented tests
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

## Key conventions

- **Architecture**: Compose UI -> OkHttp -> localhost:5000 -> Flask dashboard -> Agent
- **ABI filters**: arm64-v8a, x86_64 only
- **Python dep**: `familiar-agent[llm,mesh]>=1.10.2` (installed via Chaquopy pip)
- **Compose BOM**: 2024.12.01
- **Kotlin**: 2.1.0, AGP 8.7.3
- **Distribution**: GitHub Releases (no Google Play)
- **Release builds**: minified + shrunk resources (R8)
