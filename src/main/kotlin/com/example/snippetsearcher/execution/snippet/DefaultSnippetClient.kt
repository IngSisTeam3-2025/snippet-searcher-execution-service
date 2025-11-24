package com.example.snippetsearcher.execution.snippet

import com.example.snippetsearcher.execution.common.exception.NotFoundException
import com.example.snippetsearcher.execution.common.web.WebClientFactory
import com.example.snippetsearcher.execution.snippet.dto.SnippetResponseDTO
import com.example.snippetsearcher.execution.snippet.dto.TestResponseDTO
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.*

@Service
class DefaultSnippetClient(
    factory: WebClientFactory,
    config: SnippetServiceConfig,
) : SnippetClient {

    private val webClient = factory.create(config.url)

    override fun getSnippetById(snippetId: UUID): SnippetResponseDTO {
        return webClient.get()
            .uri("/snippets/$snippetId")
            .retrieve()
            .bodyToMono<SnippetResponseDTO>()
            .block() ?: throw NotFoundException("Snippet not found")
    }

    override fun getTestSnippetById(snippetId: UUID, testId: UUID): TestResponseDTO {
        return webClient.get()
            .uri("/api/snippets/$snippetId/tests/$testId")
            .retrieve()
            .bodyToMono<TestResponseDTO>()
            .block() ?: throw NotFoundException("Test not found")
    }
}
