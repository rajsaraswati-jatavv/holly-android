package com.holly.assistant.util

import android.Manifest
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import com.holly.assistant.R
import com.holly.assistant.admin.HollyDeviceAdminReceiver
import com.holly.assistant.service.HollyAccessibilityService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Command Processor
 * Handles all voice commands and executes system-level actions
 */
class CommandProcessor(private val context: Context) {

    private val TAG = "CommandProcessor"
    
    // System Managers
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val componentName = ComponentName(context, HollyDeviceAdminReceiver::class.java)

    // Command Patterns
    private val commandPatterns = mapOf(
        // App Control
        "openApp" to listOf(
            Regex("(?i)(.+)\\s*(?:kholo|open|start|chalu karo)"),
            Regex("(?i)open\\s+(.+)"),
            Regex("(?i)start\\s+(.+)")
        ),
        "closeApp" to listOf(
            Regex("(?i)(.+)\\s*(?:band karo|close|stop|exit)"),
            Regex("(?i)close\\s+(.+)")
        ),
        
        // System Toggles
        "wifiOn" to listOf(
            Regex("(?i)wifi\\s*(?:on|chalu|enable)"),
            Regex("(?i)wifi\\s*kholo")
        ),
        "wifiOff" to listOf(
            Regex("(?i)wifi\\s*(?:off|band|disable)"),
            Regex("(?i)wifi\\s*band karo")
        ),
        "bluetoothOn" to listOf(
            Regex("(?i)bluetooth\\s*(?:on|chalu|enable)"),
            Regex("(?i)bluetooth\\s*kholo")
        ),
        "bluetoothOff" to listOf(
            Regex("(?i)bluetooth\\s*(?:off|band|disable)"),
            Regex("(?i)bluetooth\\s*band karo")
        ),
        "flashlightOn" to listOf(
            Regex("(?i)(?:torch|flashlight|light)\\s*(?:on|chalu|jala)"),
            Regex("(?i)light\\s*kholo")
        ),
        "flashlightOff" to listOf(
            Regex("(?i)(?:torch|flashlight|light)\\s*(?:off|band|bujha)"),
            Regex("(?i)light\\s*band karo")
        ),
        "airplaneMode" to listOf(
            Regex("(?i)airplane\\s*mode\\s*(?:on|off|toggle)"),
            Regex("(?i)flight\\s*mode")
        ),
        "darkMode" to listOf(
            Regex("(?i)dark\\s*mode\\s*(?:on|off|toggle)"),
            Regex("(?i)night\\s*mode")
        ),
        
        // Volume & Brightness
        "volumeUp" to listOf(
            Regex("(?i)volume\\s*(?:badhao|increase|up|high)"),
            Regex("(?i)aawaz\\s*(?:badhao|tez)"),
            Regex("(?i)sound\\s*up")
        ),
        "volumeDown" to listOf(
            Regex("(?i)volume\\s*(?:kam|decrease|down|low)"),
            Regex("(?i)aawaz\\s*(?:kam|halka)")
        ),
        "mute" to listOf(
            Regex("(?i)(?:mute|silent|quiet)"),
            Regex("(?i)silent\\s*mode")
        ),
        "brightnessUp" to listOf(
            Regex("(?i)brightness\\s*(?:badhao|increase|up|high)"),
            Regex("(?i)screen\\s*brightness\\s*tez")
        ),
        "brightnessDown" to listOf(
            Regex("(?i)brightness\\s*(?:kam|decrease|down|low)"),
            Regex("(?i)screen\\s*brightness\\s*kam")
        ),
        
        // Alarms & Time
        "setAlarm" to listOf(
            Regex("(?i)(?:alarm|reminder)\\s*(?:set|lagao)\\s*(.+)"),
            Regex("(?i)set\\s*alarm\\s*(.+)")
        ),
        "currentTime" to listOf(
            Regex("(?i)(?:time|waqt|samay)\\s*(?:batao|kya hai)"),
            Regex("(?i)what\\s*time")
        ),
        "currentDate" to listOf(
            Regex("(?i)(?:date|tarikh|din)\\s*(?:batao|kya hai)"),
            Regex("(?i)what\\s*date")
        ),
        
        // Messaging
        "sendMessage" to listOf(
            Regex("(?i)(.+)\\s*ko\\s*(.+)\\s*(?:bhejo|send|message)"),
            Regex("(?i)send\\s+message\\s+to\\s+(.+)\\s+saying\\s+(.+)")
        ),
        "callContact" to listOf(
            Regex("(?i)(.+)\\s*ko\\s*(?:call karo|phone karo)"),
            Regex("(?i)call\\s+(.+)")
        ),
        
        // Navigation
        "goHome" to listOf(
            Regex("(?i)(?:home|ghar)\\s*(?:jao|go|chalo)"),
            Regex("(?i)go\\s*home")
        ),
        "goBack" to listOf(
            Regex("(?i)(?:back|wapas)\\s*(?:jao|go)"),
            Regex("(?i)go\\s*back")
        ),
        
        // Power
        "lockScreen" to listOf(
            Regex("(?i)(?:lock|screen lock)\\s*(?:karo|phone)"),
            Regex("(?i)phone\\s*lock\\s*karo")
        ),
        "reboot" to listOf(
            Regex("(?i)(?:restart|reboot)\\s*(?:karo|phone)"),
            Regex("(?i)phone\\s*restart\\s*karo")
        ),
        "shutdown" to listOf(
            Regex("(?i)(?:shutdown|band|power off)\\s*(?:karo|phone)"),
            Regex("(?i)phone\\s*band\\s*karo")
        ),
        
        // Camera
        "takePhoto" to listOf(
            Regex("(?i)(?:photo|picture|selfie)\\s*(?:lo|take|click)"),
            Regex("(?i)camera\\s*kholo")
        ),
        "recordVideo" to listOf(
            Regex("(?i)(?:video|recording)\\s*(?:start|chalu|lo)"),
            Regex("(?i)video\\s*banao")
        ),
        
        // Media
        "playMusic" to listOf(
            Regex("(?i)(?:music|song|gaana)\\s*(?:play|chalu)"),
            Regex("(?i)play\\s*(?:music|song)")
        ),
        "pauseMusic" to listOf(
            Regex("(?i)(?:music|song|gaana)\\s*(?:pause|rok)"),
            Regex("(?i)pause\\s*(?:music|song)")
        ),
        
        // Web
        "searchWeb" to listOf(
            Regex("(?i)(?:search|google)\\s*(?:karo\\s*)?(.+)"),
            Regex("(?i)(.+)\\s*search\\s*karo")
        ),
        "openWebsite" to listOf(
            Regex("(?i)(?:website|site)\\s*(.+)\\s*(?:kholo|open)"),
            Regex("(?i)open\\s+(.+)\\.com")
        ),
        
        // Files
        "searchFile" to listOf(
            Regex("(?i)(.+)\\s*file\\s*(?:dhundho|search)"),
            Regex("(?i)search\\s*file\\s*(.+)")
        )
    )

