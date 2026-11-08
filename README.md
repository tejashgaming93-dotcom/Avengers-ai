# AVENGERS AI (Tony) — Android, Kotlin + Jetpack Compose

Tony is a Gemini-powered voice/text assistant with phone control.
**Status: written but not compiled or device-tested by the author. Expect to fix a few build errors on first build.**

## What is implemented
- 8 screens: Home, AI Chat, Voice Assistant, Phone Control, Coding Studio, AI Settings, Chat History, About
- Gemini REST client behind an `AiProvider` interface; API key in EncryptedSharedPreferences; Test Connection; model chips + custom model id
- Tony persona (Hindi/English/Hinglish), extra persona instructions in Settings
- Voice chat: Android SpeechRecognizer (STT) + TextToSpeech, language selectable
- Phone control via intents: open apps, YouTube search, settings, camera, gallery, flashlight, battery, date/time
- Optional AccessibilityService (user enables it): Back, Home, Recents, scroll up/down. It does not read screen content.
- Chat history (local JSON), delete one/all; Coding Studio with code blocks + Copy

## NOT implemented yet
Wake word "Hey Tony", syntax highlighting, reminders, weather, web search, translation, image/screen understanding, file manager, voice picker (only language), Room DB.

## Architecture
`ui/` Compose screens -> `vm/` ViewModels (StateFlow) -> `ai/ChatRepository` -> `AiProvider` (`GeminiProvider`)
`phone/ActionExecutor` runs actions Tony requests as `[[ACTION:{...}]]` tags. The model only *asks*; the app validates against a fixed allow-list.
`data/` SettingsStore (encrypted key), ChatStore (JSON). `voice/VoiceEngine` STT+TTS.

## Permissions
INTERNET, ACCESS_NETWORK_STATE, RECORD_AUDIO (asked only when you tap the mic).
Flashlight needs no permission. Accessibility is enabled manually by the user in system Settings.

## Build option A (easiest from phone): GitHub Actions
1. Create a GitHub repo, upload this project (zip contents), push.
2. Actions tab -> "Build APK" -> download `avengers-ai-debug-apk` artifact -> install.

## Build option B: Termux (may need tweaking)
```
pkg update && pkg install openjdk-17 gradle aapt2 git
# You also need an Android SDK (platform 34 + build-tools) on the phone, e.g. from a Termux SDK package/script.
export ANDROID_HOME=$HOME/android-sdk
echo "sdk.dir=$ANDROID_HOME" > local.properties
# uncomment android.aapt2FromMavenOverride in gradle.properties (path from: which aapt2)
gradle assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`. Gradle on ARM Termux is slow and memory-hungry; if it fails use option A.

## First run
Settings -> paste Gemini API key (from aistudio.google.com) -> Save -> Test connection.
