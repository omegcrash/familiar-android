# Familiar for Android — Living Audit Document

**Last updated:** 2026-02-27
**Current version:** 1.1.2 (versionCode 4)
**Maintained by:** George Scott Foley

This document is the single source of truth for known issues, intentional stubs,
architectural decisions, and the gap between what exists and what's wired.
Update it when you add a TODO, complete a stub, or make a decision you'll want
to remember next session.

---

## How to Use This Document

**Starting a session:** Read this first. It saves reconstructing context from code.

**Ending a session:** Add anything that's intentionally incomplete, any architectural
decision made, any new TODO introduced. If you completed something, mark it ✅ and
add the version it landed in.

**Marker conventions in code:**

| Marker | Meaning |
|--------|---------|
| `# TODO:` | Planned but not started |
| `# STUB:` | Structure exists, execution path not yet implemented |
| `# FIXME:` | Known bug, not yet fixed |
| `# INTENTIONAL:` | Looks wrong but isn't — explains why |

---

## Version Status

### Current: v1.1.2
Bumped `familiar-agent[llm,mesh]>=1.8.6` for Security category in /connect
wizard: Vaultwarden (URL+token+test), Encryption, PHI Detection, RBAC, and
User Management onboarding wizards. Also includes contextual error handling
via `format_http_error` in the Vaultwarden skill.

### Previous: v1.1.1
Bumped `familiar-agent[llm,mesh]>=1.8.5` for onboarding UX fixes (interactive
email menu, calendar test button fix, Proton VPN buttons) and contextual error
handling across all self-hosted skills.

### Previous: v1.1.1 (with familiar-agent 1.8.4)
Bumped `familiar-agent[llm,mesh]>=1.8.4` for improved LLM tool selection guidance
in self-hosted skills.

### Previous: v1.1.0
Pinned `familiar-agent[llm,mesh]>=1.5.0` via Chaquopy. Added Ollama setup with
Termux install prompt and supported local model list. 4 commits on `master`.

### Previous: v1.0.0 (Initial Commit)
GrapheneOS-targeted Android app with Compose UI, foreground service,
Chaquopy Python bridge, first-run setup wizard, and CI/CD pipeline.

---

## Known Bugs

### Settings: Hardcoded Version String
**File:** `app/src/main/java/com/omegcrash/familiar/ui/screens/SettingsScreen.kt` L96
**Bug:** Displays "Familiar for Android v1.0.0" — should be "v1.1.2" (or read from
`BuildConfig.VERSION_NAME` to stay in sync with `build.gradle.kts`).
**Priority:** Low — cosmetic only.

---

## Known Intentional Stubs

None. All features are fully implemented.

---

## Known Technical Debt

### Dual JSON Libraries
**Files:** `app/build.gradle.kts` L95–96
**Issue:** Both `kotlinx-serialization-json` (1.7.3) and `gson` (2.11.0) are declared
as dependencies. `FamiliarClient.kt` uses Gson exclusively. kotlinx-serialization
is unused — likely leftover from initial scaffolding.
**Fix:** Remove `kotlinx-serialization-json` dependency unless future code needs it.

### Release Signing Config
**File:** `app/build.gradle.kts` L43
**Issue:** Release build type uses `signingConfigs.getByName("debug")` as a fallback.
CI overrides this with secrets, but local release builds silently use the debug key.
A dedicated release signing config with proper error messaging would be clearer.

### Backup Includes API Keys
**File:** `app/src/main/res/xml/backup_rules.xml` L4
**Issue:** `<include domain="sharedpref" path="." />` backs up all SharedPreferences
including API keys to Android Backup Service (Google cloud). DataStore preferences
are file-based and included via `sharedpref` domain. On GrapheneOS this is typically
disabled, but on stock Android this could leak API keys to Google.
**Mitigation:** Consider excluding DataStore files from backup, or using
`android:allowBackup="false"` and relying on manual export/import.

### Test Coverage
**Files:** `app/src/test/java/com/omegcrash/familiar/`
**Status:** 7 unit tests across 2 files. Tests cover error handling (client against
dead server) and state machine correctness. No tests for:
- PreferencesStore (DataStore operations)
- PythonBridge (Chaquopy lifecycle)
- Notification channel creation
- Navigation routing logic
- UI composables (no Compose UI tests)
**Note:** Android instrumented tests require a device/emulator. Unit tests are
sufficient for the current codebase size.

---

## Architecture Overview

