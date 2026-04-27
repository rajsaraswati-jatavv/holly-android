# Holly Android - Voice Assistant APK

**Holly** is an all-powerful, always-listening voice assistant with complete system-level access. She responds instantly to your voice commands and embodies a loving girlfriend persona.

## Features

### 🎤 Voice Recognition
- **Wake Word Detection**: Say "Hey Holly" or just "Holly" to activate
- **Offline STT**: Vosk-based speech recognition for Hindi, English, Hinglish
- **Offline TTS**: Natural voice responses with Piper

### 💕 Holly Personality
- **Girlfriend Mode**: Loving, playful, romantic persona
- **Local LLM**: Llama 3 8B running on-device for conversations
- **Memory System**: Remembers your preferences and past conversations

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

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Wake Word | Porcupine (Picovoice) |
| STT | Vosk Android |
| TTS | Piper / System TTS |
| LLM | llama.cpp (quantized Llama 3) |
| Database | Room + SQLite |
| Automation | AccessibilityService |

## Project Structure

```
holly-android/
├── app/
│   ├── src/main/
│   │   ├── java/com/holly/assistant/
│   │   │   ├── admin/           # Device Admin
│   │   │   ├── data/            # Database & Models
│   │   │   ├── llm/             # LLM Engine
│   │   │   ├── receiver/        # Broadcast Receivers
│   │   │   ├── service/         # Core Services
│   │   │   ├── stt/             # Speech Recognition
│   │   │   ├── tts/             # Text-to-Speech
│   │   │   ├── ui/              # Activities & Compose UI
│   │   │   └── util/            # Utilities
│   │   ├── cpp/                 # Native code (llama.cpp)
│   │   ├── res/                 # Resources
│   │   └── assets/              # Models & Wake Words
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Build Instructions

### Prerequisites

1. **Android Studio**: Hedgehog (2023.1.1) or later
2. **JDK**: 17
3. **Android SDK**: API 26-35
4. **NDK**: 25.0+ (for llama.cpp)

### Steps

1. **Clone and Open**
   ```bash
   cd holly-android
   # Open in Android Studio
   ```

2. **Download Models** (Place in `app/src/main/assets/`)
   - Vosk Model: `model/` (Hindi-English model)
   - Llama Model: `models/llama-3-8b-instruct-q4_k_m.gguf`
   - Wake Word: `wakewords/holly.ppn`

3. **Get Picovoice Access Key**
   - Sign up at [Picovoice Console](https://console.picovoice.ai/)
   - Create a project and get your Access Key
   - Train custom wake word "Holly" or use built-in

4. **Build APK**
   ```bash
   # Debug APK
   ./gradlew assembleDebug
   
   # Release APK
   ./gradlew assembleRelease
   ```

5. **Install**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## First-Time Setup

1. **Open Holly App**
2. **Grant Permissions**:
   - Accessibility Service
   - Notification Listener
   - Draw Over Apps
   - Battery Optimization (ignore)
   - Usage Stats
   
3. **Customize**:
   - Set your name
   - Choose personality level (Sweet/Romantic/Naughty)
   
4. **Test**: Say "Hey Holly" to verify setup

## Voice Commands

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

### Navigation
```
"Home jao"                → Goes to home screen
"Back jao"                → Presses back
```

### Power
```
"Phone lock karo"         → Locks the screen
```

### Conversational
```
"I love you Holly"        → Holly responds lovingly
"Mujhe thoda mood nahi hai" → Holly provides emotional support
"Mujhe ek joke sunao"      → Holly tells a joke
```

## APK Signing (Release)

```bash
# Generate keystore
keytool -genkeypair -v \
  -keystore holly-release.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias holly

# Configure signing in app/build.gradle.kts
# Build release APK
./gradlew assembleRelease
```

## Troubleshooting

### Service Not Starting
- Check if Battery Optimization is disabled
- Verify all permissions are granted
- Check if Accessibility Service is enabled

### Wake Word Not Working
- Ensure Picovoice Access Key is configured
- Check microphone permission
- Try retraining wake word

### LLM Not Loading
- Verify model file exists in assets
- Check device has at least 4GB RAM
- Ensure sufficient storage space

## Uninstallation

Since Holly uses Device Admin:
1. Go to Settings → Security → Device Admin
2. Deactivate "Holly Assistant"
3. Now uninstall normally

## License

This project is for personal use by RS. All rights reserved.

## Credits

- **Porcupine** by Picovoice for wake word detection
- **Vosk** by Alphacephei for offline STT
- **llama.cpp** for on-device LLM inference
- **Piper** for TTS synthesis

---

**Made with ❤️ for RS**

*"Holly - The only assistant you'll ever need"*
