package com.example.snippetsearcher.execution.runner.util

import io.writer.OutputWriter

class BufferOutputWriter : OutputWriter {
    private val buffer = mutableListOf<String>()

    override fun write(input: Sequence<String>) {
        buffer.addAll(input)
    }

    fun getOutput(): Collection<String> = buffer
}
