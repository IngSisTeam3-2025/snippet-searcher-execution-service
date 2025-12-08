package com.example.snippetsearcher.execution

import com.example.snippetsearcher.common.exception.InternalServerErrorException
import com.example.snippetsearcher.common.exception.NotFoundException
import com.example.snippetsearcher.common.exception.ServiceRequestException
import com.example.snippetsearcher.execution.dto.ExecutionRequestDTO
import com.example.snippetsearcher.execution.dto.TestExecutionRequestDTO
import com.example.snippetsearcher.execution.runner.RunnerResult
import com.example.snippetsearcher.execution.runner.SnippetRunner
import com.example.snippetsearcher.execution.runner.util.BufferEnvReader
import com.example.snippetsearcher.execution.runner.util.BufferInputReader
import com.example.snippetsearcher.snippet.SnippetClient
import com.example.snippetsearcher.snippet.dto.EnvResponseDTO
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import model.diagnostic.Diagnostic
import model.value.BooleanValue
import model.value.FloatValue
import model.value.IntegerValue
import model.value.StringValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import type.option.Option
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

        assertEquals("success", result.status) // status labels are lowercase
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

        assertEquals("error", result.status)
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

        val request = TestExecutionRequestDTO(
            content = "print(2);",
            language = "printscript",
            version = "1.0",
            inputs = emptyList(),
            outputs = listOf("2"),
        )

        every { runner.supports("printscript") } returns true
        every { snippetClient.getAllEnvs(userId) } returns emptyList()

        every {
            runner.run(any(), any(), any(), any())
        } returns RunnerResult(
            success = true,
            output = listOf("2"),
            diagnostics = emptyList(),
            runtimeMs = 10,
        )

        val result = service.executeTest(userId, request)

        assertEquals("passed", result.status)
        assertTrue(result.errors.isEmpty())
        assertEquals(10, result.runtimeMs)
    }

    @Test
    fun `executeTest should return failed when outputs do not match`() {
        val userId = UUID.randomUUID()

        val request = TestExecutionRequestDTO(
            content = "print(2);",
            language = "printscript",
            version = "1",
            inputs = emptyList(),
            outputs = listOf("3"),
        )

        every { runner.supports("printscript") } returns true
        every { snippetClient.getAllEnvs(userId) } returns emptyList()

        every {
            runner.run(any(), any(), any(), any())
        } returns RunnerResult(
            success = true,
            output = listOf("2"),
            diagnostics = emptyList(),
            runtimeMs = 10,
        )

        val result = service.executeTest(userId, request)

        assertEquals("failed", result.status)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `executeTest should return errors when runner fails`() {
        val userId = UUID.randomUUID()

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

        val result = service.executeTest(userId, request)

        assertEquals("error", result.status)
        assertEquals(1, result.errors.size)
        assertEquals(25, result.runtimeMs)
    }

    @Test
    fun `executeTest should throw when language unsupported`() {
        val request = TestExecutionRequestDTO(
            content = "print(1);",
            language = "java",
            version = "1",
            inputs = emptyList(),
            outputs = emptyList(),
        )

        every { runner.supports("java") } returns false

        assertThrows(NotFoundException::class.java) {
            service.executeTest(UUID.randomUUID(), request)
        }
    }

    // Algunos tests para mas coverage
    @Test
    fun `NotFoundException should store message`() {
        val ex = NotFoundException("Resource missing")
        assertEquals("Resource missing", ex.message)
        assertEquals(HttpStatus.NOT_FOUND, ex.status)
    }

    @Test
    fun `ServiceRequestException should expose status and message`() {
        val ex = ServiceRequestException(HttpStatus.BAD_GATEWAY, "External failed")
        assertEquals("External failed", ex.message)
        assertEquals(HttpStatus.BAD_GATEWAY, ex.status)
    }

    @Test
    fun `InternalServerErrorException should have default message`() {
        val ex = InternalServerErrorException()
        assertEquals("An unexpected error occurred", ex.message)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.status)
    }
    @Test
    fun `BufferEnvReader should parse all supported types`() {
        val reader = BufferEnvReader(
            mapOf(
                "boolTrue" to "true",
                "boolFalse" to "false",
                "intVal" to "42",
                "floatVal" to "3.14",
                "stringVal" to "hello"
            )
        )

        assertEquals(Option.Some(value = BooleanValue(true)), reader.read("boolTrue"))
        assertEquals(Option.Some(value = BooleanValue(false)), reader.read("boolFalse"))
        assertEquals(Option.Some(value = IntegerValue(42)), reader.read("intVal"))
        assertEquals(Option.Some(value = FloatValue(3.14f)), reader.read("floatVal"))
        assertEquals(Option.Some(value = StringValue("hello")), reader.read("stringVal"))
    }

    @Test
    fun `BufferEnvReader should return None when key missing`() {
        val reader = BufferEnvReader(mapOf())
        assertTrue(reader.read("missing") is Option.None)
    }

    @Test
    fun `BufferInputReader should return characters of first input`() {
        val reader = BufferInputReader(listOf("abc"))
        val result = reader.read().toList()
        assertEquals(listOf('a', 'b', 'c'), result)
    }

    @Test
    fun `BufferInputReader should return empty sequence when no more inputs`() {
        val reader = BufferInputReader(emptyList())
        val result = reader.read()
        assertTrue(result.none())
    }
}
