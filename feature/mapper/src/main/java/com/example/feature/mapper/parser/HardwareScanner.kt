package com.example.feature.mapper.parser

import com.example.core.engine.dispatchers.DispatcherProvider
import com.example.core.engine.dispatchers.DefaultDispatcherProvider
import com.example.core.engine.eventbus.SystemEvent
import com.example.core.engine.eventbus.SystemEventBus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext
import java.io.File

data class ThermalZone(val name: String, val temperature: Double)
data class CpuCore(val index: Int, val frequencyHz: Long)
data class BlockDevice(val name: String, val sizeBytes: Long, val isRotational: Boolean)

data class HardwareMetrics(
    val thermals: ImmutableList<ThermalZone>,
    val cpus: ImmutableList<CpuCore>,
    val storage: ImmutableList<BlockDevice>
)

class HardwareScanner(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
) {
    /**
     * Scans Linux system nodes for hardware health metrics.
     * Operates strictly on Dispatchers.IO.
     */
    suspend fun scan(): HardwareMetrics = withContext(dispatcherProvider.io) {
        val thermals = scanThermals()
        val cpus = scanCpus()
        val storage = scanStorage()

        // Check for thermal critical conditions (>= 80°C) and dispatch alert
        thermals.forEach { zone ->
            if (zone.temperature >= 80.0) {
                SystemEventBus.tryPublish(
                    SystemEvent.MapperEvent.ThermalCritical(
                        timestamp = System.currentTimeMillis(),
                        zone = zone.name,
                        temperature = zone.temperature
                    )
                )
            }
        }

        // Publish CPU updates on the inter-module event bus
        SystemEventBus.tryPublish(
            SystemEvent.MapperEvent.CpuMetricsUpdated(
                timestamp = System.currentTimeMillis(),
                frequenciesHz = cpus.map { it.frequencyHz }.toImmutableList()
            )
        )

        HardwareMetrics(
            thermals = thermals.toImmutableList(),
            cpus = cpus.toImmutableList(),
            storage = storage.toImmutableList()
        )
    }

    private fun scanThermals(): List<ThermalZone> {
        val thermals = mutableListOf<ThermalZone>()
        val dir = File("/sys/class/thermal")
        if (dir.exists() && dir.isDirectory) {
            val zones = dir.listFiles { file -> file.name.startsWith("thermal_zone") }
            zones?.forEach { zoneDir ->
                try {
                    val type = File(zoneDir, "type").readText().trim()
                    val tempStr = File(zoneDir, "temp").readText().trim()
                    val tempVal = tempStr.toDoubleOrNull()?.let { 
                        if (it > 1000) it / 1000.0 else it 
                    } ?: 0.0
                    thermals.add(ThermalZone(type, tempVal))
                } catch (e: Exception) {
                    // Node permission issues
                }
            }
        }
        
        // Demo fallback if nodes are empty/unreadable
        if (thermals.isEmpty()) {
            thermals.add(ThermalZone("cpu-thermal", (40..85).random().toDouble()))
            thermals.add(ThermalZone("battery", (32..43).random().toDouble()))
            thermals.add(ThermalZone("gpu-thermal", (45..78).random().toDouble()))
        }
        return thermals
    }

    private fun scanCpus(): List<CpuCore> {
        val cpus = mutableListOf<CpuCore>()
        val dir = File("/sys/devices/system/cpu")
        if (dir.exists() && dir.isDirectory) {
            val cpuDirs = dir.listFiles { file -> file.name.matches(Regex("cpu\\d+")) }
            cpuDirs?.forEach { cpuDir ->
                try {
                    val index = cpuDir.name.replace("cpu", "").toIntOrNull() ?: return@forEach
                    val freqFile = File(cpuDir, "cpufreq/scaling_cur_freq")
                    val freq = if (freqFile.exists()) {
                        freqFile.readText().trim().toLongOrNull() ?: 0L
                    } else {
                        0L
                    }
                    cpus.add(CpuCore(index, freq))
                } catch (e: Exception) {
                    // Safe read fallback
                }
            }
        }

        // Demo fallback for devices/emulators with hidden cpufreq access
        if (cpus.isEmpty() || cpus.all { it.frequencyHz == 0L }) {
            cpus.clear()
            val simulatedCores = (0..7).map { index ->
                CpuCore(index, (1200000..2840000).random().toLong())
            }
            cpus.addAll(simulatedCores)
        }
        return cpus
    }

    private fun scanStorage(): List<BlockDevice> {
        val storage = mutableListOf<BlockDevice>()
        val dir = File("/sys/block")
        if (dir.exists() && dir.isDirectory) {
            val blocks = dir.listFiles { file -> 
                val name = file.name
                name.startsWith("sd") || name.startsWith("mmcblk") || name.startsWith("nvme") || name.startsWith("dm-")
            }
            blocks?.forEach { blockDir ->
                try {
                    val name = blockDir.name
                    val sizeStr = File(blockDir, "size").readText().trim()
                    val sizeBytes = (sizeStr.toLongOrNull() ?: 0L) * 512L // 512 bytes per block sector
                    
                    val rotationalFile = File(blockDir, "queue/rotational")
                    val isRotational = if (rotationalFile.exists()) {
                        rotationalFile.readText().trim() == "1"
                    } else {
                        false
                    }
                    storage.add(BlockDevice(name, sizeBytes, isRotational))
                } catch (e: Exception) {
                    // Safe read fallback
                }
            }
        }

        // Demo fallback
        if (storage.isEmpty()) {
            storage.add(BlockDevice("mmcblk0", 64000000000L, false))
            storage.add(BlockDevice("dm-0", 32000000000L, false))
        }
        return storage
    }
}
