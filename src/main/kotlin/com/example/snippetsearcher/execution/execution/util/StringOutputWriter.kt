package com.example.snippetsearcher.execution.execution.util

import io.writer.OutputWriter

class StringOutputWriter : OutputWriter {
    private val buffer = StringBuilder()

    override fun write(input: Sequence<String>) {
        for (line in input) {
            buffer.append(line)
        }
    }

    fun getOutput(): String {
        return buffer.toString()
    }
}
