package com.example.snippetsearcher.execution.execution

import com.example.snippetsearcher.execution.env.EnvClient
import com.example.snippetsearcher.execution.execution.dto.ExecutionResponseDTO
import com.example.snippetsearcher.execution.execution.dto.TestExecutionResponseDTO
import com.example.snippetsearcher.execution.runner.LanguageRunner
import com.example.snippetsearcher.execution.snippet.SnippetClient
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ExecutionService(
    private val snippetClient: SnippetClient,
    private val envClient: EnvClient,
    private val runners: List<LanguageRunner>,
) {

    private fun resolveRunner(language: String): LanguageRunner =
        runners.find { it.supports(language) }
            ?: throw IllegalArgumentException("No runner available for language: $language")

    fun executeSnippet(snippetId: UUID): ExecutionResponseDTO {
        val snippet = snippetClient.getSnippetById(snippetId)

        val runner = resolveRunner(snippet.language)

        val inputs = snippet.inputs
        val envs = envClient.getEnvsByOwner(UUID.fromString(snippet.ownerId))
            .associate { it.key to it.value }

        val result = runner.run(
            code = snippet.content,
            version = snippet.version,
            inputs = inputs,
            envs = envs,
        )

        return ExecutionResponseDTO(
            status = if (result.success) "ok" else "error",
            output = if (result.success) result.output else result.diagnostics.joinToString("\n"),
            runtimeMs = result.runtimeMs,
        )
    }

    fun executeTest(snippetId: UUID, testId: UUID): TestExecutionResponseDTO {
        val snippet = snippetClient.getSnippetById(snippetId)
        val test = snippetClient.getTestSnippetById(snippetId, testId)

        val runner = resolveRunner(snippet.language)

        val envs = envClient.getEnvsByOwner(UUID.fromString(snippet.ownerId))
            .associate { it.key to it.value }

        val result = runner.run(
            code = snippet.content,
            version = snippet.version,
            inputs = test.inputs,
            envs = envs,
        )

        if (!result.success) {
            return TestExecutionResponseDTO("failed", result.runtimeMs)
        }

        val actualOutput = result.output.split("\n").filter { it.isNotBlank() }

        return TestExecutionResponseDTO(
            status = if (actualOutput == test.outputs.toList()) "passed" else "failed",
            runtimeMs = result.runtimeMs,
        )
    }
}
