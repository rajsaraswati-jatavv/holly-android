package com.holly.assistant.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Preference Manager
 * Handles encrypted storage of user preferences and settings
 */
class PreferenceManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "holly_preferences",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val regularPrefs = context.getSharedPreferences("holly_regular_prefs", Context.MODE_PRIVATE)

    // ==================== Setup Status ====================
    
    fun isSetupComplete(): Boolean = encryptedPrefs.getBoolean(KEY_SETUP_COMPLETE, false)
    
    fun setSetupComplete(complete: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_SETUP_COMPLETE, complete).apply()
    }
    
    fun isFirstStart(): Boolean = encryptedPrefs.getBoolean(KEY_FIRST_START, true)
    
    fun setFirstStart(first: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_FIRST_START, first).apply()
    }

    // ==================== Accessibility & Permissions ====================
    
    fun isAccessibilityEnabled(): Boolean = encryptedPrefs.getBoolean(KEY_ACCESSIBILITY_ENABLED, false)
    
    fun setAccessibilityEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_ACCESSIBILITY_ENABLED, enabled).apply()
    }
    
    fun isDeviceAdminEnabled(): Boolean = encryptedPrefs.getBoolean(KEY_DEVICE_ADMIN_ENABLED, false)
    
    fun setDeviceAdminEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_DEVICE_ADMIN_ENABLED, enabled).apply()
    }
    
    fun isNotificationListenerEnabled(): Boolean = encryptedPrefs.getBoolean(KEY_NOTIFICATION_LISTENER_ENABLED, false)
    
    fun setNotificationListenerEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_NOTIFICATION_LISTENER_ENABLED, enabled).apply()
    }

    // ==================== Voice Settings ====================
    
    fun getWakeWord(): String = encryptedPrefs.getString(KEY_WAKE_WORD, "Holly") ?: "Holly"
    
    fun setWakeWord(wakeWord: String) {
        encryptedPrefs.edit().putString(KEY_WAKE_WORD, wakeWord).apply()
    }
    
    fun getPicovoiceAccessKey(): String? = encryptedPrefs.getString(KEY_PICOVOICE_KEY, null)
    
    fun setPicovoiceAccessKey(key: String) {
        encryptedPrefs.edit().putString(KEY_PICOVOICE_KEY, key).apply()
    }
    
    fun getVoiceStyle(): VoiceStyle {
        val style = encryptedPrefs.getString(KEY_VOICE_STYLE, VoiceStyle.SWEET.name)
        return try {
            VoiceStyle.valueOf(style ?: VoiceStyle.SWEET.name)
        } catch (e: Exception) {
            VoiceStyle.SWEET
        }
    }
    
    fun setVoiceStyle(style: VoiceStyle) {
        encryptedPrefs.edit().putString(KEY_VOICE_STYLE, style.name).apply()
    }
    
    fun getPersonalityLevel(): PersonalityLevel {
        val level = encryptedPrefs.getString(KEY_PERSONALITY_LEVEL, PersonalityLevel.ROMANTIC.name)
        return try {
            PersonalityLevel.valueOf(level ?: PersonalityLevel.ROMANTIC.name)
        } catch (e: Exception) {
            PersonalityLevel.ROMANTIC
        }
    }
    
    fun setPersonalityLevel(level: PersonalityLevel) {
        encryptedPrefs.edit().putString(KEY_PERSONALITY_LEVEL, level.name).apply()
    }

    // ==================== TTS Settings ====================
    
    fun getSpeechRate(): Float = regularPrefs.getFloat(KEY_SPEECH_RATE, 1.0f)
    
    fun setSpeechRate(rate: Float) {
        regularPrefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
    }
    
    fun getTTSPitch(): Float = regularPrefs.getFloat(KEY_TTS_PITCH, 1.0f)
    
    fun setTTSPitch(pitch: Float) {
        regularPrefs.edit().putFloat(KEY_TTS_PITCH, pitch).apply()
    }

    // ==================== LLM Settings ====================
    
    fun getLLMModel(): String = regularPrefs.getString(KEY_LLM_MODEL, "llama-3-8b-instruct-q4_k_m.gguf") ?: "llama-3-8b-instruct-q4_k_m.gguf"
    
    fun setLLMModel(model: String) {
        regularPrefs.edit().putString(KEY_LLM_MODEL, model).apply()
    }
    
    fun getTemperature(): Float = regularPrefs.getFloat(KEY_TEMPERATURE, 0.7f)
    
    fun setTemperature(temp: Float) {
        regularPrefs.edit().putFloat(KEY_TEMPERATURE, temp).apply()
    }

    // ==================== User Info ====================
    
    fun getUserName(): String = encryptedPrefs.getString(KEY_USER_NAME, "Baby") ?: "Baby"
    
    fun setUserName(name: String) {
        encryptedPrefs.edit().putString(KEY_USER_NAME, name).apply()
    }
    
    fun getUserPetName(): String = encryptedPrefs.getString(KEY_USER_PET_NAME, "") ?: ""
    
    fun setUserPetName(name: String) {
        encryptedPrefs.edit().putString(KEY_USER_PET_NAME, name).apply()
    }

    // ==================== Theme ====================
    
    fun isDarkMode(): Boolean = regularPrefs.getBoolean(KEY_DARK_MODE, true)
    
    fun setDarkMode(dark: Boolean) {
        regularPrefs.edit().putBoolean(KEY_DARK_MODE, dark).apply()
    }

    companion object {
        // Setup
        private const val KEY_SETUP_COMPLETE = "setup_complete"
        private const val KEY_FIRST_START = "first_start"
        
        // Permissions
        private const val KEY_ACCESSIBILITY_ENABLED = "accessibility_enabled"
        private const val KEY_DEVICE_ADMIN_ENABLED = "device_admin_enabled"
        private const val KEY_NOTIFICATION_LISTENER_ENABLED = "notification_listener_enabled"
        
        // Voice
        private const val KEY_WAKE_WORD = "wake_word"
        private const val KEY_PICOVOICE_KEY = "picovoice_key"
        private const val KEY_VOICE_STYLE = "voice_style"
        private const val KEY_PERSONALITY_LEVEL = "personality_level"
        
        // TTS
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_TTS_PITCH = "tts_pitch"
        
        // LLM
        private const val KEY_LLM_MODEL = "llm_model"
        private const val KEY_TEMPERATURE = "temperature"
        
        // User
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PET_NAME = "user_pet_name"
        
        // Theme
        private const val KEY_DARK_MODE = "dark_mode"
    }
}

enum class VoiceStyle {
    SWEET, ROMANTIC, PLAYFUL, NAUGHTY
}

enum class PersonalityLevel {
    SWEET, ROMANTIC, NAUGHTY
}
