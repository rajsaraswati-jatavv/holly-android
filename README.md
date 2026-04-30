<div align="center">

# 💕 Holly Android

**An all-powerful, always-listening AI voice assistant with complete system-level access and a loving personality**

[![Platform](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-orange)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35%20(Android%2015)-blue)](https://developer.android.com)
[![YouTube](https://img.shields.io/badge/YouTube-T3rmuxk1ng-red?logo=youtube)](https://youtube.com/@T3rmuxk1ng)
[![Offline](https://img.shields.io/badge/Privacy-100%25%20Offline-brightgreen)](https://github.com/rajsaraswati-jatavv/holly-android)

*Built with 💚 by [T3rmuxk1ng](https://youtube.com/@T3rmuxk1ng)*

</div>

---

## 🎬 Demo & Tutorials

📺 **Watch on YouTube**: [https://youtube.com/@T3rmuxk1ng](https://youtube.com/@T3rmuxk1ng)

Subscribe for AI assistant tutorials, Android automation demos, and exclusive tool releases!

---

## ✨ Features

### 🎤 Voice Recognition
- **Wake Word Detection**: Say "Hey Holly" or just "Holly" to activate
- **Offline STT**: Vosk-based speech recognition for Hindi, English, Hinglish
- **Offline TTS**: Natural voice responses with Piper

### 💕 Holly Personality
- **Girlfriend Mode**: Loving, playful, romantic persona
- **Local LLM**: Llama 3 8B running on-device for conversations
- **Memory System**: Remembers your preferences and past conversations
- **3 Personality Levels**: Sweet / Romantic / Naughty

### 🔧 System Control
- **Full App Control**: Open, close, and interact with any app
- **Messaging Automation**: Send WhatsApp messages via voice
- **System Toggles**: WiFi, Bluetooth, flashlight, brightness, volume
- **Gesture Automation**: Tap, swipe, scroll, type automatically
- **Power Control**: Lock screen, screenshot, and more

### 🔒 Privacy-First
- **100% Offline**: All processing happens on your device
- **No Data Collection**: Nothing leaves your phone
- **Encrypted Storage**: Your data is secure

---

## 🗣️ Voice Commands

### App Control
```
"WhatsApp kholo"          → Opens WhatsApp
"Chrome kholo"            → Opens Chrome
"Settings kholo"          → Opens Settings
```

### Messaging
```
"Raj ko WhatsApp par 'Kal aaunga' bhejo"
```

### System
```
"WiFi on karo"            → Turns on WiFi
"Bluetooth band karo"     → Turns off Bluetooth
"Flashlight on karo"      → Turns on flashlight
"Volume badhao"           → Increases volume
"Brightness kam karo"     → Decreases brightness
```

### Time & Date
```
"Time batao"              → Tells current time
"Aaj tarikh kya hai"      → Tells today's date
"Alarm set karo 7 baje"   → Sets alarm for 7 AM
```

### Navigation & Power
```
"Home jao"                → Goes to home screen
"Back jao"                → Presses back
"Phone lock karo"         → Locks the screen
```

### Conversational
```
"I love you Holly"          → Holly responds lovingly
"Mujhe thoda mood nahi hai" → Holly provides emotional support
"Mujhe ek joke sunao"       → Holly tells a joke
```

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Wake Word | Porcupine (Picovoice) |
| STT | Vosk Android |
| TTS | Piper / System TTS |
| LLM | llama.cpp (quantized Llama 3) |
| Database | Room + SQLite |
| Automation | AccessibilityService |
| Background | Foreground Service + WorkManager |

---

## 📁 Project Structure

```
holly-android/
├── app/
│   ├── src/main/
│   │   ├── java/com/holly/assistant/
│   │   │   ├── admin/           # Device Admin
│   │   │   ├── data/            # Database & Models
│   │   │   ├── llm/             # LLM Engine (llama.cpp)
│   │   │   ├── receiver/        # Broadcast Receivers
│   │   │   ├── service/         # Core Services
│   │   │   ├── stt/             # Speech Recognition (Vosk)
│   │   │   ├── tts/             # Text-to-Speech (Piper)
│   │   │   ├── ui/              # Activities & Compose UI
│   │   │   └── util/            # Utilities
│   │   ├── cpp/                 # Native code (llama.cpp)
│   │   ├── res/                 # Resources
│   │   └── assets/              # Models & Wake Words
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Installation

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17
- **Android SDK**: API 26-35
- **NDK**: 25.0+ (for llama.cpp)

### Build from Source
```bash
git clone https://github.com/rajsaraswati-jatavv/holly-android.git
cd holly-android
./gradlew assembleDebug
```

### Download Required Models
Place in `app/src/main/assets/`:
- Vosk Model: `model/` (Hindi-English model from [alphacephei.com](https://alphacephei.com/vosk/models))
- Llama Model: `models/llama-3-8b-instruct-q4_k_m.gguf`
- Wake Word: `wakewords/holly.ppn`

### Install APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

> 📖 See [SETUP.md](./SETUP.md) and [PROJECT_SUMMARY.md](./PROJECT_SUMMARY.md) for detailed setup instructions.

---

## 📖 Usage

### First-Time Setup
1. **Open Holly App**
2. **Grant Permissions**: Accessibility Service, Notification Listener, Draw Over Apps, Battery Optimization bypass, Usage Stats
3. **Customize**: Set your name, choose personality level (Sweet/Romantic/Naughty)
4. **Test**: Say "Hey Holly" to verify setup

### Daily Use
- Say **"Hey Holly"** followed by any command
- Holly responds with voice and can interact with any app
- All processing happens offline on your device

---

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guidelines](./.github/README.md) for details.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## ⚠️ Disclaimer

**This application is for PERSONAL USE on your own device only.**

- Only use on devices you own
- Respect privacy laws and regulations
- The developers are not liable for any misuse

---

## 📺 YouTube

📺 **T3rmuxk1ng** — [https://youtube.com/@T3rmuxk1ng](https://youtube.com/@T3rmuxk1ng)

Subscribe for:
- AI assistant tutorials & demos
- Android automation walkthroughs
- Cybersecurity tips & tricks
- Exclusive tool releases

---

## 🙏 Credits

- **Porcupine** by [Picovoice](https://picovoice.ai) for wake word detection
- **Vosk** by [Alphacephei](https://alphacephei.com/vosk/) for offline STT
- **llama.cpp** for on-device LLM inference
- **Piper** for TTS synthesis

---

<div align="center">

**Built with 💚 by [T3rmuxk1ng](https://youtube.com/@T3rmuxk1ng)**

⭐ If you like this project, give it a star!

*"Holly — The only assistant you'll ever need"*

</div>
