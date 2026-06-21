package com.example.workstation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.monitor.ui.LogConsoleScreen
import com.example.feature.monitor.ui.LogMonitorViewModel
import com.example.feature.mapper.ui.HardwareDashboardScreen
import com.example.feature.mapper.ui.HardwareMapperViewModel
import com.example.feature.automation.ui.AutomationScreen
import com.example.feature.automation.ui.AutomationViewModel

class MainActivity : ComponentActivity() {

    private val monitorViewModel: LogMonitorViewModel by viewModels()
    private val mapperViewModel: HardwareMapperViewModel by viewModels()
    private val automationViewModel: AutomationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF0F172A),
                    surface = Color(0xFF1E293B),
                    primary = Color(0xFF00D2FF),
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    LiquidBackground {
                        WorkstationMainDashboard(
                            monitorViewModel = monitorViewModel,
                            mapperViewModel = mapperViewModel,
                            automationViewModel = automationViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidBackground(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "blobs")
    
    val blob1XState = infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1X"
    )
    val blob1YState = infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1Y"
    )

    val blob2XState = infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = -0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2X"
    )
    val blob2YState = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2Y"
    )

    val blob3XState = infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob3X"
    )
    val blob3YState = infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob3Y"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .drawBehind {
                val width = size.width
                val height = size.height
                val baseRadius = width.coerceAtLeast(height) * 0.35f

                // Read state values inside the draw phase to completely bypass recomposition!
                val b1X = blob1XState.value
                val b1Y = blob1YState.value
                val b2X = blob2XState.value
                val b2Y = blob2YState.value
                val b3X = blob3XState.value
                val b3Y = blob3YState.value

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00D2FF).copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(width * b1X, height * b1Y),
                        radius = baseRadius
                    ),
                    center = Offset(width * b1X, height * b1Y),
                    radius = baseRadius
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF7C3AED).copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(width * b2X, height * b2Y),
                        radius = baseRadius * 1.2f
                    ),
                    center = Offset(width * b2X, height * b2Y),
                    radius = baseRadius * 1.2f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEC4899).copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(width * b3X, height * b3Y),
                        radius = baseRadius * 0.9f
                    ),
                    center = Offset(width * b3X, height * b3Y),
                    radius = baseRadius * 0.9f
                )
            }
    ) {
        content()
    }
}

@Composable
fun WorkstationMainDashboard(
    monitorViewModel: LogMonitorViewModel,
    mapperViewModel: HardwareMapperViewModel,
    automationViewModel: AutomationViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Log Monitor", "Sys Mapper", "Automation", "Developer")

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0B0F19).copy(alpha = 0.65f),
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title, fontSize = 11.sp) },
                        icon = {
                            Text(
                                text = when (index) {
                                    0 -> "📟"
                                    1 -> "🔍"
                                    2 -> "⚙️"
                                    else -> "👨‍💻"
                                },
                                fontSize = 18.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00D2FF),
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color(0xFF00D2FF),
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF1E293B).copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> LogConsoleScreen(viewModel = monitorViewModel)
                1 -> HardwareDashboardScreen(viewModel = mapperViewModel)
                2 -> AutomationScreen(viewModel = automationViewModel)
                3 -> DeveloperScreen()
            }
        }
    }
}
