package com.holly.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.holly.assistant.util.PreferenceManager

/**
 * Holly Notification Listener Service
 * Reads and interacts with notifications from all apps
 * Enables voice control over notifications
 */
class HollyNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "HollyNotification"
        
        @Volatile
        private var instance: HollyNotificationListenerService? = null
        
        fun getInstance(): HollyNotificationListenerService? = instance
        
        fun isServiceEnabled(): Boolean = instance != null
    }

    private lateinit var preferenceManager: PreferenceManager
    private var shouldReadNotifications = true

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferenceManager = PreferenceManager(this)
        Log.d(TAG, "Notification listener service created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
        preferenceManager.setNotificationListenerEnabled(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
        preferenceManager.setNotificationListenerEnabled(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        sbn?.let { notification ->
            val packageName = notification.packageName
            val extras = notification.notification.extras
            
            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            
            Log.d(TAG, "Notification from $packageName: $title - $text")
            
            // Process notification
            processNotification(packageName, title, text)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "Notification removed: ${sbn?.packageName}")
    }

    private fun processNotification(packageName: String, title: String, text: String) {
        // Don't read our own notifications
        if (packageName == "com.holly.assistant") return
        
        // Skip if user disabled reading
        if (!shouldReadNotifications) return
        
        // Filter important notifications
        val importantApps = listOf(
            "com.whatsapp",
            "com.google.android.apps.messaging",
            "com.facebook.orca",
            "com.telegram.messenger",
            "com.instagram.android",
            "com.twitter.android",
            "com.android.mms",
            "com.google.android.gm"
        )
        
        if (packageName in importantApps) {
            // Broadcast to voice service
            val intent = Intent("com.holly.assistant.NOTIFICATION_RECEIVED").apply {
                putExtra("package", packageName)
                putExtra("title", title)
                putExtra("text", text)
            }
            sendBroadcast(intent)
        }
    }

    /**
     * Get all active notifications
     */
    fun getAllNotifications(): List<StatusBarNotification> {
        return activeNotifications?.toList() ?: emptyList()
    }

    /**
     * Get notifications from a specific app
     */
    fun getNotificationsFromApp(packageName: String): List<StatusBarNotification> {
        return activeNotifications?.filter { it.packageName == packageName } ?: emptyList()
    }

    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        cancelAllNotifications()
    }

    /**
     * Clear notifications from a specific app
     */
    fun clearAppNotifications(packageName: String) {
        activeNotifications?.filter { it.packageName == packageName }?.forEach {
            cancelNotification(it.key)
        }
    }

    /**
     * Reply to a notification (for messaging apps)
     */
    fun replyToNotification(sbn: StatusBarNotification, message: String): Boolean {
        // Use WearableExtender to find remote input
        val wearExtender = android.app.Notification.WearableExtender(sbn.notification)
        
        for (action in wearExtender.actions) {
            val remoteInputs = action.remoteInputs
            if (remoteInputs != null && remoteInputs.isNotEmpty()) {
                val intent = Intent()
                val bundle = android.os.Bundle()
                
                for (remoteInput in remoteInputs) {
                    bundle.putCharSequence(remoteInput.resultKey, message)
                }
                
                remoteInputs[0]?.let { ri ->
                    android.app.RemoteInput.addResultsToIntent(
                        arrayOf(ri),
                        intent,
                        bundle
                    )
                }
                
                try {
                    action.actionIntent.send(this, 0, intent)
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send reply", e)
                }
            }
        }
        
        return false
    }

    fun setReadNotifications(enabled: Boolean) {
        shouldReadNotifications = enabled
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}
