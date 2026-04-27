package com.holly.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.holly.assistant.data.local.HollyDatabase
import com.holly.assistant.service.HollyVoiceService
import com.holly.assistant.util.PreferenceManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Holly Voice Assistant Application
 * Main application class that initializes core components
 */
@HiltAndroidApp
class HollyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Initialize database
        HollyDatabase.getInstance(this)
        
        // Create notification channels
        createNotificationChannels()
        
        // Start voice service if already set up
        if (preferenceManager.isSetupComplete()) {
            HollyVoiceService.start(this)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Main service channel
            val serviceChannel = NotificationChannel(
                CHANNEL_VOICE_SERVICE,
                getString(R.string.channel_voice_service),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_voice_service_desc)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(serviceChannel)

            // Commands channel
            val commandChannel = NotificationChannel(
                CHANNEL_COMMANDS,
                getString(R.string.channel_commands),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_commands_desc)
            }
            notificationManager.createNotificationChannel(commandChannel)

            // Alerts channel
            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS,
                getString(R.string.channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_alerts_desc)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val CHANNEL_VOICE_SERVICE = "holly_voice_service"
        const val CHANNEL_COMMANDS = "holly_commands"
        const val CHANNEL_ALERTS = "holly_alerts"
    }
}
