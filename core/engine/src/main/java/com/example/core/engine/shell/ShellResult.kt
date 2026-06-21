package com.example.core.engine.shell

import kotlinx.collections.immutable.ImmutableList

data class ShellResult(
    val exitCode: Int,
    val stdout: ImmutableList<String>,
    val stderr: ImmutableList<String>
) {
    val isSuccess: Boolean get() = exitCode == 0
}
