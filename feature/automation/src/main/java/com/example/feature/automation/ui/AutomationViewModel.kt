package com.example.feature.automation.ui

import androidx.lifecycle.ViewModel
import com.example.feature.automation.rules.AutomationRule
import com.example.feature.automation.sandbox.AutomationSandbox
import com.example.feature.automation.sandbox.ExecutionHistoryItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

class AutomationViewModel : ViewModel() {
    private val sandbox = AutomationSandbox()

    val rules: StateFlow<ImmutableList<AutomationRule>> = sandbox.rules
    val history: StateFlow<ImmutableList<ExecutionHistoryItem>> = sandbox.history

    init {
        sandbox.start()
    }

    fun toggleRule(ruleId: String) {
        sandbox.toggleRule(ruleId)
    }

    override fun onCleared() {
        super.onCleared()
        sandbox.stop()
    }
}
