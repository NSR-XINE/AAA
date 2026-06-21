package com.example.feature.mapper.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.mapper.parser.CpuCore
import com.example.feature.mapper.parser.ThermalZone
import com.example.feature.mapper.parser.BlockDevice

@Composable
fun HardwareDashboardScreen(
    viewModel: HardwareMapperViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    // Pulsing animation for the scanning light
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        viewModel.startScanning()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Futuristic Dashboard Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "System Diagnostics",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "SYSFS NODE MONITOR",
                    color = Color(0xFF00D2FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }

            // Scanning state badge
            Surface(
                color = if (isScanning) Color(0xFF102A1E) else Color(0xFF2C161D),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isScanning) Color(0xFF00E676) else Color(0xFFFF2D55))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(if (isScanning) alphaAnim else 1f)
                            .background(
                                if (isScanning) Color(0xFF00E676) else Color(0xFFFF2D55),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScanning) "LIVE SCANNING" else "OFFLINE",
                        color = if (isScanning) Color(0xFF00E676) else Color(0xFFFF2D55),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00D2FF), strokeWidth = 3.dp)
            }
        } else {
            val metrics = uiState!!

            // CPU Core Frequencies Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827).copy(alpha = 0.45f)),
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CPU FREQUENCY TREE",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CpuCoreLayout(cpus = metrics.cpus)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thermal Zones Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827).copy(alpha = 0.45f)),
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "THERMAL ZONES LOAD",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ThermalsCardLayout(thermals = metrics.thermals)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Block Devices Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827).copy(alpha = 0.45f)),
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "BLOCK STORAGE VOLUMES",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StorageCardLayout(storage = metrics.storage)
                }
            }
        }
    }
}

@Composable
fun CpuCoreLayout(cpus: List<CpuCore>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val chunked = cpus.chunked(2)
        chunked.forEach { rowCores ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowCores.forEach { core ->
                    // Max standard reference frequency is 3.0 GHz for gauge
                    val maxFreq = 3000000L
                    val progress = (core.frequencyHz.toFloat() / maxFreq).coerceIn(0f, 1f)

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF1F2937),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF374151))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CORE ${core.index}",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                // Active dot indicator
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            if (core.frequencyHz > 0) Color(0xFF00D2FF) else Color.Gray,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val freqMhz = core.frequencyHz / 1000.0
                            val displayFreq = if (freqMhz > 0) "${String.format("%.1f", freqMhz)} MHz" else "Offline"
                            
                            Text(
                                text = displayFreq,
                                color = if (core.frequencyHz > 0) Color.White else Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            // Gauge bar
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                                color = Color(0xFF00D2FF),
                                trackColor = Color(0xFF111827)
                            )
                        }
                    }
                }
                if (rowCores.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ThermalsCardLayout(thermals: List<ThermalZone>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        thermals.forEach { zone ->
            val isCritical = zone.temperature >= 80.0
            val progress = (zone.temperature.toFloat() / 100f).coerceIn(0f, 1f)
            
            val statusColor = when {
                isCritical -> Color(0xFFFF2D55) // Critical Red
                zone.temperature > 55.0 -> Color(0xFFFFC400) // Moderate Yellow
                else -> Color(0xFF00E676) // Safe Emerald
            }

            Surface(
                color = Color(0xFF1F2937),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF374151))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = zone.name.replace("_", " ").uppercase(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isCritical) "Throttling Threshold Hit" else "Thermal status stable",
                                color = if (isCritical) Color(0xFFFF2D55) else Color.Gray,
                                fontSize = 9.sp
                            )
                        }

                        Text(
                            text = "${String.format("%.1f", zone.temperature)} °C",
                            color = statusColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Colored progress gauge
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = statusColor,
                        trackColor = Color(0xFF111827)
                    )
                }
            }
        }
    }
}

@Composable
fun StorageCardLayout(storage: List<BlockDevice>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        storage.forEach { dev ->
            val sizeGb = dev.sizeBytes / (1024.0 * 1024.0 * 1024.0)

            Surface(
                color = Color(0xFF1F2937),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF374151))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (dev.isRotational) "💿 " else "⚡ ",
                                fontSize = 16.sp
                            )
                            Column {
                                Text(
                                    text = dev.name.uppercase(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (dev.isRotational) "HDD Device node" else "Flash storage solid state",
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Text(
                            text = "${String.format("%.1f", sizeGb)} GB",
                            color = Color(0xFF00D2FF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Visual partition bar representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFF111827), shape = RoundedCornerShape(3.dp))
                    ) {
                        // Mock partition 1 (System)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.35f)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
                                    ),
                                    shape = RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp)
                                )
                        )
                        // Mock partition 2 (User data)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.8f)
                                .padding(start = 60.dp) // Offset after system
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF0D9488), Color(0xFF00D2FF))
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
