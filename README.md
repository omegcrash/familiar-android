# Familiar for Android

Native Android app for [Familiar](https://github.com/omegcrash/familiar) — your self-hosted AI companion. Built for GrapheneOS and privacy-first Android users.

## What This Is

A native Android wrapper around the Familiar Python agent. The Python package runs unchanged inside the app via [Chaquopy](https://chaquo.com/chaquopy/). The app provides:

- Material 3 chat interface (Jetpack Compose)
- Foreground service keeping the agent alive
- First-run setup for API keys (stored on-device only)
- Agent status and skill monitoring
- Android notifications for proactive briefings

**Your data stays on your device.** No cloud relay, no telemetry.

## Architecture

```
Android App (Kotlin + Compose)
  └─ FamiliarService (foreground service)
       └─ Chaquopy Python 3.11
            └─ familiar-agent (from PyPI, unchanged)
                 └─ Flask dashboard on localhost:5000
```

The Kotlin UI talks to the Flask API over `http://127.0.0.1:5000`. All data lives in app-private storage.

## Requirements

- Android 8.0+ (API 26) — covers all Pixel devices supported by GrapheneOS
- An LLM API key (Anthropic, OpenAI) or a reachable Ollama instance

## Build

```bash
git clone https://github.com/omegcrash/familiar-android.git
cd familiar-android
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/`.

## Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or download a signed APK from [GitHub Releases](https://github.com/omegcrash/familiar-android/releases).

## First Run

1. Open the app
2. Select your LLM provider (Anthropic / OpenAI / Ollama)
3. Enter your API key (stored locally, never transmitted)
4. Chat with your Familiar

## Distribution

- **GitHub Releases** — signed APKs on every tagged version
- **No Google Play** — aligns with GrapheneOS philosophy
- **F-Droid** — planned for future release

## License

MIT — see [LICENSE](LICENSE).
