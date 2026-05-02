package com.holly.assistant.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Floating Bubble Service
 * Provides a floating bubble overlay for quick access to Holly
 */
class FloatingBubbleService : Service() {

    companion object {
        private const val TAG = "FloatingBubbleService"
    }

    override fun onCreate() {
        super.onCreate()
        // Floating bubble will be implemented with SYSTEM_ALERT_WINDOW permission
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }
}
