package com.example.snippetsearcher.execution.execution.runner.util

class ExecutionTimer {
    private var start = 0L

    companion object {
        private const val NANOS_TO_MILLIS = 1_000_000
    }

    fun start() {
        start = System.nanoTime()
    }

    fun stop(): Long {
        return (System.nanoTime() - start) / NANOS_TO_MILLIS
    }
}
