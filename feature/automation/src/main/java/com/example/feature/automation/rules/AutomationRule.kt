package com.example.feature.automation.rules

import com.example.core.engine.eventbus.SystemEvent

data class AutomationRule(
    val id: String,
    val name: String,
    val description: String,
    val triggerType: String, // UI-friendly trigger categorization
    val shellCommand: String,
    val isEnabled: Boolean = true,
    val condition: (SystemEvent) -> Boolean
)
