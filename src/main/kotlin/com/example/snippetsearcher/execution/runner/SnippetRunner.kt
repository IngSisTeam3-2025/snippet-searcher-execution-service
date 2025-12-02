package com.example.snippetsearcher.execution.runner

import RunnerResult

interface SnippetRunner {
    fun supports(language: String): Boolean

    fun run(
        code: String,
        version: String,
        inputs: Collection<String>,
        envs: Map<String, String>,
    ): RunnerResult
}
