package com.example.snippetsearcher.execution.env

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "services.env-service")
class EnvServiceConfig {
    lateinit var url: String
}
