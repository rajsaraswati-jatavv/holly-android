package com.holly.assistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.holly.assistant.util.GestureResultCallback

/**
 * Holly Accessibility Service
 * Provides full UI automation capabilities including:
 * - Screen reading and element detection
 * - Gesture injection (tap, swipe, scroll, pinch)
 * - Text input automation
 * - App navigation and control
 */
class HollyAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "HollyAccessibility"
        
        @Volatile
        private var instance: HollyAccessibilityService? = null
        
        fun getInstance(): HollyAccessibilityService? = instance
        
        fun isServiceEnabled(): Boolean = instance != null
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Process accessibility events if needed
        event?.let {
            when (it.eventType) {
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                    handleNotification(it)
                }
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    handleWindowChange(it)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    // ==================== App Control ====================

    fun openApp(packageName: String): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } else {
            Log.e(TAG, "Cannot open app: $packageName")
            false
        }
    }

    fun closeCurrentApp(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun goHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun openRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    fun openNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

    fun openQuickSettings(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    }

    fun openPowerDialog(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
    }

    fun lockScreen(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }

    fun takeScreenshot(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
    }

    // ==================== Gesture Actions ====================

    fun tap(x: Float, y: Float, callback: GestureResultCallback? = null): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        
        return dispatchGesture(gesture, callback, null)
    }

    fun tapElement(node: AccessibilityNodeInfo, callback: GestureResultCallback? = null): Boolean {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val x = bounds.exactCenterX()
        val y = bounds.exactCenterY()
        return tap(x, y, callback)
    }

    fun swipe(
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        duration: Long = 300,
        callback: GestureResultCallback? = null
    ): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
        
        return dispatchGesture(gesture, callback, null)
    }

    fun swipeUp(callback: GestureResultCallback? = null): Boolean {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()
        
        return swipe(
            width / 2, height * 0.75f,
            width / 2, height * 0.25f,
            300, callback
        )
    }

    fun swipeDown(callback: GestureResultCallback? = null): Boolean {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()
        
        return swipe(
            width / 2, height * 0.25f,
            width / 2, height * 0.75f,
            300, callback
        )
    }

    fun longPress(x: Float, y: Float, callback: GestureResultCallback? = null): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 1000))
            .build()
        
        return dispatchGesture(gesture, callback, null)
    }

    fun pinch(
        centerX: Float, centerY: Float,
        startDistance: Float, endDistance: Float,
        callback: GestureResultCallback? = null
    ): Boolean {
        // Two-finger pinch gesture
        val finger1Path = Path().apply {
            moveTo(centerX - startDistance / 2, centerY)
            lineTo(centerX - endDistance / 2, centerY)
        }
        
        val finger2Path = Path().apply {
            moveTo(centerX + startDistance / 2, centerY)
            lineTo(centerX + endDistance / 2, centerY)
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(finger1Path, 0, 300))
            .addStroke(GestureDescription.StrokeDescription(finger2Path, 0, 300))
            .build()
        
        return dispatchGesture(gesture, callback, null)
    }

    // ==================== Text Input ====================

    fun typeText(text: String, nodeId: Int = -1): Boolean {
        val node = if (nodeId != -1) {
            findNodeById(nodeId)
        } else {
            findFocusedEditableNode()
        }
        
        return node?.let {
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        } ?: false
    }

    fun appendText(text: String): Boolean {
        val node = findFocusedEditableNode() ?: return false
        
        val currentText = node.text?.toString() ?: ""
        val arguments = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                currentText + text
            )
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    fun clearText(): Boolean {
        val node = findFocusedEditableNode() ?: return false
        
        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    // ==================== Element Finding ====================

    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        return rootInActiveWindow?.findAccessibilityNodeInfosByText(text)?.firstOrNull()
    }

    fun findNodeByViewId(viewId: String): AccessibilityNodeInfo? {
        return rootInActiveWindow?.findAccessibilityNodeInfosByViewId(viewId)?.firstOrNull()
    }

    fun findNodeById(nodeId: Int): AccessibilityNodeInfo? {
        return rootInActiveWindow?.findAccessibilityNodeInfosByViewId("$nodeId")?.firstOrNull()
    }

    fun findFocusedEditableNode(): AccessibilityNodeInfo? {
        return rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
    }

    fun findAllButtons(): List<AccessibilityNodeInfo> {
        return rootInActiveWindow?.findAccessibilityNodeInfosByViewId("")?.filter {
            it.isClickable
        } ?: emptyList()
    }

    fun findAllEditTexts(): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        rootInActiveWindow?.let { root ->
            findNodesByClassName(root, "android.widget.EditText", result)
        }
        return result
    }

    private fun findNodesByClassName(
        node: AccessibilityNodeInfo,
        className: String,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        if (node.className == className) {
            result.add(node)
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { findNodesByClassName(it, className, result) }
        }
    }

    // ==================== WhatsApp Automation ====================

    fun openWhatsAppChat(contactName: String): Boolean {
        // Open WhatsApp
        val whatsappPackage = "com.whatsapp"
        val intent = packageManager.getLaunchIntentForPackage(whatsappPackage)
        
        if (intent == null) {
            Log.e(TAG, "WhatsApp not installed")
            return false
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        
        // Wait for app to load
        handler.postDelayed({
            // Tap on search
            val searchNode = findNodeByViewId("com.whatsapp:id/search_input")
            searchNode?.let {
                tapElement(it)
                
                handler.postDelayed({
                    // Type contact name
                    typeText(contactName)
                    
                    handler.postDelayed({
                        // Tap on the contact
                        val contactNode = findNodeByText(contactName)
                        contactNode?.let { tapElement(it) }
                    }, 500)
                }, 300)
            }
        }, 1000)
        
        return true
    }

    fun sendWhatsAppMessage(message: String): Boolean {
        handler.postDelayed({
            // Find the message input field
            val messageInput = findNodeByViewId("com.whatsapp:id/entry")
            messageInput?.let {
                typeText(message)
                
                handler.postDelayed({
                    // Find and tap send button
                    val sendButton = findNodeByViewId("com.whatsapp:id/send")
                    sendButton?.let { tapElement(it) }
                }, 200)
            }
        }, 500)
        
        return true
    }

    // ==================== Event Handlers ====================

    private fun handleNotification(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val text = event.text?.joinToString(" ") ?: return
        
        Log.d(TAG, "Notification from $packageName: $text")
        
        // Broadcast notification to voice service
        val intent = Intent("com.holly.assistant.NOTIFICATION_RECEIVED").apply {
            putExtra("package", packageName)
            putExtra("text", text)
        }
        sendBroadcast(intent)
    }

    private fun handleWindowChange(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: return
        
        Log.d(TAG, "Window changed: $packageName / $className")
    }
}
