package com.example.snippetsearcher.execution

import com.example.snippetsearcher.common.exception.NotFoundException
import com.example.snippetsearcher.execution.dto.ExecutionRequestDTO
import com.example.snippetsearcher.execution.dto.ExecutionResponseDTO
import com.example.snippetsearcher.execution.dto.TestExecutionRequestDTO
import com.example.snippetsearcher.execution.dto.TestExecutionResponseDTO
import com.example.snippetsearcher.execution.model.Status
import com.example.snippetsearcher.execution.runner.SnippetRunner
import com.example.snippetsearcher.snippet.SnippetClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ExecutionService(
    private val snippetClient: SnippetClient,
    private val runners: Collection<SnippetRunner>,
) {

    private val logger = LoggerFactory.getLogger(ExecutionService::class.java)

    private fun getRunner(language: String): SnippetRunner =
        runners.firstOrNull { it.supports(language) }
            ?: throw NotFoundException("Execution not supported for language '$language'")

    fun executeSnippet(
        userId: UUID,
        request: ExecutionRequestDTO,
    ): ExecutionResponseDTO {
        logger.info("Executing snippet: userId=$userId, language=${request.language}, version=${request.version}")

        val runner = getRunner(request.language)

        val inputs = request.inputs
        val envs = snippetClient.getAllEnvs(userId)
            .associate { it.key to it.value }

        logger.debug("Retrieved ${envs.size} environment variables for user: $userId")

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

        val status = if (result.success) Status.SUCCESS else Status.ERROR

        logger.info("Snippet execution completed: userId=$userId, status=$status, runtimeMs=${result.runtimeMs}")

        return ExecutionResponseDTO(
            status = status,
            output = output,
            runtimeMs = result.runtimeMs,
        )
    }

    @Transactional
    fun executeTestStateless(
        userId: UUID,
        request: TestExecutionRequestDTO,
    ): TestExecutionResponseDTO {
        logger.info("Executing stateless test: userId=$userId, language=${request.language}")

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
            logger.warn("Test execution failed: userId=$userId, errors=${result.diagnostics.size}")
            return TestExecutionResponseDTO(
                status = Status.ERROR,
                errors = result.diagnostics.map { it.format() },
            )
        }

        val output = result.output.filterNot { it.isBlank() }
        val status = if (output == request.outputs.toList()) Status.PASSED else Status.FAILED

        logger.info("Stateless test completed: userId=$userId, status=$status")

        return TestExecutionResponseDTO(
            status = status,
            errors = emptyList(),
        )
    }

    @Transactional
    fun executeTest(
        userId: UUID,
        snippetId: UUID,
        testId: UUID,
        request: TestExecutionRequestDTO,
    ): TestExecutionResponseDTO {
        logger.info("Executing test: userId=$userId, snippetId=$snippetId, testId=$testId, language=${request.language}")

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
            logger.warn("Test execution failed: snippetId=$snippetId, testId=$testId, errors=${result.diagnostics.size}")
            return TestExecutionResponseDTO(
                status = Status.ERROR,
                errors = result.diagnostics.map { it.format() },
            )
        }

        val output = result.output.filterNot { it.isBlank() }
        val status = if (output == request.outputs.toList()) Status.PASSED else Status.FAILED

        logger.info("Test execution completed: snippetId=$snippetId, testId=$testId, status=$status")

        snippetClient.updateTestStatus(userId, snippetId, testId, status)

        logger.info("Test status updated successfully: snippetId=$snippetId, testId=$testId")

        return TestExecutionResponseDTO(
            status = status,
            errors = emptyList(),
        )
    }
}
