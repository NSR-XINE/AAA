package com.example.core.engine.shell

import com.example.core.engine.dispatchers.DispatcherProvider
import com.example.core.engine.dispatchers.DefaultDispatcherProvider
import com.topjohnwu.superuser.Shell
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ShellExecutor(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
) {
    /**
     * Executes a shell command asynchronously on Dispatchers.IO.
     * Attempts to run via libsu Root shell, falling back to a standard user process shell if root is not granted.
     */
    suspend fun execute(command: String): ShellResult = withContext(dispatcherProvider.io) {
        val isRoot = try {
            Shell.isAppGrantedRoot() == true
        } catch (e: Exception) {
            false
        }

        if (isRoot) {
            try {
                val result = Shell.cmd(command).exec()
                return@withContext ShellResult(
                    exitCode = result.code,
                    stdout = result.out.toImmutableList(),
                    stderr = result.err.toImmutableList()
                )
            } catch (e: Exception) {
                // If Libsu fails, fallback to running in normal shell environment
            }
        }

        executeFallback(command)
    }

    private fun executeFallback(command: String): ShellResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdout = mutableListOf<String>()
            val stderr = mutableListOf<String>()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stdout.add(line!!)
                }
            }

            BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stderr.add(line!!)
                }
            }

            val exitCode = process.waitFor()
            ShellResult(
                exitCode = exitCode,
                stdout = stdout.toImmutableList(),
                stderr = stderr.toImmutableList()
            )
        } catch (e: Exception) {
            ShellResult(
                exitCode = -1,
                stdout = emptyList<String>().toImmutableList(),
                stderr = listOf(e.localizedMessage ?: "Standard execution failed").toImmutableList()
            )
        }
    }
}
