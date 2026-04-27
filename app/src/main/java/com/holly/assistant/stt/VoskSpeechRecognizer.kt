package com.holly.assistant.stt

import android.content.Context
import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import java.io.IOException

/**
 * Vosk Speech Recognizer
 * Provides offline speech-to-text using Vosk models
 * Supports Hindi, English, and Hinglish
 */
class VoskSpeechRecognizer(private val context: Context) : RecognitionListener {

    companion object {
        private const val TAG = "VoskSTT"
        private const val SAMPLE_RATE = 16000f
    }

    private var model: Model? = null
    private var speechService: SpeechService? = null
    
    var onResult: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onPartial: ((String) -> Unit)? = null
    
    private var isListening = false

    init {
        initializeModel()
    }

    private fun initializeModel() {
        try {
            // Load the Vosk model from assets
            // Model should be placed in assets/model folder
            // For Hindi-English, use: vosk-model-small-en-in-0.4 or similar
            val modelPath = copyModelAssets()
            model = Model(modelPath)
            Log.d(TAG, "Vosk model loaded successfully")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load Vosk model", e)
            onError?.invoke("Failed to load speech model")
        }
    }

    private fun copyModelAssets(): String {
        // Copy model from assets to internal storage
        val modelDir = File(context.filesDir, "vosk-model")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
            // Copy model files from assets
            // Implementation depends on how model is packaged
        }
        return modelDir.absolutePath
    }

    fun startListening() {
        if (model == null) {
            onError?.invoke("Model not loaded")
            return
        }
        
        if (isListening) {
            Log.w(TAG, "Already listening")
            return
        }
        
        try {
            val recognizer = Recognizer(model, SAMPLE_RATE)
            speechService = SpeechService(recognizer, SAMPLE_RATE)
            speechService?.startListening(this)
            isListening = true
            Log.d(TAG, "Started listening")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            onError?.invoke("Failed to start speech recognition")
        }
    }

    fun stopListening() {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
        isListening = false
        Log.d(TAG, "Stopped listening")
    }

    fun isListening(): Boolean = isListening

    override fun onPartialResult(hypothesis: String?) {
        hypothesis?.let {
            // Extract partial text from JSON
            val partial = extractText(it)
            onPartial?.invoke(partial)
            Log.d(TAG, "Partial: $partial")
        }
    }

    override fun onResult(hypothesis: String?) {
        hypothesis?.let {
            val text = extractText(it)
            if (text.isNotEmpty()) {
                onResult?.invoke(text)
                Log.d(TAG, "Result: $text")
            }
        }
    }

    override fun onFinalResult(hypothesis: String?) {
        hypothesis?.let {
            val text = extractText(it)
            if (text.isNotEmpty()) {
                onResult?.invoke(text)
                Log.d(TAG, "Final result: $text")
            }
        }
        isListening = false
    }

    override fun onError(e: Exception?) {
        e?.let {
            Log.e(TAG, "Recognition error", it)
            onError?.invoke(it.message ?: "Unknown error")
        }
        isListening = false
    }

    override fun onTimeout() {
        Log.d(TAG, "Recognition timeout")
        isListening = false
    }

    private fun extractText(json: String): String {
        // Parse JSON response from Vosk
        // Format: {"text": "hello world", "partial": "hello"}
        return try {
            val textPattern = """"text"\s*:\s*"([^"]*)"""".toRegex()
            val match = textPattern.find(json)
            match?.groupValues?.get(1) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun destroy() {
        stopListening()
        model?.close()
        model = null
    }
}
