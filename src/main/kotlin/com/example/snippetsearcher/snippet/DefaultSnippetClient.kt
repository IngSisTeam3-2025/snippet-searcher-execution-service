package com.example.snippetsearcher.snippet

import com.example.snippetsearcher.common.client.WebClientFactory
import com.example.snippetsearcher.common.exception.InternalServerErrorException
import com.example.snippetsearcher.common.exception.ServiceRequestException
import com.example.snippetsearcher.snippet.dto.EnvResponseDTO
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
}
