package com.example.snippetsearcher.execution

import com.example.snippetsearcher.common.exception.NotFoundException
import com.example.snippetsearcher.execution.dto.ExecutionRequestDTO
import com.example.snippetsearcher.execution.dto.TestExecutionRequestDTO
import com.example.snippetsearcher.execution.model.Status
import com.example.snippetsearcher.execution.runner.RunnerResult
import com.example.snippetsearcher.execution.runner.SnippetRunner
import com.example.snippetsearcher.snippet.SnippetClient
import com.example.snippetsearcher.snippet.dto.EnvResponseDTO
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import model.diagnostic.Diagnostic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class ExecutionServiceTest {

    private lateinit var snippetClient: SnippetClient
    private lateinit var runner: SnippetRunner
    private lateinit var service: ExecutionService

    @BeforeEach
    fun setup() {
        snippetClient = mockk()
        runner = mockk()

        service = ExecutionService(
            snippetClient = snippetClient,
            runners = listOf(runner),
        )
    }

    @Test
    fun `executeSnippet should call runner and return success response`() {
        val userId = UUID.randomUUID()

        val request = ExecutionRequestDTO(
            content = "print(1);",
            language = "printscript",
            version = "1.0",
            inputs = listOf("a"),
        )

        every { runner.supports("printscript") } returns true

        every { snippetClient.getAllEnvs(userId) } returns listOf(
            EnvResponseDTO(UUID.randomUUID(), userId, "x", "10"),
            EnvResponseDTO(UUID.randomUUID(), userId, "y", "20"),
        )

        every {
            runner.run(
                code = "print(1);",
                version = "1.0",
                inputs = listOf("a"),
                envs = mapOf("x" to "10", "y" to "20"),
            )
        } returns RunnerResult(
            success = true,
            output = listOf("1"),
            diagnostics = emptyList(),
            runtimeMs = 15,
        )

        val result = service.executeSnippet(userId, request)

        assertEquals(Status.SUCCESS, result.status)
        assertEquals(listOf("1"), result.output.toList())
        assertEquals(15, result.runtimeMs)

        verify { runner.run(any(), any(), any(), any()) }
    }

    @Test
    fun `executeSnippet should return error status when runner fails`() {
        val userId = UUID.randomUUID()

        val request = ExecutionRequestDTO(
            content = "print(;",
            language = "printscript",
            version = "1.0",
            inputs = emptyList(),
        )

        every { runner.supports("printscript") } returns true
        every { snippetClient.getAllEnvs(userId) } returns emptyList()

        val diag1 = mockk<Diagnostic>()
        val diag2 = mockk<Diagnostic>()
        every { diag1.format() } returns "Syntax Error at 1:1"
        every { diag2.format() } returns "Unexpected token at 1:2"

        every {
            runner.run(any(), any(), any(), any())
        } returns RunnerResult(
            success = false,
            output = emptyList(),
            diagnostics = listOf(diag1, diag2),
            runtimeMs = 22,
        )

        val result = service.executeSnippet(userId, request)

        assertEquals(Status.ERROR, result.status)
        assertEquals(2, result.output.size)
        assertEquals(22, result.runtimeMs)
    }

    @Test
    fun `executeSnippet should throw when language unsupported`() {
        val request = ExecutionRequestDTO(
            content = "print(1);",
            language = "kotlin",
            version = "1",
            inputs = emptyList(),
        )

        every { runner.supports("kotlin") } returns false

        assertThrows(NotFoundException::class.java) {
            service.executeSnippet(UUID.randomUUID(), request)
        }
    }

    @Test
    fun `executeTest should return passed when outputs match`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        val request = TestExecutionRequestDTO(
            content = "print(2);",
            language = "printscript",
            version = "1.0",
            inputs = emptyList(),
            outputs = listOf("2"),
        )

        every { runner.supports("printscript") } returns true
        every { snippetClient.getAllEnvs(userId) } returns emptyList()
        justRun { snippetClient.updateTestStatus(userId, snippetId, testId, Status.PASSED) }

        every {
            runner.run(any(), any(), any(), any())
        } returns RunnerResult(
            success = true,
            output = listOf("2"),
            diagnostics = emptyList(),
            runtimeMs = 10,
        )

        val result = service.executeTest(userId, snippetId, testId, request)

        assertEquals(Status.PASSED, result.status)
        assertTrue(result.errors.isEmpty())

        verify { snippetClient.updateTestStatus(userId, snippetId, testId, Status.PASSED) }
    }

    @Test
    fun `executeTest should return failed when outputs do not match`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        val request = TestExecutionRequestDTO(
            content = "print(2);",
            language = "printscript",
            version = "1",
            inputs = emptyList(),
            outputs = listOf("3"),
        )

        every { runner.supports("printscript") } returns true
        every { snippetClient.getAllEnvs(userId) } returns emptyList()
        justRun { snippetClient.updateTestStatus(userId, snippetId, testId, Status.FAILED) }

        every {
            runner.run(any(), any(), any(), any())
        } returns RunnerResult(
            success = true,
            output = listOf("2"),
            diagnostics = emptyList(),
            runtimeMs = 10,
        )

        val result = service.executeTest(userId, snippetId, testId, request)

        assertEquals(Status.FAILED, result.status)
        assertTrue(result.errors.isEmpty())

        verify { snippetClient.updateTestStatus(userId, snippetId, testId, Status.FAILED) }
    }

    @Test
    fun `executeTest should return errors when runner fails`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        val request = TestExecutionRequestDTO(
            content = "print(;",
            language = "printscript",
            version = "1",
            inputs = emptyList(),
            outputs = emptyList(),
        )

        every { runner.supports("printscript") } returns true
        every { snippetClient.getAllEnvs(userId) } returns emptyList()

        val diag = mockk<Diagnostic>()
        every { diag.format() } returns "Syntax Error at 1:1"

        every {
            runner.run(any(), any(), any(), any())
        } returns RunnerResult(
            success = false,
            output = emptyList(),
            diagnostics = listOf(diag),
            runtimeMs = 25,
        )

        val result = service.executeTest(userId, snippetId, testId, request)

        assertEquals(Status.ERROR, result.status)
        assertEquals(1, result.errors.size)

        verify(exactly = 0) { snippetClient.updateTestStatus(any(), any(), any(), any()) }
    }

    @Test
    fun `executeTest should throw when language unsupported`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        val request = TestExecutionRequestDTO(
            content = "print(1);",
            language = "java",
            version = "1",
            inputs = emptyList(),
            outputs = emptyList(),
        )

        every { runner.supports("java") } returns false

        assertThrows(NotFoundException::class.java) {
            service.executeTest(userId, snippetId, testId, request)
        }
    }

    @Test
    fun `executeTestStateless should return passed when outputs match`() {
        val userId = UUID.randomUUID()

        val request = TestExecutionRequestDTO(
            content = "print(42);",
            language = "printscript",
            version = "1.0",
            inputs = emptyList(),
            outputs = listOf("42"),
        )

        every { runner.supports("printscript") } returns true
        every { snippetClient.getAllEnvs(userId) } returns emptyList()

        every {
            runner.run(any(), any(), any(), any())
        } returns RunnerResult(
            success = true,
            output = listOf("42"),
            diagnostics = emptyList(),
            runtimeMs = 10,
        )

        val result = service.executeTestStateless(userId, request)

        assertEquals(Status.PASSED, result.status)
        assertTrue(result.errors.isEmpty())

        verify(exactly = 0) { snippetClient.updateTestStatus(any(), any(), any(), any()) }
    }

    @Test
    fun `executeTestStateless should return failed when outputs do not match`() {
        val userId = UUID.randomUUID()

        val request = TestExecutionRequestDTO(
            content = "print(42);",
            language = "printscript",
            version = "1.0",
            inputs = emptyList(),
            outputs = listOf("100"),
        )

        every { runner.supports("printscript") } returns true
        every { snippetClient.getAllEnvs(userId) } returns emptyList()

        every {
            runner.run(any(), any(), any(), any())
        } returns RunnerResult(
            success = true,
            output = listOf("42"),
            diagnostics = emptyList(),
            runtimeMs = 10,
        )

        val result = service.executeTestStateless(userId, request)

        assertEquals(Status.FAILED, result.status)
        assertTrue(result.errors.isEmpty())

        verify(exactly = 0) { snippetClient.updateTestStatus(any(), any(), any(), any()) }
    }

    @Test
    fun `executeTestStateless should filter blank output lines`() {
        val userId = UUID.randomUUID()

        val request = TestExecutionRequestDTO(
            content = "println(1); println(); println(2);",
            language = "printscript",
            version = "1.0",
            inputs = emptyList(),
            outputs = listOf("1", "2"),
        )

        every { runner.supports("printscript") } returns true
        every { snippetClient.getAllEnvs(userId) } returns emptyList()

        every {
            runner.run(any(), any(), any(), any())
        } returns RunnerResult(
            success = true,
            output = listOf("1", "", "2"),
            diagnostics = emptyList(),
            runtimeMs = 12,
        )

        val result = service.executeTestStateless(userId, request)

        assertEquals(Status.PASSED, result.status)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `executeTestStateless should use environment variables from snippet client`() {
        val userId = UUID.randomUUID()

        val request = TestExecutionRequestDTO(
            content = "println(x);",
            language = "printscript",
            version = "1.0",
            inputs = emptyList(),
            outputs = listOf("100"),
        )

        every { runner.supports("printscript") } returns true

        every { snippetClient.getAllEnvs(userId) } returns listOf(
            EnvResponseDTO(UUID.randomUUID(), userId, "x", "100"),
            EnvResponseDTO(UUID.randomUUID(), userId, "y", "200"),
        )

        every {
            runner.run(
                code = "println(x);",
                version = "1.0",
                inputs = emptyList(),
                envs = mapOf("x" to "100", "y" to "200"),
            )
        } returns RunnerResult(
            success = true,
            output = listOf("100"),
            diagnostics = emptyList(),
            runtimeMs = 8,
        )

        val result = service.executeTestStateless(userId, request)

        assertEquals(Status.PASSED, result.status)

        verify { snippetClient.getAllEnvs(userId) }
        verify {
            runner.run(
                code = "println(x);",
                version = "1.0",
                inputs = emptyList(),
                envs = mapOf("x" to "100", "y" to "200"),
            )
        }
    }

    @Test
    fun `executeTestStateless should throw when language unsupported`() {
        val userId = UUID.randomUUID()

        val request = TestExecutionRequestDTO(
            content = "print(1);",
            language = "python",
            version = "3.0",
            inputs = emptyList(),
            outputs = emptyList(),
        )

        every { runner.supports("python") } returns false

        assertThrows(NotFoundException::class.java) {
            service.executeTestStateless(userId, request)
        }
    }
}
