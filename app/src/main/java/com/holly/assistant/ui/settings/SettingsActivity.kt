package com.holly.assistant.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holly.assistant.service.HollyVoiceService
import com.holly.assistant.ui.theme.HollyTheme
import com.holly.assistant.util.PersonalityLevel
import com.holly.assistant.util.PreferenceManager
import com.holly.assistant.util.VoiceStyle

/**
 * Settings Activity
 * Configuration options for Holly Assistant
 */
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = PreferenceManager(this)
        
        setContent {
            HollyTheme(darkTheme = prefs.isDarkMode()) {
                SettingsScreen(
                    preferenceManager = prefs,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferenceManager: PreferenceManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    var wakeWord by remember { mutableStateOf(preferenceManager.getWakeWord()) }
    var personality by remember { mutableStateOf(preferenceManager.getPersonalityLevel()) }
    var voiceStyle by remember { mutableStateOf(preferenceManager.getVoiceStyle()) }
    var speechRate by remember { mutableStateOf(preferenceManager.getSpeechRate()) }
    var darkMode by remember { mutableStateOf(preferenceManager.isDarkMode()) }
    var userName by remember { mutableStateOf(preferenceManager.getUserName()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E)
                        )
                    )
                )
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Settings
            SettingsSection(title = "User") {
                SettingsTextField(
                    label = "Your Name",
                    value = userName,
                    onValueChange = {
                        userName = it
                        preferenceManager.setUserName(it)
                    }
                )
            }
            
            // Voice Settings
            SettingsSection(title = "Voice") {
                SettingsTextField(
                    label = "Wake Word",
                    value = wakeWord,
                    onValueChange = {
                        wakeWord = it
                        preferenceManager.setWakeWord(it)
                    }
                )
                
                SettingsSlider(
                    label = "Speech Rate",
                    value = speechRate,
                    valueRange = 0.5f..2f,
                    onValueChange = {
                        speechRate = it
                        preferenceManager.setSpeechRate(it)
                    }
                )
                
                SettingsDropdown(
                    label = "Personality",
                    options = PersonalityLevel.values().map { it.name },
                    selected = personality.name,
                    onSelected = {
                        personality = PersonalityLevel.valueOf(it)
                        preferenceManager.setPersonalityLevel(personality)
                    }
                )
                
                SettingsDropdown(
                    label = "Voice Style",
                    options = VoiceStyle.values().map { it.name },
                    selected = voiceStyle.name,
                    onSelected = {
                        voiceStyle = VoiceStyle.valueOf(it)
                        preferenceManager.setVoiceStyle(voiceStyle)
                    }
                )
            }
            
            // Appearance
            SettingsSection(title = "Appearance") {
                SettingsToggle(
                    label = "Dark Mode",
                    description = "Use dark theme",
                    checked = darkMode,
                    onCheckedChange = {
                        darkMode = it
                        preferenceManager.setDarkMode(it)
                    }
                )
            }
            
            // System Settings
            SettingsSection(title = "System") {
                SettingsButton(
                    label = "Accessibility Settings",
                    description = "Configure accessibility permissions",
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                )
                
                SettingsButton(
                    label = "Notification Settings",
                    description = "Configure notification access",
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                )
                
                SettingsButton(
                    label = "Battery Optimization",
                    description = "Disable battery optimization for Holly",
                    onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )
            }
            
            // Service Control
            SettingsSection(title = "Service") {
                SettingsButton(
                    label = "Restart Holly Service",
                    description = "Restart the voice assistant service",
                    onClick = {
                        HollyVoiceService.stop(context)
                        HollyVoiceService.start(context)
                    }
                )
                
                SettingsButton(
                    label = "Stop Holly Service",
                    description = "Stop the voice assistant temporarily",
                    onClick = {
                        HollyVoiceService.stop(context)
                    }
                )
            }
            
            // About
            SettingsSection(title = "About") {
                SettingsInfo(
                    label = "Version",
                    value = "1.0.0"
                )
                
                SettingsInfo(
                    label = "Made with ❤️ for",
                    value = "RS"
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            color = Color(0xFFE94560),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFE94560),
                checkedTrackColor = Color(0xFFE94560).copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE94560),
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

@Composable
fun SettingsSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = String.format("%.1f", value),
                color = Color(0xFFE94560),
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFE94560),
                activeTrackColor = Color(0xFFE94560)
            )
        )
    }
}

@Composable
fun SettingsDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE94560),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsButton(
    label: String,
    description: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SettingsInfo(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
