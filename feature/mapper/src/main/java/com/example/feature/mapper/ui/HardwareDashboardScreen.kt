package com.example.feature.mapper.ui

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

    // Start scanning loop on entry
    LaunchedEffect(Unit) {
        viewModel.startScanning()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Slate dark background
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Module Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hardware Status Scanner",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Scanning /sys/block, cpufreq & thermal nodes",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // Scanning state light
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isScanning) Color(0xFF00E676) else Color(0xFFFF2D55),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isScanning) "ACTIVE" else "STOPPED",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00D2FF))
            }
        } else {
            val metrics = uiState!!

            // CPU Core Frequencies Section
            Text(
                text = "CPU Core Layout",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            CpuCoreLayout(cpus = metrics.cpus)

            Spacer(modifier = Modifier.height(16.dp))

            // Thermal Zones Section
            Text(
                text = "Thermal Zones",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            ThermalsCardLayout(thermals = metrics.thermals)

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Block Devices Section
            Text(
                text = "Block Storage",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            StorageCardLayout(storage = metrics.storage)
        }
    }
}

@Composable
fun CpuCoreLayout(cpus: List<CpuCore>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val chunked = cpus.chunked(2)
        chunked.forEach { rowCores ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowCores.forEach { core ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "CORE ${core.index}",
                                color = Color(0xFF94A3B8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val freqMhz = core.frequencyHz / 1000.0
                            val displayFreq = if (freqMhz > 0) "${String.format("%.1f", freqMhz)} MHz" else "Offline"
                            Text(
                                text = displayFreq,
                                color = Color(0xFF00D2FF),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            thermals.forEach { zone ->
                val isCritical = zone.temperature >= 80.0
                val textColor = when {
                    isCritical -> Color(0xFFFF2D55)
                    zone.temperature > 55.0 -> Color(0xFFFFC400)
                    else -> Color(0xFF00E676)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = zone.name.replace("_", " ").uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCritical) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .background(Color(0xFFFF2D55).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "THROTTLING",
                                    color = Color(0xFFFF2D55),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "${String.format("%.1f", zone.temperature)} °C",
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StorageCardLayout(storage: List<BlockDevice>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            storage.forEach { dev ->
                val sizeGb = dev.sizeBytes / (1024.0 * 1024.0 * 1024.0)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = dev.name.uppercase(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (dev.isRotational) "HDD Device" else "SSD/Flash Store",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = "${String.format("%.1f", sizeGb)} GB",
                        color = Color(0xFF00D2FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
