package com.example.snippetsearcher.execution.common.web

import org.springframework.web.reactive.function.client.WebClient

class WebClientFactory {
    fun create(baseUrl: String): WebClient =
        WebClient.builder()
            .baseUrl(baseUrl)
            .build()
}
