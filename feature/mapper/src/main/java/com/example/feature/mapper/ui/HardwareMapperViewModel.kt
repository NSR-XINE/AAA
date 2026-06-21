package com.example.feature.mapper.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.engine.dispatchers.DispatcherProvider
import com.example.core.engine.dispatchers.DefaultDispatcherProvider
import com.example.feature.mapper.parser.HardwareMetrics
import com.example.feature.mapper.parser.HardwareScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HardwareMapperViewModel(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val scanner = HardwareScanner(dispatcherProvider)

    private val _uiState = MutableStateFlow<HardwareMetrics?>(null)
    val uiState: StateFlow<HardwareMetrics?> = _uiState.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var scanJob: Job? = null

    fun startScanning() {
        if (_isScanning.value) return
        _isScanning.value = true

        scanJob = viewModelScope.launch(dispatcherProvider.default) {
            while (_isScanning.value) {
                val metrics = scanner.scan()
                _uiState.value = metrics
                delay(1500)
            }
        }
    }

    fun stopScanning() {
        _isScanning.value = false
        scanJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }
}