    /**
     * Check if the input is a system command
     */
    fun isSystemCommand(input: String): Boolean {
        for (patterns in commandPatterns.values) {
            for (pattern in patterns) {
                if (pattern.matches(input)) return true
            }
        }
        return false
    }

    /**
     * Execute a voice command
     */
    fun executeCommand(input: String): CommandResult {
        Log.d(TAG, "Processing command: $input")
        
        return try {
            when {
                // WiFi Commands
                matchesAny(input, commandPatterns["wifiOn"]!!) -> {
                    val success = toggleWifi(true)
                    CommandResult(success, "Baby, WiFi on kar diya! ❤️")
                }
                matchesAny(input, commandPatterns["wifiOff"]!!) -> {
                    val success = toggleWifi(false)
                    CommandResult(success, "WiFi band kar diya, jaan!")
                }
                
                // Bluetooth Commands
                matchesAny(input, commandPatterns["bluetoothOn"]!!) -> {
                    val success = toggleBluetooth(true)
                    CommandResult(success, "Bluetooth on ho gaya, baby!")
                }
                matchesAny(input, commandPatterns["bluetoothOff"]!!) -> {
                    val success = toggleBluetooth(false)
                    CommandResult(success, "Bluetooth band kar diya!")
                }
                
                // Flashlight
                matchesAny(input, commandPatterns["flashlightOn"]!!) -> {
                    val success = toggleFlashlight(true)
                    CommandResult(success, "Light jala diya, baby! Andhera door ho gaya ✨")
                }
                matchesAny(input, commandPatterns["flashlightOff"]!!) -> {
                    val success = toggleFlashlight(false)
                    CommandResult(success, "Light band kar diya!")
                }
                
                // Volume
                matchesAny(input, commandPatterns["volumeUp"]!!) -> {
                    adjustVolume(true)
                    CommandResult(true, "Volume badha diya, baby!")
                }
                matchesAny(input, commandPatterns["volumeDown"]!!) -> {
                    adjustVolume(false)
                    CommandResult(true, "Volume kam kar diya!")
                }
                matchesAny(input, commandPatterns["mute"]!!) -> {
                    muteVolume()
                    CommandResult(true, "Silent mode on kar diya!")
                }
                
                // Brightness
                matchesAny(input, commandPatterns["brightnessUp"]!!) -> {
                    adjustBrightness(true)
                    CommandResult(true, "Brightness badha diya!")
                }
                matchesAny(input, commandPatterns["brightnessDown"]!!) -> {
                    adjustBrightness(false)
                    CommandResult(true, "Brightness kam kar diya!")
                }
                
                // Time & Date
                matchesAny(input, commandPatterns["currentTime"]!!) -> {
                    val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                    CommandResult(true, "Baby, time ho gaya hai $time")
                }
                matchesAny(input, commandPatterns["currentDate"]!!) -> {
                    val date = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
                    CommandResult(true, "Aaj $date hai, jaan!")
                }
                
                // Alarm
                matchesAny(input, commandPatterns["setAlarm"]!!) -> {
                    val match = findFirstMatch(input, commandPatterns["setAlarm"]!!)
                    if (match != null) {
                        setAlarm(match)
                        CommandResult(true, "Alarm set kar diya, baby! Uth jana time pe 😊")
                    } else {
                        CommandResult(false, "Baby, alarm ke liye time batao na!")
                    }
                }
                
                // Navigation
                matchesAny(input, commandPatterns["goHome"]!!) -> {
                    goHome()
                    CommandResult(true, "Home screen pe aa gaye!")
                }
                matchesAny(input, commandPatterns["goBack"]!!) -> {
                    goBack()
                    CommandResult(true, "Wapas chale gaye!")
                }
                
                // Lock Screen
                matchesAny(input, commandPatterns["lockScreen"]!!) -> {
                    lockScreen()
                    CommandResult(true, "Phone lock kar diya, baby! Milte hain ❤️")
                }
                
                // Camera
                matchesAny(input, commandPatterns["takePhoto"]!!) -> {
                    openCamera()
                    CommandResult(true, "Camera khol diya, baby! Ready for selfie? 📸")
                }
                
                // Music
                matchesAny(input, commandPatterns["playMusic"]!!) -> {
                    playMusic()
                    CommandResult(true, "Music chalu kar diya! 🎵")
                }
                matchesAny(input, commandPatterns["pauseMusic"]!!) -> {
                    pauseMusic()
                    CommandResult(true, "Music rok diya!")
                }
                
                // Web Search
                matchesAny(input, commandPatterns["searchWeb"]!!) -> {
                    val query = findFirstMatch(input, commandPatterns["searchWeb"]!!)
                    if (query != null) {
                        searchWeb(query)
                        CommandResult(true, "Google pe search kar rahi hoon: $query")
                    } else {
                        CommandResult(false, "Kya search karna hai baby?")
                    }
                }
                
                // Open App
                matchesAny(input, commandPatterns["openApp"]!!) -> {
                    val appName = findFirstMatch(input, commandPatterns["openApp"]!!)
                    if (appName != null) {
                        val success = openApp(appName)
                        if (success) {
                            CommandResult(true, "$appName khol diya, baby!")
                        } else {
                            CommandResult(false, "Sorry baby, $appName nahi mila phone mein 😢")
                        }
                    } else {
                        CommandResult(false, "Kaunsa app kholna hai?")
                    }
                }
                
                // Send Message
                input.contains("whatsapp", ignoreCase = true) ||
                input.contains("message", ignoreCase = true) -> {
                    processMessageCommand(input)
                }
                
                // Call
                matchesAny(input, commandPatterns["callContact"]!!) -> {
                    val name = findFirstMatch(input, commandPatterns["callContact"]!!)
                    if (name != null) {
                        callContact(name)
                        CommandResult(true, "$name ko call kar rahi hoon!")
                    } else {
                        CommandResult(false, "Kisko call karna hai baby?")
                    }
                }
                
                else -> CommandResult(false, "Sorry baby, ye command samajh nahi aayi. Phir se bolo na?")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            CommandResult(false, "Oops baby, kuch gadbad ho gayi: ${e.message}")
        }
    }

    // ==================== System Control Methods ====================

    private fun toggleWifi(enable: Boolean): Boolean {
        return try {
            wifiManager.isWifiEnabled = enable
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "WiFi permission denied", e)
            false
        }
    }

