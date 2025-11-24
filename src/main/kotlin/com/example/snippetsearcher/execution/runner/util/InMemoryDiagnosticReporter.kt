package com.example.snippetsearcher.execution.runner.util

import io.reporter.DiagnosticReporter
import model.diagnostic.Diagnostic

class InMemoryDiagnosticReporter : DiagnosticReporter {

    private val _errors = mutableListOf<Diagnostic>()
    val errors: List<Diagnostic> get() = _errors

    override fun report(diagnostic: Diagnostic) {
        _errors.add(diagnostic)
    }

    fun hasErrors(): Boolean = _errors.isNotEmpty()

    fun getDiagnostics(): List<Diagnostic> = _errors
}
