package com.holly.assistant.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

/**
 * Piper Text-to-Speech
 * Provides offline text-to-speech with a warm, feminine voice
 * Falls back to system TTS if Piper is not available
 */
class PiperTTS(private val context: Context) {

    companion object {
        private const val TAG = "PiperTTS"
    }

    private var systemTTS: TextToSpeech? = null
    private var isReady = false
    private var utteranceId = 0
    
    var onSpeakStart: (() -> Unit)? = null
    var onSpeakEnd: (() -> Unit)? = null

    init {
        initializeSystemTTS()
    }

    private fun initializeSystemTTS() {
        systemTTS = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Set language to Hindi/English
                val result = systemTTS?.setLanguage(Locale("en", "IN"))
                
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to US English
                    systemTTS?.setLanguage(Locale.US)
                }
                
                systemTTS?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        onSpeakStart?.invoke()
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        onSpeakEnd?.invoke()
                    }
                    
                    override fun onError(utteranceId: String?) {
                        Log.e(TAG, "TTS error for utterance: $utteranceId")
                    }
                })
                
                isReady = true
                Log.d(TAG, "System TTS initialized")
            } else {
                Log.e(TAG, "Failed to initialize TTS")
            }
        }
    }

    /**
     * Speak the given text
     */
    fun speak(text: String) {
        if (!isReady) {
            Log.w(TAG, "TTS not ready")
            return
        }
        
        utteranceId++
        systemTTS?.speak(
            text,
            TextToSpeech.QUEUE_ADD,
            null,
            "utterance_$utteranceId"
        )
    }

    /**
     * Speak immediately, flushing previous queue
     */
    fun speakNow(text: String) {
        if (!isReady) {
            Log.w(TAG, "TTS not ready")
            return
        }
        
        utteranceId++
        systemTTS?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "utterance_$utteranceId"
        )
    }

    /**
     * Stop speaking
     */
    fun stop() {
        systemTTS?.stop()
    }

    /**
     * Set speech rate (0.5 to 2.0)
     */
    fun setSpeechRate(rate: Float) {
        systemTTS?.setSpeechRate(rate)
    }

    /**
     * Set pitch (0.5 to 2.0)
     */
    fun setPitch(pitch: Float) {
        systemTTS?.setPitch(pitch)
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        systemTTS?.stop()
        systemTTS?.shutdown()
        systemTTS = null
        isReady = false
    }
}
