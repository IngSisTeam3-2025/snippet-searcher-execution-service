package com.example.snippetsearcher.asset

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "services.asset-service")
class AssetServiceConfig {
    lateinit var url: String
    var container: String = "snippets"
}