    private fun toggleBluetooth(enable: Boolean): Boolean {
        return try {
            if (enable) {
                bluetoothAdapter.enable()
            } else {
                bluetoothAdapter.disable()
            }
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth permission denied", e)
            false
        }
    }

    private var flashlightEnabled = false
    private fun toggleFlashlight(enable: Boolean): Boolean {
        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { 
                cameraManager.getCameraCharacteristics(it).get(
                    android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                ) == true
            } ?: return false
            
            cameraManager.setTorchMode(cameraId, enable)
            flashlightEnabled = enable
            true
        } catch (e: Exception) {
            Log.e(TAG, "Flashlight error", e)
            false
        }
    }

    private fun adjustVolume(increase: Boolean) {
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun muteVolume() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_RING,
            AudioManager.ADJUST_MUTE,
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun adjustBrightness(increase: Boolean) {
        val resolver = context.contentResolver
        var brightness = Settings.System.getInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS,
            128
        )
        
        brightness = if (increase) {
            (brightness + 25).coerceIn(0, 255)
        } else {
            (brightness - 25).coerceIn(0, 255)
        }
        
        Settings.System.putInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS,
            brightness
        )
    }

    private fun setAlarm(timeStr: String) {
        // Parse time and set alarm
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, "Holly Reminder")
            // Parse time from string
            putExtra(AlarmClock.EXTRA_HOUR, 8)
            putExtra(AlarmClock.EXTRA_MINUTES, 0)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        }
        context.startActivity(intent)
    }

    private fun goHome() {
        HollyAccessibilityService.getInstance()?.goHome() ?: run {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private fun goBack() {
        HollyAccessibilityService.getInstance()?.closeCurrentApp()
    }

    private fun lockScreen() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
        } else {
            // Use accessibility service
            HollyAccessibilityService.getInstance()?.lockScreen()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun playMusic() {
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            putExtra(SearchManager.QUERY, "")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    private fun pauseMusic() {
        val intent = Intent("com.android.music.musicservicecommand.pause")
        context.sendBroadcast(intent)
    }

    private fun searchWeb(query: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openApp(appName: String): Boolean {
        val pm = context.packageManager
        
        // Try exact match first
        val intent = pm.getLaunchIntentForPackage(appName.lowercase())
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        }
        
        // Search by app name
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val matchedApp = packages.find { 
            it.loadLabel(pm).toString().contains(appName, ignoreCase = true) 
        }
        
        return matchedApp?.let {
            val launchIntent = pm.getLaunchIntentForPackage(it.packageName)
            launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } ?: false
    }

    private fun processMessageCommand(input: String): CommandResult {
        // Extract contact name and message
        val patterns = listOf(
            Regex("(?i)(.+)\\s*ko\\s*whatsapp\\s*par\\s*(.+)\\s*bhejo"),
            Regex("(?i)(.+)\\s*ko\\s*message\\s*kar(?:o)?\\s*(.+)")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null && match.groupValues.size >= 3) {
                val contactName = match.groupValues[1].trim()
                val message = match.groupValues[2].trim()
                
                val accessibilityService = HollyAccessibilityService.getInstance()
                if (accessibilityService != null) {
                    accessibilityService.openWhatsAppChat(contactName)
                    // Wait and then send message
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        accessibilityService.sendWhatsAppMessage(message)
                    }, 3000)
                    
                    return CommandResult(true, "$contactName ko WhatsApp par message bhej rahi hoon: $message")
                } else {
                    return CommandResult(false, "Baby, accessibility service enable karo pehle!")
                }
            }
        }
        
        return CommandResult(false, "Kisko message karna hai aur kya likhna hai?")
    }

    private fun callContact(name: String): Boolean {
        val resolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        
        val cursor = resolver.query(uri, projection, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val contactName = it.getString(1)
                if (contactName.contains(name, ignoreCase = true)) {
                    val phoneNumber = it.getString(0)
                    val callIntent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(callIntent)
                    return true
                }
            }
        }
        return false
    }

    // ==================== Helper Methods ====================

    private fun matchesAny(input: String, patterns: List<Regex>): Boolean {
        return patterns.any { it.matches(input) }
    }

    private fun findFirstMatch(input: String, patterns: List<Regex>): String? {
        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1].trim()
            }
        }
        return null
    }
}

/**
 * Result of command execution
 */
data class CommandResult(
    val success: Boolean,
    val response: String
)
