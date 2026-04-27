# Holly Android - Project Summary

## Quick Start

1. **Open in Android Studio**
   ```bash
   # Open the holly-android folder in Android Studio
   ```

2. **Sync Project** - Let Gradle sync and download dependencies

3. **Add Required Models** to `app/src/main/assets/`:
   - `model/` - Vosk model (download from https://alphacephei.com/vosk/models)
   - `models/llama-3-8b-instruct-q4_k_m.gguf` - Llama model (quantized)
   - `wakewords/holly.ppn` - Porcupine wake word file

4. **Configure Picovoice Key** in `PreferenceManager.kt` or setup wizard

5. **Build & Run** on device (API 26+)

## Files Created

### Core Services
| File | Purpose |
|------|---------|
| `HollyVoiceService.kt` | Main foreground service for voice assistant |
| `HollyAccessibilityService.kt` | UI automation and app control |
| `HollyNotificationListenerService.kt` | Notification reading and interaction |

### Voice Processing
| File | Purpose |
|------|---------|
| `VoskSpeechRecognizer.kt` | Offline speech-to-text |
| `PiperTTS.kt` | Text-to-speech synthesis |
| `WakeWordManager.kt` | Wake word configuration |

### Intelligence
| File | Purpose |
|------|---------|
| `LlamaEngine.kt` | Local LLM inference |
| `CommandProcessor.kt` | Command parsing and execution |
| `MemoryManager.kt` | User memory and preferences |

### Data
| File | Purpose |
|------|---------|
| `HollyDatabase.kt` | Room database |
| `Models.kt` | Data entities |
| `Dao.kt` | Database access objects |

### UI
| File | Purpose |
|------|---------|
| `MainActivity.kt` | Main dashboard |
| `SetupActivity.kt` | First-time setup wizard |
| `SettingsActivity.kt` | Settings screen |
| `Theme.kt` | Material 3 theme |

## Dependencies

```kotlin
// Voice
implementation("com.alphacephei:vosk-android:0.3.47")
implementation("ai.picovoice:porcupine-android:3.0.2")

// UI
implementation(platform("androidx.compose:compose-bom:2024.01.00"))
implementation("androidx.compose.material3:material3")

// Database
implementation("androidx.room:room-runtime:2.6.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## Permissions Required

- `RECORD_AUDIO` - Voice input
- `BIND_ACCESSIBILITY_SERVICE` - UI automation
- `BIND_NOTIFICATION_LISTENER_SERVICE` - Notifications
- `BIND_DEVICE_ADMIN` - Lock screen
- `SYSTEM_ALERT_WINDOW` - Floating overlay
- `FOREGROUND_SERVICE` - Background service
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` - Always-on

## APK Information

- **Package**: `com.holly.assistant`
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)
- **Size Estimate**: ~50MB (with models)

## Next Steps

1. Add Vosk model for Hindi-English STT
2. Add Llama 3 quantized model for conversations
3. Get Picovoice access key and train wake word
4. Build and test on device
5. Generate signed release APK

## Support

For issues or questions, check:
- README.md for detailed documentation
- Logcat with tag `HollyVoiceService`
- Android Studio debugger
