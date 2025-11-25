package com.example.snippetsearcher.execution.execution.runner.util

import io.reporter.DiagnosticReporter
import model.diagnostic.Diagnostic

class InMemoryDiagnosticReporter : DiagnosticReporter {
    private val errors = mutableListOf<Diagnostic>()

    override fun report(diagnostic: Diagnostic) {
        errors.add(diagnostic)
    }

    fun hasErrors(): Boolean = errors.isNotEmpty()

    fun getDiagnostics(): List<Diagnostic> = errors
}
