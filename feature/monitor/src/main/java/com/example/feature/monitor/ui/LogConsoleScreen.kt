package com.example.feature.monitor.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.monitor.pipeline.LogLineUiModel
import com.example.feature.monitor.pipeline.LogLevel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
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
    val context = LocalContext.current
    val exportPath by viewModel.exportPath.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    val filteredLogs = remember(logs, selectedFilter) {
        if (selectedFilter == null) {
            logs
        } else {
            logs.filter { it.level == selectedFilter }.toImmutableList()
        }
    }

    // Auto-scroll logic when filteredLogs size changes
    LaunchedEffect(filteredLogs.size) {
        if (autoScrollEnabled && filteredLogs.isNotEmpty()) {
            listState.scrollToItem(filteredLogs.size - 1)
        }
    }

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val counts = remember(logs) { logs.groupingBy { it.level }.eachCount() }

    // Pulsing alpha for the active logging state
    val infiniteTransition = rememberInfiniteTransition(label = "terminal_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Stats & Operations Controller Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Log Stream Dashboard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isStreaming) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .alpha(pulseAlpha)
                                        .background(Color(0xFF00E676), shape = RoundedCornerShape(2.5.dp))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isStreaming) "STREAM ACTIVE" else "STREAM IDLE",
                                color = if (isStreaming) Color(0xFF00E676) else Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Auto-Scroll",
                            color = Color(0xFF9CA3AF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = autoScrollEnabled,
                            onCheckedChange = { autoScrollEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E676),
                                checkedTrackColor = Color(0xFF102A1E),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF1F2937)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Chips Selector Row (Horizontally Scrollable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LevelCountChip(
                        label = "ALL",
                        count = logs.size,
                        color = Color.White,
                        isSelected = selectedFilter == null,
                        onClick = { viewModel.setFilter(null) }
                    )
                    LevelCountChip(
                        label = "SUCCESS",
                        count = counts[LogLevel.SUCCESS] ?: 0,
                        color = Color(0xFF00E676),
                        isSelected = selectedFilter == LogLevel.SUCCESS,
                        onClick = {
                            viewModel.setFilter(if (selectedFilter == LogLevel.SUCCESS) null else LogLevel.SUCCESS)
                        }
                    )
                    LevelCountChip(
                        label = "INFO",
                        count = counts[LogLevel.INFO] ?: 0,
                        color = Color(0xFF00D2FF),
                        isSelected = selectedFilter == LogLevel.INFO,
                        onClick = {
                            viewModel.setFilter(if (selectedFilter == LogLevel.INFO) null else LogLevel.INFO)
                        }
                    )
                    LevelCountChip(
                        label = "WARN",
                        count = counts[LogLevel.WARN] ?: 0,
                        color = Color(0xFFFFC400),
                        isSelected = selectedFilter == LogLevel.WARN,
                        onClick = {
                            viewModel.setFilter(if (selectedFilter == LogLevel.WARN) null else LogLevel.WARN)
                        }
                    )
                    LevelCountChip(
                        label = "ERROR",
                        count = counts[LogLevel.ERROR] ?: 0,
                        color = Color(0xFFFF2D55),
                        isSelected = selectedFilter == LogLevel.ERROR,
                        onClick = {
                            viewModel.setFilter(if (selectedFilter == LogLevel.ERROR) null else LogLevel.ERROR)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row
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
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isStreaming) "Stop Engine" else "Simulate Logs",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = { viewModel.exportLogs(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Save Logs",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearLogs() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFF4B5563)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Clear", maxLines = 1)
                    }
                }

                // File Write Toast Status Banner
                if (exportPath != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Box(
                            modifier = Modifier.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = exportPath!!,
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Pitch-black terminal log arena
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp)
                .background(Color(0xFF06090F).copy(alpha = 0.55f), shape = RoundedCornerShape(14.dp))
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.02f)
                            )
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(10.dp)
        ) {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CONSOLE IDLE. INITIALIZE SIMULATOR.",
                        color = Color(0xFF4B5563),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            } else if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO LOGS FOUND FOR FILTER MATCH.",
                        color = Color(0xFF4B5563),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(
                        items = filteredLogs,
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
    val levelColor = Color(logItem.level.colorValue)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(levelColor.copy(alpha = 0.04f), shape = RoundedCornerShape(6.dp))
            .border(
                BorderStroke(
                    0.5.dp,
                    Brush.linearGradient(
                        listOf(levelColor.copy(alpha = 0.15f), Color.Transparent)
                    )
                ),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical indicator accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .background(levelColor, shape = RoundedCornerShape(1.5.dp))
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        // Timestamp
        Text(
            text = timeFormatter.format(Date(logItem.timestamp)),
            color = Color(0xFF64748B),
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            modifier = Modifier.padding(end = 6.dp)
        )

        // Level Badge with glowing alpha backgrounds
        Surface(
            color = levelColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(0.5.dp, levelColor.copy(alpha = 0.5f)),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = logItem.level.name,
                color = levelColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
            )
        }

        // Log Message body
        Text(
            text = logItem.message,
            color = Color(0xFFF1F5F9),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LevelCountChip(
    label: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFF1E293B).copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 0.5.dp,
            if (isSelected) color else Color.White.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, shape = RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "($count)",
                color = if (isSelected) color else Color(0xFF475569),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
