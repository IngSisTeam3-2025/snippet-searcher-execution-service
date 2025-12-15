package com.example.snippetsearcher.execution

import com.example.snippetsearcher.common.exception.NotFoundException
import com.example.snippetsearcher.execution.dto.ExecutionRequestDTO
import com.example.snippetsearcher.execution.dto.ExecutionResponseDTO
import com.example.snippetsearcher.execution.dto.TestExecutionRequestDTO
import com.example.snippetsearcher.execution.dto.TestExecutionResponseDTO
import com.example.snippetsearcher.execution.model.Status
import com.example.snippetsearcher.execution.runner.SnippetRunner
import com.example.snippetsearcher.snippet.SnippetClient
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ExecutionService(
    private val snippetClient: SnippetClient,
    private val runners: Collection<SnippetRunner>,
) {

    private fun getRunner(language: String): SnippetRunner =
        runners.firstOrNull { it.supports(language) }
            ?: throw NotFoundException("Execution not supported for language '$language'")

    fun executeSnippet(
        userId: UUID,
        request: ExecutionRequestDTO,
    ): ExecutionResponseDTO {
        val runner = getRunner(request.language)

        val inputs = request.inputs
        val envs = snippetClient.getAllEnvs(userId)
            .associate { it.key to it.value }

        val result = runner.run(
            code = request.content,
            version = request.version,
            inputs = inputs,
            envs = envs,
        )

        val output: Collection<String> =
            if (result.success) {
                result.output
            } else {
                result.diagnostics.map { it.format() }
            }

        return ExecutionResponseDTO(
            status = if (result.success) Status.SUCCESS else Status.ERROR,
            output = output,
            runtimeMs = result.runtimeMs,
        )
    }

    fun executeTest(
        userId: UUID,
        request: TestExecutionRequestDTO,
    ): TestExecutionResponseDTO {
        val runner = getRunner(request.language)

        val envs = snippetClient.getAllEnvs(userId)
            .associate { it.key to it.value }

        val result = runner.run(
            code = request.content,
            version = request.version,
            inputs = request.inputs,
            envs = envs,
        )

        if (!result.success) {
            return TestExecutionResponseDTO(
                status = Status.ERROR,
                errors = result.diagnostics.map { it.format() },
            )
        }

        val output = result.output.filterNot { it.isBlank() }

        val status = if (output == request.outputs.toList()) Status.PASSED else Status.FAILED

        return TestExecutionResponseDTO(
            status = status,
            errors = emptyList(),
        )
    }
}
