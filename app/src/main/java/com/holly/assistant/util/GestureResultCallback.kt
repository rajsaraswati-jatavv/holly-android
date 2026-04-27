package com.holly.assistant.util

import android.accessibilityservice.GestureDescription
import android.util.Log

/**
 * Gesture Result Callback
 * Handles results of gesture execution
 */
abstract class GestureResultCallback : GestureDescription.ResultCallback() {
    
    companion object {
        private const val TAG = "GestureCallback"
    }
    
    override fun onCompleted(gestureDescription: GestureDescription?) {
        Log.d(TAG, "Gesture completed successfully")
        onGestureComplete(true)
    }
    
    override fun onCancelled(gestureDescription: GestureDescription?) {
        Log.d(TAG, "Gesture cancelled")
        onGestureComplete(false)
    }
    
    abstract fun onGestureComplete(success: Boolean)
}
