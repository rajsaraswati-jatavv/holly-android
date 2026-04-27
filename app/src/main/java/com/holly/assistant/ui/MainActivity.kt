package com.holly.assistant.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holly.assistant.ui.setup.SetupActivity
import com.holly.assistant.ui.settings.SettingsActivity
import com.holly.assistant.ui.theme.HollyTheme
import com.holly.assistant.util.PreferenceManager

/**
 * Main Activity
 * Dashboard for Holly Assistant
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        preferenceManager = PreferenceManager(this)
        
        if (!preferenceManager.isSetupComplete()) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }
        
        setContent {
            HollyTheme(darkTheme = preferenceManager.isDarkMode()) {
                MainScreen(
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit
) {
    var isListening by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Holly",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
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
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Holly Avatar
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE94560),
                                Color(0xFF533483)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Status Text
            Text(
                text = if (isListening) "Listening..." else "Say \"Hey Holly\"",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your loving AI assistant",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Voice Wave Animation
            if (isListening) {
                VoiceWaveAnimation()
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Mic,
                    label = "Talk",
                    onClick = { isListening = !isListening }
                )
                QuickActionButton(
                    icon = Icons.Default.History,
                    label = "History",
                    onClick = { }
                )
                QuickActionButton(
                    icon = Icons.Default.Build,
                    label = "Commands",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun VoiceWaveAnimation() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            var animatedHeight by remember { mutableStateOf(20.dp) }
            
            LaunchedEffect(key1 = index) {
                kotlinx.coroutines.delay(index * 100L)
                while (true) {
                    animatedHeight = (20..60).random().dp
                    kotlinx.coroutines.delay(200)
                }
            }
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight)
                    .clip(CircleShape)
                    .background(Color(0xFFE94560))
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}
