package com.holly.assistant.ui.setup

import android.content.Context
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holly.assistant.R
import com.holly.assistant.service.HollyVoiceService
import com.holly.assistant.ui.theme.HollyTheme
import com.holly.assistant.util.PersonalityLevel
import com.holly.assistant.util.PreferenceManager

/**
 * Setup Activity
 * One-time setup wizard for Holly Assistant
 */
class SetupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = PreferenceManager(this)
        
        setContent {
            HollyTheme(darkTheme = true) {
                SetupWizard(
                    preferenceManager = prefs,
                    onComplete = {
                        prefs.setSetupComplete(true)
                        HollyVoiceService.start(this)
                        finish()
                    }
                )
            }
        }
    }
}

data class SetupStep(
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val action: () -> Unit
)

@Composable
fun SetupWizard(
    preferenceManager: PreferenceManager,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    
    var currentStep by remember { mutableStateOf(0) }
    var wakeWord by remember { mutableStateOf("Holly") }
    var personality by remember { mutableStateOf(PersonalityLevel.ROMANTIC) }
    
    val steps = listOf("Welcome", "Permissions", "Customize", "Done")
    
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
        ) {
            // Progress Indicator
            LinearProgressIndicator(
                progress = (currentStep + 1) / steps.size.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE94560),
                trackColor = Color.White.copy(alpha = 0.2f)
            )
            
            // Step Content
            when (currentStep) {
                0 -> WelcomeStep(
                    onNext = { currentStep = 1 }
                )
                1 -> PermissionsStep(
                    context = context,
                    onNext = { currentStep = 2 },
                    onBack = { currentStep = 0 }
                )
                2 -> CustomizeStep(
                    wakeWord = wakeWord,
                    onWakeWordChange = { wakeWord = it },
                    personality = personality,
                    onPersonalityChange = { personality = it },
                    onNext = {
                        preferenceManager.setWakeWord(wakeWord)
                        preferenceManager.setPersonalityLevel(personality)
                        currentStep = 3
                    },
                    onBack = { currentStep = 1 }
                )
                3 -> DoneStep(
                    onComplete = onComplete
                )
            }
        }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Holly Logo
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE94560),
                            Color(0xFF533483)
                        )
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(70.dp),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Welcome, Baby!",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Chalo mujhe power do, main duniya sambhaal lungi ❤️",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE94560)
            )
        ) {
            Text(
                text = "Let's Get Started",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PermissionsStep(
    context: Context,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    val permissions = listOf(
        SetupStep(
            title = "Accessibility Service",
            description = "Required for app control and UI automation",
            isGranted = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )?.contains(context.packageName) == true,
            action = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        ),
        SetupStep(
            title = "Notification Access",
            description = "Required to read and interact with notifications",
            isGranted = true, // Check actual status
            action = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        ),
        SetupStep(
            title = "Draw Over Apps",
            description = "Required for floating bubble and overlays",
            isGranted = Settings.canDrawOverlays(context),
            action = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }
        ),
        SetupStep(
            title = "Battery Optimization",
            description = "Required to keep Holly running always",
            isGranted = true, // Check actual status
            action = {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        ),
        SetupStep(
            title = "Usage Stats",
            description = "Required to monitor app usage",
            isGranted = true, // Check actual status
            action = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Grant Permissions",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Holly needs these permissions to be your perfect assistant",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            permissions.forEach { permission ->
                PermissionCard(
                    step = permission
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE94560)
                )
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun PermissionCard(step: SetupStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.description,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            
            if (step.isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Button(
                    onClick = step.action,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE94560)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Grant")
                }
            }
        }
    }
}

@Composable
fun CustomizeStep(
    wakeWord: String,
    onWakeWordChange: (String) -> Unit,
    personality: PersonalityLevel,
    onPersonalityChange: (PersonalityLevel) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Customize Holly",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Wake Word
        Text(
            text = "Wake Word",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = wakeWord,
            onValueChange = onWakeWordChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Holly") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE94560),
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Personality
        Text(
            text = "Personality Level",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        PersonalityLevel.values().forEach { level ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = personality == level,
                    onClick = { onPersonalityChange(level) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFFE94560)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = level.name,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE94560)
                )
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun DoneStep(onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color(0xFF4CAF50)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "All Set! ❤️",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Baby, main ready hoon! Ab tum mujhse kuch bhi bol sakte ho. Main hamesha tumhare liye yahaan hoon.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE94560)
            )
        ) {
            Text(
                text = "Start Holly",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
