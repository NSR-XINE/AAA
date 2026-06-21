package com.example.core.engine.eventbus

import kotlinx.collections.immutable.ImmutableList

sealed interface SystemEvent {
    val timestamp: Long

    sealed interface LogEvent : SystemEvent {
        val level: String
        val message: String

        data class RawLogReceived(
            override val timestamp: Long,
            override val level: String,
            override val message: String
        ) : LogEvent
    }

    sealed interface MapperEvent : SystemEvent {
        data class ThermalCritical(
            override val timestamp: Long,
            val zone: String,
            val temperature: Double
        ) : MapperEvent

        data class CpuMetricsUpdated(
            override val timestamp: Long,
            val frequenciesHz: ImmutableList<Long>
        ) : MapperEvent

        data class StorageHealthUpdated(
            override val timestamp: Long,
            val device: String,
            val healthStatus: String
        ) : MapperEvent
    }

    sealed interface AutomationEvent : SystemEvent {
        data class RuleMatched(
            override val timestamp: Long,
            val ruleName: String,
            val command: String
        ) : AutomationEvent

        data class CommandExecuted(
            override val timestamp: Long,
            val command: String,
            val exitCode: Int,
            val output: String
        ) : AutomationEvent
    }
}
