package com.example.feature.automation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Slate dark theme
            .padding(12.dp)
    ) {
        // Module Header
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = "Automation Sandbox",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Event-driven reactive rules running terminal scripts",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        // Section: Rules List
        Text(
            text = "Active Rules",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rules, key = { it.id }) { rule ->
                RuleCard(rule = rule, onToggle = { viewModel.toggleRule(rule.id) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section: Execution History
        Text(
            text = "Execution History Log",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No automations triggered yet.",
                            color = Color.DarkGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Display newest trigger events at the top
                        items(history.reversed(), key = { it.id }) { item ->
                            HistoryRow(item = item, timeFormatter = timeFormatter)
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
            containerColor = if (rule.isEnabled) Color(0xFF1E293B) else Color(0xFF1E293B).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rule.name,
                    color = if (rule.isEnabled) Color.White else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E676),
                        checkedTrackColor = Color(0xFF103426)
                    )
                )
            }
            
            Text(
                text = rule.description,
                color = Color.LightGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Trigger & Shell Payload indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0D9488).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ON: ${rule.triggerType}",
                        color = Color(0xFF2DD4BF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "RUN: ${rule.shellCommand}",
                        color = Color(0xFFC084FC),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryRow(
    item: ExecutionHistoryItem,
    timeFormatter: SimpleDateFormat
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeFormatter.format(Date(item.timestamp)),
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.ruleName,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            val statusColor = when {
                item.status.contains("Completed") -> Color(0xFF00E676)
                item.status.contains("Failed") -> Color(0xFFFF2D55)
                else -> Color(0xFFFFC400) // Executing...
            }
            Text(
                text = item.status,
                color = statusColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        
        if (item.output.isNotBlank()) {
            Text(
                text = item.output.trim(),
                color = Color(0xFFE2E8F0),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 2.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                    .padding(6.dp)
            )
        }
    }
}
