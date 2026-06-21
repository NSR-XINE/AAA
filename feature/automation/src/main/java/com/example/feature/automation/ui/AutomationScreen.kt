package com.example.feature.automation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.automation.rules.AutomationRule
import com.example.feature.automation.sandbox.ExecutionHistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AutomationScreen(
    viewModel: AutomationViewModel,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.rules.collectAsState()
    val history by viewModel.history.collectAsState()
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }

    // Pulsing animation for the active execution status
    val infiniteTransition = rememberInfiniteTransition(label = "execution_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16)) // Space black background
            .padding(16.dp)
    ) {
        // Module Header
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = "Automation Sandbox",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "EVENT DRIVEN SHELL ACTIONS",
                color = Color(0xFF00D2FF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        // Section: Rules List
        Text(
            text = "Active Rules Configuration",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(rules, key = { it.id }) { rule ->
                RuleCard(rule = rule, onToggle = { viewModel.toggleRule(rule.id) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Execution History
        Text(
            text = "Sandbox Execution History Audit",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF06090F)),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFF1F2937))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO SHELL INSTRUCTIONS TRIGGERED YET.",
                            color = Color(0xFF4B5563),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(history.reversed(), key = { it.id }) { item ->
                            HistoryRow(item = item, timeFormatter = timeFormatter, pulseAlpha = pulseAlpha)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RuleCard(
    rule: AutomationRule,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isEnabled) Color(0xFF111827) else Color(0xFF111827).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (rule.isEnabled) Color(0xFF1F2937) else Color(0xFF1F2937).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rule.name,
                    color = if (rule.isEnabled) Color.White else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E676),
                        checkedTrackColor = Color(0xFF102A1E),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF1F2937)
                    )
                )
            }
            
            Text(
                text = rule.description,
                color = if (rule.isEnabled) Color(0xFF9CA3AF) else Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Trigger & Shell Payload tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF0D9488).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF2DD4BF).copy(alpha = if (rule.isEnabled) 0.5f else 0.2f))
                ) {
                    Text(
                        text = "ON: ${rule.triggerType}",
                        color = if (rule.isEnabled) Color(0xFF2DD4BF) else Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFC084FC).copy(alpha = if (rule.isEnabled) 0.5f else 0.2f))
                ) {
                    Text(
                        text = "RUN: ${rule.shellCommand}",
                        color = if (rule.isEnabled) Color(0xFFC084FC) else Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryRow(
    item: ExecutionHistoryItem,
    timeFormatter: SimpleDateFormat,
    pulseAlpha: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeFormatter.format(Date(item.timestamp)),
                    color = Color(0xFF4B5563),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.ruleName,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            val isRunning = item.status.contains("Executing")
            val statusColor = when {
                item.status.contains("Completed") -> Color(0xFF00E676)
                item.status.contains("Failed") -> Color(0xFFFF2D55)
                else -> Color(0xFFFFC400) // Executing...
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isRunning) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(5.dp)
                            .alpha(pulseAlpha)
                            .background(statusColor, shape = RoundedCornerShape(2.5.dp))
                    )
                }
                Text(
                    text = item.status.uppercase(),
                    color = statusColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        
        if (item.output.isNotBlank()) {
            Text(
                text = item.output.trim(),
                color = Color(0xFFE5E7EB),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 2.dp)
                    .background(Color(0xFF1F2937).copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
                    .border(BorderStroke(0.5.dp, Color(0xFF374151)), shape = RoundedCornerShape(4.dp))
                    .padding(6.dp)
            )
        }
    }
}
