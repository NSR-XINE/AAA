package com.example.feature.monitor.pipeline

import com.example.core.engine.dispatchers.DispatcherProvider
import com.example.core.engine.dispatchers.DefaultDispatcherProvider
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.regex.Pattern

/**
 * Extension on ReceiveChannel to buffer elements into lists based on
 * either maximum item count or time elapsed since the first item in the batch.
 */
fun ReceiveChannel<String>.chunkedFlow(
    timeWindowMs: Long = 50L,
    maxSize: Int = 200
): Flow<List<String>> = flow {
    val buffer = mutableListOf<String>()
    var isChannelClosed = false
    
    while (!isChannelClosed) {
        val startTime = System.currentTimeMillis()
        while (buffer.size < maxSize) {
            val elapsed = System.currentTimeMillis() - startTime
            val remainingTime = timeWindowMs - elapsed
            if (remainingTime <= 0) break
            
            val item = try {
                kotlinx.coroutines.withTimeoutOrNull(remainingTime) {
                    receive()
                }
            } catch (e: ClosedReceiveChannelException) {
                isChannelClosed = true
                null
            } catch (e: Exception) {
                null
            }
            
            if (item != null) {
                buffer.add(item)
            } else {
                break
            }
        }
        
        if (buffer.isNotEmpty()) {
            emit(buffer.toList())
            buffer.clear()
        }
        
        if (isChannelClosed) break
        
        if (buffer.isEmpty()) {
            kotlinx.coroutines.delay(10) // Small yield to prevent hot CPU spinning
        }
    }
}

class LogPipelineProcessor(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
) {
    // Compiled regex pattern for finding level markers like [ERROR], [WARN], etc.
    private val levelPattern = Pattern.compile("\\[(ERROR|WARN|SUCCESS|INFO)\\]", Pattern.CASE_INSENSITIVE)

    /**
     * Map a list of raw log strings into structured, UI-optimized data models.
     * Runs concurrently on Dispatchers.Default.
     */
    suspend fun processBatch(rawLogs: List<String>): List<LogLineUiModel> = withContext(dispatcherProvider.default) {
        rawLogs.map { raw ->
            val matcher = levelPattern.matcher(raw)
            var detectedLevel = LogLevel.UNKNOWN
            var cleanMessage = raw

            if (matcher.find()) {
                detectedLevel = when (matcher.group(1)?.uppercase()) {
                    "ERROR" -> LogLevel.ERROR
                    "WARN", "WARNING" -> LogLevel.WARN
                    "SUCCESS" -> LogLevel.SUCCESS
                    "INFO" -> LogLevel.INFO
                    else -> LogLevel.UNKNOWN
                }
                cleanMessage = raw.replaceRange(matcher.start(), matcher.end(), "").trim()
            } else {
                // Secondary check for standard keywords in the string
                detectedLevel = when {
                    raw.contains("ERROR", ignoreCase = true) -> LogLevel.ERROR
                    raw.contains("WARN", ignoreCase = true) || raw.contains("WARNING", ignoreCase = true) -> LogLevel.WARN
                    raw.contains("SUCCESS", ignoreCase = true) -> LogLevel.SUCCESS
                    raw.contains("INFO", ignoreCase = true) -> LogLevel.INFO
                    else -> LogLevel.UNKNOWN
                }
            }

            // Create a unique stable ID using timestamp, content hash, and a random long
            val timestamp = System.currentTimeMillis()
            val id = "${timestamp}_${raw.hashCode()}_${UUID.randomUUID().mostSignificantBits}"

            LogLineUiModel(
                id = id,
                timestamp = timestamp,
                level = detectedLevel,
                message = cleanMessage
            )
        }
    }
}

/**
 * Transforms a flow of raw log batches to UI-ready model batches.
 */
fun Flow<List<String>>.mapToUiModels(processor: LogPipelineProcessor): Flow<List<LogLineUiModel>> {
    return this.map { batch -> processor.processBatch(batch) }
}
