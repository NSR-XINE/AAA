package com.example.feature.monitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.monitor.pipeline.LogLineUiModel
import com.example.feature.monitor.pipeline.LogLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogConsoleScreen(
    viewModel: LogMonitorViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.logs.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    
    val listState = rememberLazyListState()
    var autoScrollEnabled by remember { mutableStateOf(true) }

    // Side-effect to auto-scroll when new logs arrive, only if auto-scroll is enabled
    LaunchedEffect(logs.size) {
        if (autoScrollEnabled && logs.isNotEmpty()) {
            listState.scrollToItem(logs.size - 1)
        }
    }

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }

    // Dynamic counts
    val counts = remember(logs) {
        logs.groupingBy { it.level }.eachCount()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121824)) // Dark slate background
    ) {
        // Stats and control headers
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Stream Dashboard",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Auto-Scroll",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Switch(
                            checked = autoScrollEnabled,
                            onCheckedChange = { autoScrollEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E676),
                                checkedTrackColor = Color(0xFF103426)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stats Chips row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LevelCountChip(label = "SUCCESS", count = counts[LogLevel.SUCCESS] ?: 0, color = Color(0xFF00E676))
                    LevelCountChip(label = "INFO", count = counts[LogLevel.INFO] ?: 0, color = Color(0xFF00D2FF))
                    LevelCountChip(label = "WARN", count = counts[LogLevel.WARN] ?: 0, color = Color(0xFFFFC400))
                    LevelCountChip(label = "ERROR", count = counts[LogLevel.ERROR] ?: 0, color = Color(0xFFFF2D55))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (isStreaming) {
                                viewModel.stopStreaming()
                            } else {
                                viewModel.startStreaming(mock = true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStreaming) Color(0xFFFF2D55) else Color(0xFF00D2FF)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isStreaming) "Stop Engine" else "Simulate Logs",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearLogs() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Clear Terminal")
                    }
                }
            }
        }

        // Terminal Log Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(Color(0xFF0B0F19), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Terminal idle. Tap Simulate Logs above.",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    // Explicit stable keys are set on each log item to bypass redundant recompositions
                    items(
                        items = logs,
                        key = { it.id }
                    ) { logItem ->
                        LogLineRow(logItem = logItem, timeFormatter = timeFormatter)
                    }
                }
            }
        }
    }
}

@Composable
fun LogLineRow(
    logItem: LogLineUiModel,
    timeFormatter: SimpleDateFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timestamp
        Text(
            text = timeFormatter.format(Date(logItem.timestamp)),
            color = Color(0xFF64748B),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.padding(end = 6.dp)
        )

        // Level Badge
        val levelColor = Color(logItem.level.colorValue)
        Box(
            modifier = Modifier
                .background(levelColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = logItem.level.name,
                color = levelColor,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Message body
        Text(
            text = logItem.message,
            color = Color(0xFFE2E8F0),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LevelCountChip(
    label: String,
    count: Int,
    color: Color
) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, shape = RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label: $count",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
