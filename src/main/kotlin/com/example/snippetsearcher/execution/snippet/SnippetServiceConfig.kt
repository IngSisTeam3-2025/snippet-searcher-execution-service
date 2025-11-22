package com.example.snippetsearcher.execution.snippet

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "services.snippet-service")
class SnippetServiceConfig {
    lateinit var url: String
}