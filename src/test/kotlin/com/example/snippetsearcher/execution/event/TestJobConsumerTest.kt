package com.example.snippetsearcher.execution.event

import com.example.snippetsearcher.asset.AssetClient
import com.example.snippetsearcher.common.event.EventWrapper
import com.example.snippetsearcher.execution.ExecutionService
import com.example.snippetsearcher.execution.dto.TestExecutionResponseDTO
import com.example.snippetsearcher.execution.model.Status
import com.example.snippetsearcher.snippet.SnippetClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import java.util.UUID

class TestJobConsumerTest {

    private lateinit var objectMapper: ObjectMapper
    private lateinit var executionService: ExecutionService
    private lateinit var assetClient: AssetClient
    private lateinit var snippetClient: SnippetClient
    private lateinit var redis: RedisTemplate<String, String>
    private lateinit var consumer: TestJobConsumer

    @BeforeEach
    fun setup() {
        objectMapper = jacksonObjectMapper()
        executionService = mockk()
        assetClient = mockk()
        snippetClient = mockk()
        redis = mockk(relaxed = true)

        consumer = TestJobConsumer(
            streamKey = "test-jobs-stream",
            groupId = "test-jobs-group",
            redis = redis,
            objectMapper = objectMapper,
            executionService = executionService,
            assetClient = assetClient,
            snippetClient = snippetClient,
        )
    }

    @Test
    fun `onMessage should process test job event successfully`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        val event = TestJobEvent(
            testId = testId,
            snippetId = snippetId,
            ownerId = userId,
            language = "printscript",
            version = "1.0",
            inputs = listOf("input1"),
            outputs = listOf("output1"),
        )

        val wrapper = EventWrapper(
            payload = objectMapper.writeValueAsString(event),
        )

        val record = mockk<ObjectRecord<String, EventWrapper>>()
        every { record.value } returns wrapper

        every { assetClient.getSnippetContent(snippetId) } returns "println(5);"

        every {
            executionService.executeTest(userId, snippetId, testId, any())
        } returns TestExecutionResponseDTO(
            status = Status.PASSED,
            errors = emptyList(),
        )

        justRun { snippetClient.updateTestStatus(userId, snippetId, testId, Status.PASSED) }

        assertDoesNotThrow {
            consumer.onMessage(record)
        }

        verify { assetClient.getSnippetContent(snippetId) }
        verify {
            executionService.executeTest(
                userId,
                snippetId,
                testId,
                match { request ->
                    request.content == "println(5);" &&
                        request.language == "printscript" &&
                        request.version == "1.0" &&
                        request.inputs == listOf("input1") &&
                        request.outputs == listOf("output1")
                },
            )
        }
        verify { snippetClient.updateTestStatus(userId, snippetId, testId, Status.PASSED) }
    }

    @Test
    fun `onMessage should handle test execution with errors`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        val event = TestJobEvent(
            testId = testId,
            snippetId = snippetId,
            ownerId = userId,
            language = "printscript",
            version = "1.0",
            inputs = emptyList(),
            outputs = emptyList(),
        )

        val wrapper = EventWrapper(
            payload = objectMapper.writeValueAsString(event),
        )

        val record = mockk<ObjectRecord<String, EventWrapper>>()
        every { record.value } returns wrapper

        every { assetClient.getSnippetContent(snippetId) } returns "let x: number = 'wrong';"

        every {
            executionService.executeTest(userId, snippetId, testId, any())
        } returns TestExecutionResponseDTO(
            status = Status.ERROR,
            errors = listOf("Type mismatch"),
        )

        justRun { snippetClient.updateTestStatus(userId, snippetId, testId, Status.ERROR) }

        assertDoesNotThrow {
            consumer.onMessage(record)
        }

        verify { assetClient.getSnippetContent(snippetId) }
        verify { executionService.executeTest(userId, snippetId, testId, any()) }
        verify { snippetClient.updateTestStatus(userId, snippetId, testId, Status.ERROR) }
    }

    @Test
    fun `onMessage should handle asset client failure gracefully`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        val event = TestJobEvent(
            testId = testId,
            snippetId = snippetId,
            ownerId = userId,
            language = "printscript",
            version = "1.0",
            inputs = emptyList(),
            outputs = emptyList(),
        )

        val wrapper = EventWrapper(
            payload = objectMapper.writeValueAsString(event),
        )

        val record = mockk<ObjectRecord<String, EventWrapper>>()
        every { record.value } returns wrapper

        every { assetClient.getSnippetContent(snippetId) } throws RuntimeException("Asset not found")

        assertDoesNotThrow {
            consumer.onMessage(record)
        }

        verify { assetClient.getSnippetContent(snippetId) }
        verify(exactly = 0) { executionService.executeTest(any(), any(), any(), any()) }
        verify(exactly = 0) { snippetClient.updateTestStatus(any(), any(), any(), any()) }
    }

    @Test
    fun `onMessage should handle execution service failure gracefully`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        val event = TestJobEvent(
            testId = testId,
            snippetId = snippetId,
            ownerId = userId,
            language = "printscript",
            version = "1.0",
            inputs = emptyList(),
            outputs = emptyList(),
        )

        val wrapper = EventWrapper(
            payload = objectMapper.writeValueAsString(event),
        )

        val record = mockk<ObjectRecord<String, EventWrapper>>()
        every { record.value } returns wrapper

        every { assetClient.getSnippetContent(snippetId) } returns "println(5);"

        every {
            executionService.executeTest(userId, snippetId, testId, any())
        } throws RuntimeException("Execution failed")

        assertDoesNotThrow {
            consumer.onMessage(record)
        }

        verify { assetClient.getSnippetContent(snippetId) }
        verify { executionService.executeTest(userId, snippetId, testId, any()) }
        verify(exactly = 0) { snippetClient.updateTestStatus(any(), any(), any(), any()) }
    }

    @Test
    fun `onMessage should handle malformed event payload gracefully`() {
        val wrapper = EventWrapper(
            payload = "invalid json",
        )

        val record = mockk<ObjectRecord<String, EventWrapper>>()
        every { record.value } returns wrapper

        assertDoesNotThrow {
            consumer.onMessage(record)
        }

        verify(exactly = 0) { assetClient.getSnippetContent(any()) }
        verify(exactly = 0) { executionService.executeTest(any(), any(), any(), any()) }
        verify(exactly = 0) { snippetClient.updateTestStatus(any(), any(), any(), any()) }
    }
}
