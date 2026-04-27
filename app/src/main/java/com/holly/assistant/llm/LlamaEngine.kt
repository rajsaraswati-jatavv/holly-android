package com.holly.assistant.llm

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Llama Engine
 * Wrapper for llama.cpp to run quantized LLM models locally
 * Enables Holly's girlfriend personality responses
 */
class LlamaEngine(private val context: Context) {

    companion object {
        private const val TAG = "LlamaEngine"
        
        init {
            try {
                System.loadLibrary("llama")
                Log.d(TAG, "llama.cpp library loaded")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load llama library", e)
            }
        }
    }

    private var modelPath: String? = null
    private var isLoaded = false
    private var contextPtr: Long = 0

    /**
     * Load a model from the given path
     * @param modelFileName Name of the model file in assets/models/
     */
    fun loadModel(modelFileName: String): Boolean {
        try {
            val modelFile = File(context.filesDir, "models/$modelFileName")
            
            // If model doesn't exist, copy from assets
            if (!modelFile.exists()) {
                modelFile.parentFile?.mkdirs()
                copyModelFromAssets(modelFileName, modelFile)
            }
            
            modelPath = modelFile.absolutePath
            
            // Initialize native model
            contextPtr = nativeLoadModel(modelPath!!)
            
            if (contextPtr != 0L) {
                isLoaded = true
                Log.d(TAG, "Model loaded successfully: $modelFileName")
                return true
            } else {
                Log.e(TAG, "Failed to load model in native code")
                return false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model", e)
            return false
        }
    }

    /**
     * Generate text given a prompt
     * @param prompt The input prompt
     * @param maxTokens Maximum number of tokens to generate
     * @return Generated text
     */
    fun generate(prompt: String, maxTokens: Int = 100): String {
        if (!isLoaded) {
            Log.e(TAG, "Model not loaded")
            return "Model not loaded"
        }
        
        return try {
            nativeGenerate(contextPtr, prompt, maxTokens)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating text", e)
            "Error generating response"
        }
    }

    /**
     * Generate text with streaming callback
     */
    fun generateStream(
        prompt: String,
        maxTokens: Int = 100,
        onToken: (String) -> Unit
    ): String {
        if (!isLoaded) {
            Log.e(TAG, "Model not loaded")
            return "Model not loaded"
        }
        
        val result = StringBuilder()
        
        try {
            nativeGenerateStream(contextPtr, prompt, maxTokens) { token ->
                result.append(token)
                onToken(token)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming text", e)
        }
        
        return result.toString()
    }

    /**
     * Set generation parameters
     */
    fun setParameters(
        temperature: Float = 0.7f,
        topP: Float = 0.9f,
        topK: Int = 40,
        repeatPenalty: Float = 1.1f
    ) {
        if (isLoaded) {
            nativeSetParameters(contextPtr, temperature, topP, topK, repeatPenalty)
        }
    }

    /**
     * Close and release resources
     */
    fun close() {
        if (isLoaded && contextPtr != 0L) {
            nativeFree(contextPtr)
            contextPtr = 0
            isLoaded = false
        }
    }

    private fun copyModelFromAssets(modelFileName: String, destFile: File) {
        context.assets.open("models/$modelFileName").use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    // Native methods
    private external fun nativeLoadModel(modelPath: String): Long
    private external fun nativeGenerate(contextPtr: Long, prompt: String, maxTokens: Int): String
    private external fun nativeGenerateStream(
        contextPtr: Long,
        prompt: String,
        maxTokens: Int,
        callback: (String) -> Unit
    )
    private external fun nativeSetParameters(
        contextPtr: Long,
        temperature: Float,
        topP: Float,
        topK: Int,
        repeatPenalty: Float
    )
    private external fun nativeFree(contextPtr: Long)
}
