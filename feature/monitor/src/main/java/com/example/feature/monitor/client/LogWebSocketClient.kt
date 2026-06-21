package com.example.feature.monitor.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import java.util.concurrent.atomic.AtomicBoolean

class LogWebSocketClient {
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }
    
    private val _logChannel = Channel<String>(Channel.UNLIMITED)
    val logChannel: ReceiveChannel<String> = _logChannel

    private val isRunning = AtomicBoolean(false)

    suspend fun connect(url: String) {
        if (!isRunning.compareAndSet(false, true)) return
        
        try {
            val session = client.webSocketSession(url)
            while (session.isActive && isRunning.get()) {
                val frame = session.incoming.receive()
                if (frame is Frame.Text) {
                    _logChannel.send(frame.readText())
                }
            }
        } catch (e: Exception) {
            _logChannel.send("[ERROR] WebSocket connection failed: ${e.localizedMessage}")
        } finally {
            isRunning.set(false)
        }
    }

    /**
     * Starts a simulation log feed that generates rapid events (every 5-80ms)
     * to verify the chunking, parsing, and Compose performance.
     */
    suspend fun startMockFeed() {
        if (!isRunning.compareAndSet(false, true)) return
        
        val levels = listOf("[INFO]", "[WARN]", "[ERROR]", "[SUCCESS]")
        val messages = listOf(
            "Service initiated successfully",
            "Database connection latency high",
            "Memory pressure detected in nodes",
            "Thermal limit zone 0 exceeded thresholds",
            "Automated shell script rule executed",
            "Failed to bind to socket port 8080",
            "System properties reloaded",
            "GC overhead threshold warning issued"
        )
        
        var counter = 0L
        while (isRunning.get()) {
            val randomLevel = levels.random()
            val randomMsg = messages.random()
            val timestamp = System.currentTimeMillis()
            _logChannel.send("$randomLevel ($timestamp) | Line: $counter | $randomMsg")
            counter++
            kotlinx.coroutines.delay((5..80).random().toLong())
        }
    }

    fun stop() {
        isRunning.set(false)
        client.close()
    }
}
