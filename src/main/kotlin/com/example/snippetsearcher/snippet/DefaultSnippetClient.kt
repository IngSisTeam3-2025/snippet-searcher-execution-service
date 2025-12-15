package com.example.snippetsearcher.snippet

import com.example.snippetsearcher.common.client.WebClientFactory
import com.example.snippetsearcher.common.exception.InternalServerErrorException
import com.example.snippetsearcher.common.exception.ServiceRequestException
import com.example.snippetsearcher.execution.model.Status
import com.example.snippetsearcher.snippet.dto.EnvResponseDTO
import com.example.snippetsearcher.snippet.dto.TestStatusRequestDTO
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class DefaultSnippetClient(
    factory: WebClientFactory,
    config: SnippetClientConfig,
) : SnippetClient {

    private val client = factory.create(config.url)

    override fun getAllEnvs(userId: UUID): Collection<EnvResponseDTO> {
        return client.get()
            .uri("/api/envs")
            .header("X-User-Id", userId.toString())
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.bodyToMono(String::class.java).flatMap { body ->
                    val status = HttpStatus.valueOf(response.statusCode().value())
                    val safeMessage = if (status.is4xxClientError) {
                        body
                    } else {
                        "An unexpected error occurred"
                    }
                    Mono.error(ServiceRequestException(status, safeMessage))
                }
            }
            .bodyToMono<Collection<EnvResponseDTO>>()
            .block() ?: throw InternalServerErrorException()
    }

    override fun updateTestStatus(snippetId: UUID, testId: UUID, status: Status) {
        client.put()
            .uri("/internal/snippets/$snippetId/tests/$testId")
            .bodyValue(TestStatusRequestDTO(status))
            .retrieve()
            .bodyToMono(Void::class.java)
            .block()
    }
}
