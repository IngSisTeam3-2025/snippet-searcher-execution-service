package com.example.snippetsearcher.execution.runner.util

import io.reader.input.InputReader

class StringReader(private val string: String) : InputReader {
    override fun read(): Sequence<Char> = string.asSequence()
}
