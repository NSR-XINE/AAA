package com.example.feature.monitor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.engine.dispatchers.DispatcherProvider
import com.example.core.engine.dispatchers.DefaultDispatcherProvider
import com.example.feature.monitor.client.LogWebSocketClient
import com.example.feature.monitor.pipeline.LogLineUiModel
import com.example.feature.monitor.pipeline.LogPipelineProcessor
import com.example.feature.monitor.pipeline.chunkedFlow
import com.example.feature.monitor.pipeline.mapToUiModels
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogMonitorViewModel(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val client = LogWebSocketClient()
    private val processor = LogPipelineProcessor(dispatcherProvider)

    private val _logs = MutableStateFlow<ImmutableList<LogLineUiModel>>(persistentListOf())
    val logs: StateFlow<ImmutableList<LogLineUiModel>> = _logs.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private var streamJob: Job? = null
    private var processJob: Job? = null

    fun startStreaming(mock: Boolean = true, url: String = "") {
        if (_isStreaming.value) return
        _isStreaming.value = true

        // Launch logging receiver
        streamJob = viewModelScope.launch(dispatcherProvider.io) {
            if (mock) {
                client.startMockFeed()
            } else {
                client.connect(url)
            }
        }

        // Launch ingestion and parsing pipeline
        processJob = viewModelScope.launch(dispatcherProvider.default) {
            client.logChannel
                .chunkedFlow(timeWindowMs = 50L, maxSize = 200)
                .mapToUiModels(processor)
                .collect { batch ->
                    _logs.value = (_logs.value + batch)
                        .takeLast(1000) // Caps log display to prevent memory overhead
                        .toImmutableList()
                }
        }
    }

    fun stopStreaming() {
        client.stop()
        streamJob?.cancel()
        processJob?.cancel()
        _isStreaming.value = false
    }

    fun clearLogs() {
        _logs.value = persistentListOf()
    }

    override fun onCleared() {
        super.onCleared()
        stopStreaming()
    }
}
