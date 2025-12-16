package com.example.snippetsearcher.execution.event

import com.example.snippetsearcher.asset.AssetClient
import com.example.snippetsearcher.common.event.EventWrapper
import com.example.snippetsearcher.execution.ExecutionService
import com.example.snippetsearcher.execution.dto.TestExecutionRequestDTO
import com.example.snippetsearcher.execution.model.Status
import com.example.snippetsearcher.snippet.SnippetClient
import com.fasterxml.jackson.databind.ObjectMapper
import org.austral.ingsis.redis.RedisStreamConsumer
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
@Profile("!test")
class TestJobConsumer(
    @Value("\${redis.streams.test-jobs}")
    streamKey: String,
    @Value("\${redis.groups.test-jobs}")
    groupId: String,
    redis: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val executionService: ExecutionService,
    private val assetClient: AssetClient,
    private val snippetClient: SnippetClient,
) : RedisStreamConsumer<EventWrapper>(streamKey, groupId, redis) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val CORRELATION_ID_KEY = "correlationId"
    }

    override fun onMessage(record: ObjectRecord<String, EventWrapper>) {
        val correlationId = UUID.randomUUID().toString()

        try {
            MDC.put(CORRELATION_ID_KEY, correlationId)

            val wrapper = record.value
            val job = objectMapper.readValue(wrapper.payload, TestJobEvent::class.java)

            logger.info("Received test job: snippetId=${job.snippetId}, ownerId=${job.ownerId}, testId=${job.testId}")
            processEvent(job)
        } catch (e: Exception) {
            logger.error("Failed to process test job event", e)
        } finally {
            MDC.clear()
        }
    }

    override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, EventWrapper>> {
        return StreamReceiver.StreamReceiverOptions.builder()
            .pollTimeout(Duration.ofSeconds(2))
            .targetType(EventWrapper::class.java)
            .build()
    }

    private fun processEvent(event: TestJobEvent) {
        try {
            logger.info("Processing test job: snippetId=${event.snippetId}, ownerId=${event.ownerId}, language=${event.language}")

            val content = assetClient.getSnippetContent(event.snippetId)
            logger.info("Retrieved snippet content: snippetId=${event.snippetId}, contentLength=${content.length}")

            val request = TestExecutionRequestDTO(
                content = content,
                language = event.language,
                version = event.version,
                inputs = event.inputs,
                outputs = event.outputs,
            )

            val result = executionService.executeTest(event.ownerId, event.snippetId, event.testId, request)

            if (result.status == Status.ERROR) {
                logger.warn("Test execution completed with errors: snippetId=${event.snippetId}, testId=${event.testId}, errorCount=${result.errors.size}")
            } else {
                logger.info("Test execution successful: snippetId=${event.snippetId}, testId=${event.testId}, status=${result.status}")
            }

            snippetClient.updateTestStatus(event.ownerId, event.snippetId, event.testId, result.status)
            logger.info("Test status updated: snippetId=${event.snippetId}, testId=${event.testId}, status=${result.status}")
        } catch (e: Exception) {
            logger.error("Failed to process test job: snippetId=${event.snippetId}, ownerId=${event.ownerId}, testId=${event.testId}", e)
        }
    }
}
