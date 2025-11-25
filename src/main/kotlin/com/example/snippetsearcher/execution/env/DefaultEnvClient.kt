package com.example.snippetsearcher.execution.env

import com.example.snippetsearcher.execution.common.web.WebClientFactory
import com.example.snippetsearcher.execution.env.dto.EnvResponseDTO
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.Collections.emptyList
import java.util.UUID

@Component
class DefaultEnvClient(
    factory: WebClientFactory,
    config: EnvServiceConfig,
) : EnvClient {

    private val webClient = factory.create(config.url)

    override fun getEnvsByOwner(ownerId: UUID): Collection<EnvResponseDTO> {
        return webClient.get()
            .uri("/api/envs?ownerId=$ownerId")
            .retrieve()
            .bodyToMono<List<EnvResponseDTO>>()
            .block() ?: emptyList()
    }
}
