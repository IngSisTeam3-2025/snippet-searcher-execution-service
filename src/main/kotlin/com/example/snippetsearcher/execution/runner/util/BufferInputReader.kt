package com.example.snippetsearcher.execution.runner.util

import io.reader.input.InputReader

class BufferInputReader(
    inputs: Collection<String>,
) : InputReader {

    private val queue = ArrayDeque(inputs)

    override fun read(): Sequence<Char> {
        val next = queue.removeFirstOrNull() ?: return emptySequence()
        return next.asSequence()
    }
}
