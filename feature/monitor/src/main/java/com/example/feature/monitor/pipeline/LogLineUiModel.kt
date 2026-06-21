package com.example.feature.monitor.pipeline

enum class LogLevel(val colorValue: Long) {
    INFO(0xFF00D2FF),       // Electric Cyan/Blue
    WARN(0xFFFFC400),       // Neon Amber
    ERROR(0xFFFF2D55),      // Vibrant Rose/Red
    SUCCESS(0xFF00E676),    // Bright Emerald Green
    UNKNOWN(0xFFB0BEC5)     // Cool Slate
}

data class LogLineUiModel(
    val id: String,           // Stable unique key for Jetpack Compose LazyColumn optimization
    val timestamp: Long,
    val level: LogLevel,
    val message: String,
    val tag: String = ""      // Optional parsed tag or category
)
