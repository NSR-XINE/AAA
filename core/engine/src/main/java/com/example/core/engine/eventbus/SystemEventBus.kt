package com.example.core.engine.eventbus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SystemEventBus {
    private val _events = MutableSharedFlow<SystemEvent>(
        replay = 0,
        extraBufferCapacity = 1000 // Large capacity to prevent blocking on fast bursts
    )
    val events: SharedFlow<SystemEvent> = _events.asSharedFlow()

    /**
     * Publishes a SystemEvent asynchronously, suspending if the buffer is full.
     */
    suspend fun publish(event: SystemEvent) {
        _events.emit(event)
    }

    /**
     * Attempts to publish a SystemEvent without suspending.
     * Returns true if the event was successfully emitted, false otherwise.
     */
    fun tryPublish(event: SystemEvent): Boolean {
        return _events.tryEmit(event)
    }
}
