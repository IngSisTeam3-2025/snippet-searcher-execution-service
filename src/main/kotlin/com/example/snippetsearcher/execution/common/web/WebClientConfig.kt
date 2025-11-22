package com.example.snippetsearcher.execution.common.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebClientConfig {

    @Bean
    fun webClientFactory(): WebClientFactory = WebClientFactory()
}
