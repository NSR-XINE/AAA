package com.example.feature.automation.sandbox

import com.example.core.engine.dispatchers.DispatcherProvider
import com.example.core.engine.dispatchers.DefaultDispatcherProvider
import com.example.core.engine.eventbus.SystemEvent
import com.example.core.engine.eventbus.SystemEventBus
import com.example.core.engine.shell.ShellExecutor
import com.example.feature.automation.rules.AutomationRule
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ExecutionHistoryItem(
    val id: String,
    val timestamp: Long,
    val ruleName: String,
    val command: String,
    val status: String,
    val output: String = ""
)

class AutomationSandbox(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
) {
    private val shellExecutor = ShellExecutor(dispatcherProvider)
    private val scope = CoroutineScope(dispatcherProvider.default)

    private val _rules = MutableStateFlow<ImmutableList<AutomationRule>>(persistentListOf())
    val rules: StateFlow<ImmutableList<AutomationRule>> = _rules.asStateFlow()

    private val _history = MutableStateFlow<ImmutableList<ExecutionHistoryItem>>(persistentListOf())
    val history: StateFlow<ImmutableList<ExecutionHistoryItem>> = _history.asStateFlow()

    private var collectionJob: Job? = null
    
    init {
        loadDefaultRules()
    }

    private fun loadDefaultRules() {
        val defaultRules = listOf(
            AutomationRule(
                id = UUID.randomUUID().toString(),
                name = "Thermal Throttling Governer",
                description = "Triggered when CPU or battery thermal zone exceeds 80°C. Shifts CPU governor to powersave mode to cool down devices.",
                triggerType = "MapperEvent.ThermalCritical (>=80°C)",
                shellCommand = "echo 'powersave' > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
            ) { event ->
                event is SystemEvent.MapperEvent.ThermalCritical && event.temperature >= 80.0
            },
            AutomationRule(
                id = UUID.randomUUID().toString(),
                name = "Syslog Error Auditing Logger",
                description = "Monitors system events. If a raw [ERROR] log is received on the bus, dumps system specifications to temporary audit files.",
                triggerType = "LogEvent.RawLogReceived (ERROR)",
                shellCommand = "echo '[AUDIT] Error occurrence caught! Logging build specs' && uname -a"
            ) { event ->
                event is SystemEvent.LogEvent.RawLogReceived && event.level == "ERROR"
            }
        )
        _rules.value = defaultRules.toImmutableList()
    }

    fun start() {
        if (collectionJob != null) return

        collectionJob = scope.launch {
            SystemEventBus.events.collect { event ->
                // Check all active rules
                _rules.value.forEach { rule ->
                    if (rule.isEnabled && rule.condition(event)) {
                        launch {
                            triggerRule(rule, event)
                        }
                    }
                }
            }
        }
    }

    fun stop() {
        collectionJob?.cancel()
        collectionJob = null
    }

    private suspend fun triggerRule(rule: AutomationRule, cause: SystemEvent) {
        val timestamp = System.currentTimeMillis()
        
        // 1. Publish RuleMatched event
        val matchedEvent = SystemEvent.AutomationEvent.RuleMatched(
            timestamp = timestamp,
            ruleName = rule.name,
            command = rule.shellCommand
        )
        SystemEventBus.publish(matchedEvent)

        // Add matching event to history
        val historyItem = ExecutionHistoryItem(
            id = UUID.randomUUID().toString(),
            timestamp = timestamp,
            ruleName = rule.name,
            command = rule.shellCommand,
            status = "Executing..."
        )
        _history.value = (_history.value + historyItem).takeLast(100).toImmutableList()

        // 2. Execute command
        val result = shellExecutor.execute(rule.shellCommand)

        // 3. Publish CommandExecuted event
        val executedEvent = SystemEvent.AutomationEvent.CommandExecuted(
            timestamp = System.currentTimeMillis(),
            command = rule.shellCommand,
            exitCode = result.exitCode,
            output = result.stdout.firstOrNull() ?: result.stderr.firstOrNull() ?: ""
        )
        SystemEventBus.publish(executedEvent)

        // Update item in history
        _history.value = _history.value.map { item ->
            if (item.id == historyItem.id) {
                item.copy(
                    status = if (result.isSuccess) "Completed" else "Failed (Exit ${result.exitCode})",
                    output = (result.stdout + result.stderr).joinToString("\n")
                )
            } else {
                item
            }
        }.toImmutableList()
    }

    fun toggleRule(ruleId: String) {
        _rules.value = _rules.value.map { rule ->
            if (rule.id == ruleId) {
                rule.copy(isEnabled = !rule.isEnabled)
            } else {
                rule
            }
        }.toImmutableList()
    }

    fun addRule(rule: AutomationRule) {
        _rules.value = (_rules.value + rule).toImmutableList()
    }
}
