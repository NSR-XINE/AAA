package com.example.workstation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                    color = MaterialTheme.colorScheme.background
                ) {
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

@Composable
fun WorkstationMainDashboard(
    monitorViewModel: LogMonitorViewModel,
    mapperViewModel: HardwareMapperViewModel,
    automationViewModel: AutomationViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Log Monitor", "Sys Mapper", "Automation", "Developer")

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0B0F19),
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
                            indicatorColor = Color(0xFF1E293B)
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
