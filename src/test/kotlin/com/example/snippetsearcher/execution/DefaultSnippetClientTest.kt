package com.example.snippetsearcher.execution

import com.example.snippetsearcher.common.client.WebClientFactory
import com.example.snippetsearcher.common.exception.InternalServerErrorException
import com.example.snippetsearcher.execution.model.Status
import com.example.snippetsearcher.snippet.DefaultSnippetClient
import com.example.snippetsearcher.snippet.SnippetClientConfig
import com.example.snippetsearcher.snippet.dto.EnvResponseDTO
import com.example.snippetsearcher.snippet.dto.TestStatusRequestDTO
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.util.UUID

class DefaultSnippetClientTest {

    private lateinit var webClient: WebClient
    private lateinit var requestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*>
    private lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec
    private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>
    private lateinit var requestBodySpec: WebClient.RequestBodySpec
    private lateinit var responseSpec: WebClient.ResponseSpec
    private lateinit var factory: WebClientFactory
    private lateinit var config: SnippetClientConfig
    private lateinit var client: DefaultSnippetClient

    @BeforeEach
    fun setup() {
        webClient = mockk()
        requestHeadersUriSpec = mockk()
        requestBodyUriSpec = mockk()
        requestHeadersSpec = mockk()
        requestBodySpec = mockk()
        responseSpec = mockk()
        factory = mockk()
        config = mockk()

        every { config.url } returns "http://snippet-service"
        every { factory.create("http://snippet-service") } returns webClient

        client = DefaultSnippetClient(factory, config)
    }

    @Test
    fun `getAllEnvs should return collection of environments`() {
        val userId = UUID.randomUUID()
        val env1 = EnvResponseDTO(UUID.randomUUID(), userId, "key1", "value1")
        val env2 = EnvResponseDTO(UUID.randomUUID(), userId, "key2", "value2")
        val expectedEnvs = listOf(env1, env2)

        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("/api/envs") } returns requestHeadersSpec
        every { requestHeadersSpec.header("X-User-Id", userId.toString()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono<Collection<EnvResponseDTO>>() } returns Mono.just(expectedEnvs)

        val result = client.getAllEnvs(userId)

        assertEquals(expectedEnvs, result)
        verify { webClient.get() }
        verify { requestHeadersUriSpec.uri("/api/envs") }
        verify { requestHeadersSpec.header("X-User-Id", userId.toString()) }
    }

    @Test
    fun `getAllEnvs should return empty collection when no environments exist`() {
        val userId = UUID.randomUUID()
        val expectedEnvs = emptyList<EnvResponseDTO>()

        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("/api/envs") } returns requestHeadersSpec
        every { requestHeadersSpec.header("X-User-Id", userId.toString()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono<Collection<EnvResponseDTO>>() } returns Mono.just(expectedEnvs)

        val result = client.getAllEnvs(userId)

        assertEquals(expectedEnvs, result)
    }

    @Test
    fun `getAllEnvs should throw InternalServerErrorException when response is null`() {
        val userId = UUID.randomUUID()

        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("/api/envs") } returns requestHeadersSpec
        every { requestHeadersSpec.header("X-User-Id", userId.toString()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono<Collection<EnvResponseDTO>>() } returns Mono.empty()

        assertThrows(InternalServerErrorException::class.java) {
            client.getAllEnvs(userId)
        }
    }

    @Test
    fun `updateTestStatus should call snippet service with correct parameters`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()
        val status = Status.PASSED

        every { webClient.put() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("/internal/snippets/$snippetId/tests/$testId/status") } returns requestBodySpec
        every { requestBodySpec.header("X-User-Id", userId.toString()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(TestStatusRequestDTO(status)) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(Void::class.java) } returns Mono.empty()

        client.updateTestStatus(userId, snippetId, testId, status)

        verify { webClient.put() }
        verify { requestBodyUriSpec.uri("/internal/snippets/$snippetId/tests/$testId/status") }
        verify { requestBodySpec.header("X-User-Id", userId.toString()) }
        verify { requestBodySpec.bodyValue(TestStatusRequestDTO(status)) }
    }

    @Test
    fun `updateTestStatus should work with FAILED status`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()
        val status = Status.FAILED

        every { webClient.put() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("/internal/snippets/$snippetId/tests/$testId/status") } returns requestBodySpec
        every { requestBodySpec.header("X-User-Id", userId.toString()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(TestStatusRequestDTO(status)) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(Void::class.java) } returns Mono.empty()

        client.updateTestStatus(userId, snippetId, testId, status)

        verify { requestBodySpec.bodyValue(TestStatusRequestDTO(Status.FAILED)) }
    }

    @Test
    fun `updateTestStatus should work with ERROR status`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()
        val status = Status.ERROR

        every { webClient.put() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("/internal/snippets/$snippetId/tests/$testId/status") } returns requestBodySpec
        every { requestBodySpec.header("X-User-Id", userId.toString()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(TestStatusRequestDTO(status)) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(Void::class.java) } returns Mono.empty()

        client.updateTestStatus(userId, snippetId, testId, status)

        verify { requestBodySpec.bodyValue(TestStatusRequestDTO(Status.ERROR)) }
    }
}
