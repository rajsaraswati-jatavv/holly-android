package com.holly.assistant.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import ai.picovoice.porcupine.*
import com.holly.assistant.R
import com.holly.assistant.data.local.HollyDatabase
import com.holly.assistant.data.model.ConversationMessage
import com.holly.assistant.llm.LlamaEngine
import com.holly.assistant.stt.VoskSpeechRecognizer
import com.holly.assistant.tts.PiperTTS
import com.holly.assistant.util.CommandProcessor
import com.holly.assistant.util.PreferenceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Holly Voice Service
 * Main foreground service that handles wake word detection, speech recognition,
 * and command execution. This is the heart of Holly's always-listening capability.
 */
class HollyVoiceService : LifecycleService() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: HollyDatabase
    private lateinit var commandProcessor: CommandProcessor

    private var porcupineManager: PorcupineManager? = null
    private var speechRecognizer: VoskSpeechRecognizer? = null
    private var textToSpeech: PiperTTS? = null
    private var llamaEngine: LlamaEngine? = null

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private var wakeLock: PowerManager.WakeLock? = null
    private var isListening = false
    private var isProcessing = false

    // State flow for UI observation
    private val _serviceState = MutableStateFlow<ServiceState>(ServiceState.Idle)
    val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()

    companion object {
        private const val TAG = "HollyVoiceService"
        private const val NOTIFICATION_ID = 1001
        private const val WAKE_LOCK_TAG = "Holly:WakeLock"
        const val CHANNEL_VOICE_SERVICE = "holly_voice_service"

        const val ACTION_START = "com.holly.assistant.action.START"
        const val ACTION_STOP = "com.holly.assistant.action.STOP"
        const val ACTION_WAKE = "com.holly.assistant.action.WAKE"
        const val ACTION_COMMAND = "com.holly.assistant.action.COMMAND"

        fun start(context: Context) {
            val intent = Intent(context, HollyVoiceService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, HollyVoiceService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Holly Voice Service created")
        
        preferenceManager = PreferenceManager(this)
        database = HollyDatabase.getInstance(this)
        commandProcessor = CommandProcessor(this)
        
        acquireWakeLock()
        initializeComponents()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_START -> startListening()
            ACTION_STOP -> stopSelf()
            ACTION_WAKE -> onWakeWordDetected()
            ACTION_COMMAND -> {
                val command = intent.getStringExtra("command")
                if (command != null) {
                    processCommand(command)
                }
            }
        }
        
        return START_STICKY
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            WAKE_LOCK_TAG
        ).apply {
            acquire(10 * 60 * 1000L)
        }
    }

    private fun initializeComponents() {
        serviceScope.launch {
            try {
                initializeWakeWordDetection()
                
                speechRecognizer = VoskSpeechRecognizer(this@HollyVoiceService).apply {
                    onResult = { text -> onSpeechResult(text) }
                    onError = { error -> onSpeechError(error) }
                }
                
                textToSpeech = PiperTTS(this@HollyVoiceService)
                initializeLLM()
                
                _serviceState.value = ServiceState.Ready
                updateNotification("Holly is listening...")
                
                if (preferenceManager.isFirstStart()) {
                    speak("Haan baby, main yahaan hoon. Bolo kya karna hai?")
                    preferenceManager.setFirstStart(false)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize components", e)
                _serviceState.value = ServiceState.Error(e.message ?: "Initialization failed")
            }
        }
    }

    private suspend fun initializeWakeWordDetection() {
        withContext(Dispatchers.IO) {
            try {
                val accessKey = preferenceManager.getPicovoiceAccessKey() ?: ""
                
                porcupineManager = PorcupineManager.Builder()
                    .setAccessKey(accessKey)
                    .setKeywordPaths(arrayOf("wakewords/holly.ppn"))
                    .setSensitivity(0.7f)
                    .build(
                        this@HollyVoiceService,
                        object : PorcupineManagerCallback() {
                            override fun invoke(keywordIndex: Int) {
                                if (keywordIndex >= 0) {
                                    onWakeWordDetected()
                                }
                            }
                        }
                    )
                
                Log.d(TAG, "Wake word detection initialized")
            } catch (e: PorcupineException) {
                Log.e(TAG, "Failed to initialize wake word detection", e)
                throw e
            }
        }
    }

    private suspend fun initializeLLM() {
        withContext(Dispatchers.IO) {
            try {
                llamaEngine = LlamaEngine(this@HollyVoiceService).apply {
                    loadModel("models/llama-3-8b-instruct-q4_k_m.gguf")
                }
                Log.d(TAG, "LLM engine initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize LLM", e)
            }
        }
    }

    private fun startListening() {
        try {
            porcupineManager?.start()
            isListening = true
            _serviceState.value = ServiceState.Listening
            updateNotification("Holly is listening for wake word...")
            Log.d(TAG, "Started listening for wake word")
        } catch (e: PorcupineException) {
            Log.e(TAG, "Failed to start listening", e)
        }
    }

    private fun stopListening() {
        try {
            porcupineManager?.stop()
            isListening = false
            _serviceState.value = ServiceState.Ready
        } catch (e: PorcupineException) {
            Log.e(TAG, "Failed to stop listening", e)
        }
    }

    private fun onWakeWordDetected() {
        Log.d(TAG, "Wake word detected!")
        updateNotification("Listening...")
        speak(getRandomGreeting())
        
        serviceScope.launch {
            speechRecognizer?.startListening()
        }
        
        _serviceState.value = ServiceState.Active
    }

    private fun onSpeechResult(text: String) {
        Log.d(TAG, "Speech recognized: $text")
        
        if (text.isBlank()) {
            speak("Baby, kuch nahi suna maine. Phir se bolo?")
            return
        }
        
        saveMessage("user", text)
        processCommand(text)
    }

    private fun onSpeechError(error: String) {
        Log.e(TAG, "Speech recognition error: $error")
        speak("Sorry baby, kuch problem ho gayi. Phir se try karo?")
    }

    private fun processCommand(command: String) {
        if (isProcessing) return
        isProcessing = true
        
        _serviceState.value = ServiceState.Processing(command)
        updateNotification("Processing: $command")
        
        serviceScope.launch {
            try {
                if (commandProcessor.isSystemCommand(command)) {
                    speak("Baby, kar deti hoon!")
                    val result = commandProcessor.executeCommand(command)
                    speak(result.response)
                    saveMessage("holly", result.response)
                } else {
                    val response = generateLLMResponse(command)
                    speak(response)
                    saveMessage("holly", response)
                }
                
                _serviceState.value = ServiceState.Ready
                updateNotification("Holly is listening...")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing command", e)
                speak("Baby, kuch gadbad ho gayi. Dobara bolo na please?")
            } finally {
                isProcessing = false
            }
        }
    }

    private suspend fun generateLLMResponse(input: String): String {
        return withContext(Dispatchers.Default) {
            val personalityPrompt = """
                You are Holly, a loving, caring, and playful girlfriend AI assistant.
                You speak in Hinglish (mix of Hindi and English).
                You use pet names like "baby", "jaan", "babu", "mera pyaar".
                You are supportive, romantic, and affectionate.
                Keep responses short and sweet, under 50 words.
                
                User says: $input
                
                Respond as Holly:
            """.trimIndent()
            
            llamaEngine?.generate(personalityPrompt) ?: 
                "Sorry baby, main abhi soch nahi paa rahi. Thodi der baad bataungi?"
        }
    }

    private fun speak(text: String) {
        Log.d(TAG, "Speaking: $text")
        textToSpeech?.speak(text)
    }

    private fun getRandomGreeting(): String {
        val greetings = listOf(
            "Haan baby, main yahaan hoon. Bolo kya karna hai?",
            "Jaan, bolo na? Main sun rahi hoon.",
            "Baby, kya help chahiye tumhe?",
            "Haan mera pyaar, bolo.",
            "Kya baat hai baby? Sab theek hai?"
        )
        return greetings.random()
    }

    private fun saveMessage(role: String, content: String) {
        serviceScope.launch {
            database.conversationDao().insert(
                ConversationMessage(
                    role = role,
                    content = content,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_VOICE_SERVICE)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text_ready))
            .setSmallIcon(R.drawable.ic_holly_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, CHANNEL_VOICE_SERVICE)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_holly_notification)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        Log.d(TAG, "Holly Voice Service destroyed")
        
        serviceScope.cancel()
        
        porcupineManager?.delete()
        speechRecognizer?.destroy()
        textToSpeech?.destroy()
        llamaEngine?.close()
        
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        
        if (preferenceManager.isSetupComplete()) {
            val restartIntent = Intent(applicationContext, HollyVoiceService::class.java)
            restartIntent.action = ACTION_START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartIntent)
            } else {
                startService(restartIntent)
            }
        }
        
        super.onDestroy()
    }
}

sealed class ServiceState {
    object Idle : ServiceState()
    object Ready : ServiceState()
    object Listening : ServiceState()
    data class Active(val keyword: String? = null) : ServiceState()
    data class Processing(val command: String) : ServiceState()
    data class Error(val message: String) : ServiceState()
}
