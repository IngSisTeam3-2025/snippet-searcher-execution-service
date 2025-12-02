package com.example.snippetsearcher.snippet

import com.example.snippetsearcher.common.client.WebClientFactory
import com.example.snippetsearcher.snippet.dto.EnvResponseDTO
import com.example.snippetsearcher.snippet.dto.SnippetResponseDTO
import com.example.snippetsearcher.snippet.dto.TestResponseDTO
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.UUID

@Service
class DefaultSnippetClient(
    factory: WebClientFactory,
    config: SnippetClientConfig,
) : SnippetClient {
    private val client = factory.create(config.url)

    override fun getSnippet(
        userId: UUID,
        snippetId: UUID,
    ): SnippetResponseDTO {
        return client.get()
            .uri("/api/snippets/$snippetId")
            .header("X-User-Id", userId.toString())
            .retrieve()
            .bodyToMono<SnippetResponseDTO>()
            .block()!!
    }

    override fun getSnippetTest(
        userId: UUID,
        snippetId: UUID,
        testId: UUID,
    ): TestResponseDTO {
        return client.get()
            .uri("/api/snippets/$snippetId/tests/$testId")
            .header("X-User-Id", userId.toString())
            .retrieve()
            .bodyToMono<TestResponseDTO>()
            .block()!!
    }

    override fun getAllEnvs(userId: UUID): Collection<EnvResponseDTO> {
        return client.get()
            .uri("/api/envs")
            .header("X-User-Id", userId.toString())
            .retrieve()
            .bodyToMono<Collection<EnvResponseDTO>>()
            .block()!!
    }
}
