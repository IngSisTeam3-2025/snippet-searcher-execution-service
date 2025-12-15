package com.example.snippetsearcher.asset

import com.example.snippetsearcher.common.client.WebClientFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DefaultAssetClient(
    factory: WebClientFactory,
    config: AssetServiceConfig,
) : AssetClient {
    private val client = factory.create(config.url)
    private val container = config.container

    override fun getSnippetContent(snippetId: UUID): String {
        return client.get()
            .uri("/v1/asset/$container/$snippetId")
            .retrieve()
            .bodyToMono(String::class.java)
            .block()!!
    }
}
