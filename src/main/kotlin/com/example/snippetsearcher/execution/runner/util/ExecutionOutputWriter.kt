package com.example.snippetsearcher.execution.runner.util

import io.writer.OutputWriter

class ExecutionOutputWriter : OutputWriter {

    private val buffer = mutableListOf<String>()

    override fun write(input: Sequence<String>) {
        buffer.addAll(input.toList())
    }

    fun getOutput(): String =
        buffer.joinToString("\n")
}
