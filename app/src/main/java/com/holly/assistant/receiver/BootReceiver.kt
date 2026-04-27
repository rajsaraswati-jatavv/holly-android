package com.holly.assistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.holly.assistant.service.HollyVoiceService
import com.holly.assistant.util.PreferenceManager

/**
 * Boot Receiver
 * Starts Holly service on device boot
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Boot received: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                val prefs = PreferenceManager(context)
                
                if (prefs.isSetupComplete()) {
                    // Start voice service after boot
                    val serviceIntent = Intent(context, HollyVoiceService::class.java).apply {
                        action = HollyVoiceService.ACTION_START
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    
                    Log.d(TAG, "Holly service started after boot")
                }
            }
        }
    }
}
