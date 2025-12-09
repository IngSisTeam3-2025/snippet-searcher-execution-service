package com.example.snippetsearcher.execution.runner

import model.diagnostic.Diagnostic

data class RunnerResult(
    val success: Boolean,
    val output: Collection<String>,
    val diagnostics: Collection<Diagnostic>,
    val runtimeMs: Long,
)
