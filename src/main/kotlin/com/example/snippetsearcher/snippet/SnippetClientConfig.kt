package com.example.snippetsearcher.snippet

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "services.snippet-service")
class SnippetClientConfig {
    lateinit var url: String
}
