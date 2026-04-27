package com.holly.assistant.util

import android.content.Context
import android.util.Log

/**
 * Wake Word Manager
 * Manages custom wake word configuration
 */
class WakeWordManager(private val context: Context) {

    companion object {
        private const val TAG = "WakeWordManager"
        
        // Default wake words
        val DEFAULT_WAKE_WORDS = listOf("Holly", "Hey Holly")
        
        // Custom wake word file names
        const val WAKE_WORD_FILE = "holly.ppn"
    }

    private val prefs = PreferenceManager(context)

    /**
     * Get current wake word
     */
    fun getWakeWord(): String {
        return prefs.getWakeWord()
    }

    /**
     * Set custom wake word
     * Note: Requires Porcupine custom keyword training
     */
    fun setWakeWord(word: String) {
        prefs.setWakeWord(word)
        Log.d(TAG, "Wake word set to: $word")
    }

    /**
     * Check if wake word is default
     */
    fun isDefaultWakeWord(): Boolean {
        return getWakeWord() in DEFAULT_WAKE_WORDS
    }

    /**
     * Get wake word asset path
     */
    fun getWakeWordAssetPath(): String {
        return "wakewords/$WAKE_WORD_FILE"
    }

    /**
     * Get all available wake words
     */
    fun getAvailableWakeWords(): List<String> {
        return DEFAULT_WAKE_WORDS + getCustomWakeWords()
    }

    /**
     * Get custom wake words (if any trained)
     */
    private fun getCustomWakeWords(): List<String> {
        // Return custom wake words from storage
        return emptyList()
    }
}