```
Familiar for Android v1.1.2 — Privacy-First Mobile AI Agent
├── app/src/main/
│   ├── AndroidManifest.xml          — 6 permissions, foreground svc, boot receiver
│   ├── java/com/omegcrash/familiar/
│   │   ├── FamiliarApp.kt           — Application class, notification channels (11 lines)
│   │   ├── MainActivity.kt          — Edge-to-edge Compose, starts service (31 lines)
│   │   ├── data/
│   │   │   ├── FamiliarClient.kt    — OkHttp REST client for localhost:5000 (86 lines)
│   │   │   └── PreferencesStore.kt  — DataStore prefs: provider, key, model (62 lines)
│   │   ├── service/
│   │   │   ├── FamiliarService.kt   — Foreground svc, Python lifecycle, wake lock (95 lines)
│   │   │   ├── ServiceState.kt      — Sealed class: Idle/Starting/Running/Error/Stopped (9 lines)
│   │   │   └── BootReceiver.kt      — Auto-start on BOOT_COMPLETED (14 lines)
│   │   ├── python/
│   │   │   └── PythonBridge.kt      — Chaquopy init/start/stop, thread-safe (39 lines)
│   │   ├── notifications/
│   │   │   └── NotificationHelper.kt — Service + briefings channels (80 lines)
│   │   └── ui/
│   │       ├── theme/Theme.kt       — Material 3, dynamic color (57 lines)
│   │       ├── navigation/NavGraph.kt — 4 routes: Setup/Chat/Status/Settings (65 lines)
│   │       ├── screens/
│   │       │   ├── SetupScreen.kt   — First-run wizard: provider/key/model (236 lines)
│   │       │   ├── ChatScreen.kt    — Message list, input, tool cards (193 lines)
│   │       │   ├── StatusScreen.kt  — Agent info + skills list (118 lines)
│   │       │   └── SettingsScreen.kt — Config review + factory reset (140 lines)
│   │       └── components/
│   │           ├── MessageBubble.kt — User/agent chat bubbles (52 lines)
│   │           ├── StatusBar.kt     — Service state indicator (54 lines)
│   │           └── ToolCallCard.kt  — Expandable skill execution card (69 lines)
│   ├── python/
│   │   ├── start_familiar.py        — Agent + Flask bootstrap (42 lines)
│   │   └── requirements.txt         — familiar-agent[llm,mesh]>=1.8.6
│   └── res/
│       ├── drawable/ic_familiar.xml  — Adaptive icon ("F" letterform)
│       ├── values/strings.xml        — String resources
│       ├── values/themes.xml         — Theme reference
│       └── xml/
│           ├── network_security.xml  — Cleartext localhost only
│           └── backup_rules.xml      — Prefs + .familiar/, exclude logs/cache
├── app/src/test/
│   └── java/com/omegcrash/familiar/
│       ├── FamiliarClientTest.kt     — 3 tests: error handling (30 lines)
│       └── ServiceStateTest.kt       — 4 tests: state machine (39 lines)
├── .github/workflows/build.yml      — Lint + debug APK + signed release on tags
├── build.gradle.kts                  — AGP 8.7.3, Kotlin 2.1.0, Chaquopy 16.0.0
└── gradle/wrapper/ (Gradle 8.11.1)
```

**Total:** 1,410 lines Kotlin + 42 lines Python = ~1,450 lines of source code.
**Tests:** 7 unit tests (2 files).

---

## Architectural Decisions (Recorded)

### Local-Only Architecture
All communication between Compose UI and the Python agent goes through
`localhost:5000`. No cloud relay, no telemetry, no external API calls from the
Android layer. API keys are sent only to the user's chosen LLM provider by the
Python agent, never by the Android app itself.

### Chaquopy for Python Bridge
Chose Chaquopy 16.0.0 over alternatives (embedded Python, Kivy, BeeWare) because
it integrates directly with the Gradle build system, handles native library
bundling for arm64/x86_64, and allows importing `familiar-agent` from PyPI
at build time. Playwright excluded — not compatible with Android.

### Foreground Service + Wake Lock
Python agent runs in a foreground service with `PARTIAL_WAKE_LOCK` to survive
backgrounding. `START_STICKY` ensures the system restarts it after low-memory
kills. The persistent notification is required by Android for foreground services.

### Separate Thread for Python
Python runs on a dedicated Java thread (not a coroutine) because Chaquopy's
Python.start() blocks. The 3-second delay in `FamiliarService.startPython()`
allows Flask to bind before the service reports `Running`.

### StateFlow for Service Lifecycle
`FamiliarService.state` is a companion-object `MutableStateFlow<ServiceState>`
observable from any composable via `collectAsState()`. This avoids binding
to the service or using broadcasts.

### DataStore over SharedPreferences
`PreferencesStore` uses Jetpack DataStore (file-backed, coroutine-native) rather
than legacy SharedPreferences. Keys are stored unencrypted in app-private storage —
acceptable because GrapheneOS provides full-disk encryption, and the app targets
privacy-focused users who control their device.

### Network Security: Cleartext Localhost Only
`network_security.xml` allows HTTP only to `127.0.0.1` and `localhost`. All other
domains require HTTPS. This is the minimum needed for the Flask API bridge.

### Distribution: GitHub Releases Only
No Google Play — the target audience (GrapheneOS users) avoids Google services.
APKs are signed in CI and published as GitHub Release assets. F-Droid planned
for future.

---

## CI Status

GitHub Actions pipeline configured in `.github/workflows/build.yml`.
Runs on push to master, tags matching `v*`, and pull requests to master.

**Repo:** `omegcrash/familiar-android`
**Build tools:** Java 17 (Temurin), Gradle 8.11.1
**Jobs:** Lint, Build Debug (always), Build Release (tags only)
**Release:** Signed APK via secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
`KEY_ALIAS`, `KEY_PASSWORD`) + GitHub Release with generated notes.

---

## Security & Privacy Summary

| Concern | Status |
|---------|--------|
| Data exfiltration | No external network calls from Android layer |
| API key storage | DataStore (app-private), unencrypted on disk, FDE expected |
| Network cleartext | Localhost only, HTTPS required for all other domains |
| Backup leakage | SharedPrefs backed up — risk on non-GrapheneOS (see tech debt) |
| Permissions | 6 total, all justified (see manifest) |
| Service exposure | FamiliarService `exported="false"`, no IPC surface |
| Boot receiver | `exported="false"`, restricted to BOOT_COMPLETED |

---

## Open Questions

1. **Backup vs API key leakage:** Should `android:allowBackup` be set to `false`
   to prevent API keys reaching Google's backup service on stock Android?
2. **Version string source of truth:** Should `SettingsScreen` read from
   `BuildConfig.VERSION_NAME` instead of hardcoding?
