package com.example.workstation

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.shell.ShellExecutor
import kotlinx.coroutines.launch

@Composable
fun DeveloperScreen(
    modifier: Modifier = Modifier
) {
    val shellExecutor = remember { ShellExecutor() }
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var systemInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoadingInfo by remember { mutableStateOf(false) }

    fun refreshInfo() {
        if (isLoadingInfo) return
        isLoadingInfo = true
        coroutineScope.launch {
            systemInfo = try {
                val modelRes = shellExecutor.execute("getprop ro.product.model")
                val osRes = shellExecutor.execute("getprop ro.build.version.release")
                val kernelRes = shellExecutor.execute("uname -r")
                val uptimeRes = shellExecutor.execute("cat /proc/uptime")

                val model = modelRes.stdout.firstOrNull()?.trim() ?: "Android Device"
                val os = osRes.stdout.firstOrNull()?.trim() ?: "Unknown"
                val kernel = kernelRes.stdout.firstOrNull()?.trim() ?: "Unknown"
                
                val uptimeRaw = uptimeRes.stdout.firstOrNull()?.substringBefore(" ")?.trim()
                val uptime = try {
                    val seconds = uptimeRaw?.toDouble()?.toInt() ?: 0
                    val hrs = seconds / 3600
                    val mins = (seconds % 3600) / 60
                    val secs = seconds % 60
                    if (hrs > 0) "${hrs}h ${mins}m ${secs}s" else "${mins}m ${secs}s"
                } catch (e: Exception) {
                    "Unknown"
                }

                val checkRoot = shellExecutor.execute("id")
                val isRoot = checkRoot.stdout.firstOrNull()?.contains("uid=0") == true
                val accessMode = if (isRoot) "Root (Privileged)" else "Standard User Shell"

                mapOf(
                    "Device" to model,
                    "OS Version" to "Android $os",
                    "Kernel" to kernel,
                    "Uptime" to uptime,
                    "Access Mode" to accessMode
                )
            } catch (e: Exception) {
                mapOf(
                    "Device" to "Fallback Device",
                    "OS Version" to "Android (Unknown)",
                    "Kernel" to "Generic Linux",
                    "Uptime" to "Unknown",
                    "Access Mode" to "Standard User Shell"
                )
            } finally {
                isLoadingInfo = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshInfo()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF00D2FF).copy(alpha = glowAlpha),
                                    Color(0xFF0EA5E9).copy(alpha = glowAlpha / 2)
                                )
                            )
                        ),
                        CircleShape
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Icon",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Dev Workstation",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "v1.0.0 (Stable)",
                color = Color(0xFF00D2FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Developer Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.45f)),
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
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "DEVELOPER PROFILE",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "NSR-XINE",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "Lead Systems Architect & Core Developer. Specializing in Android diagnostics, system automation, and low-level customization.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .clickable {
                            uriHandler.openUri("https://github.com/NSR-XINE/AAA")
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🐙", fontSize = 16.sp)
                        Text(
                            text = "GitHub Repository",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "github.com/NSR-XINE/AAA",
                        color = Color(0xFF00D2FF),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Live Engine Diagnostics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.45f)),
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
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE SYSTEM DIAGNOSTICS",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = { refreshInfo() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color(0xFF00D2FF)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        if (isLoadingInfo) {
                            CircularProgressIndicator(
                                color = Color(0xFF00D2FF),
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp
                            )
                        } else {
                            Text("Refresh", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val keys = listOf("Device", "OS Version", "Kernel", "Uptime", "Access Mode")
                    keys.forEach { key ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key,
                                color = Color(0xFF64748B),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = systemInfo[key] ?: (if (isLoadingInfo) "Loading..." else "Unknown"),
                                color = if (key == "Access Mode") {
                                    if (systemInfo[key]?.contains("Root") == true) Color(0xFF10B981) else Color(0xFFF59E0B)
                                } else {
                                    Color.White
                                },
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Technical Stack Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.45f)),
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
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "TECHNICAL ARCHITECTURE",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                val stack = listOf(
                    "Jetpack Compose" to "Purely Declarative UI with Space Dark theme overlays.",
                    "Kotlin Coroutines" to "Non-blocking background context worker pool.",
                    "SharedFlow System Bus" to "Decoupled cross-module event dispatching.",
                    "Libsu Root Worker" to "Privileged terminal executor targeting Shizuku/SU.",
                    "SysFS Node Scanner" to "Diagnostics mapper reading raw system entries."
                )

                stack.forEach { (tech, desc) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00D2FF))
                            )
                            Text(
                                text = tech,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = desc,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
