package com.holly.assistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.holly.assistant.service.HollyVoiceService
import com.holly.assistant.util.PreferenceManager

/**
 * Watchdog Receiver
 * Ensures Holly service stays alive
 * Triggered by periodic alarm
 */
class WatchdogReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WatchdogReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Watchdog triggered")
        
        val prefs = PreferenceManager(context)
        
        if (prefs.isSetupComplete()) {
            // Check if service is running and restart if needed
            val serviceIntent = Intent(context, HollyVoiceService::class.java).apply {
                action = HollyVoiceService.ACTION_START
            }
            
            context.startService(serviceIntent)
            Log.d(TAG, "Watchdog restarted service")
        }
    }
}
