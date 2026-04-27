# Holly Android - Additional Setup Info

## Quick Model Downloads

### Vosk Model (Already Included)
The Vosk Hindi-English model is included in `app/src/main/assets/model/`

### Llama Model
Download from HuggingFace:
- **Recommended**: [Llama-3-8B-Instruct-Q4_K_M.gguf](https://huggingface.co/maziyarpanahi/Llama-3-8B-Instruct-GGUF/resolve/main/Llama-3-8B-Instruct.Q4_K_M.gguf)
- Place in: `app/src/main/assets/models/`

### Porcupine Wake Word
1. Go to [Picovoice Console](https://console.picovoice.ai/)
2. Create a free account
3. Create a new project
4. Train wake word "Holly" or use "Jarvis" (built-in)
5. Download `.ppn` file to `app/src/main/assets/wakewords/`
6. Add your Access Key in `PreferenceManager.kt`

## Build Commands

```bash
# Debug APK
./gradlew assembleDebug

# Release APK  
./gradlew assembleRelease

# Clean
./gradlew clean
```

## Architecture

```
┌─────────────────────────────────────────┐
│           Holly Voice Service           │
│  (Foreground Service - Always Running)  │
├─────────────────────────────────────────┤
│  ┌─────────────┐    ┌────────────────┐  │
│  │  Wake Word  │───▶│  Speech STT   │  │
│  │  (Porcupine)│    │    (Vosk)      │  │
│  └─────────────┘    └────────────────┘  │
│                              │          │
│                              ▼          │
│                    ┌────────────────┐   │
│                    │   Command      │   │
│                    │   Processor    │   │
│                    └────────────────┘   │
│                       │         │       │
│                       ▼         ▼       │
│              ┌──────────┐ ┌──────────┐  │
│              │   LLM    │ │   TTS    │  │
│              │(llama.cpp)│ │ (Piper)  │  │
│              └──────────┘ └──────────┘  │
└─────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────┐
│       Accessibility Service             │
│  (UI Automation - Gestures & Control)   │
└─────────────────────────────────────────┘
```

---
Made with ❤️ for RS
