package com.example.snippetsearcher.common.client

import com.example.snippetsearcher.constants.CorrelationIdConstants.CORRELATION_ID_HEADER
import com.example.snippetsearcher.constants.CorrelationIdConstants.CORRELATION_ID_KEY
import org.slf4j.MDC
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.WebClient

class WebClientFactory {
    fun create(baseUrl: String): WebClient =
        WebClient.builder()
            .baseUrl(baseUrl)
            .filter { request, next ->
                val correlationId = MDC.get(CORRELATION_ID_KEY)
                val modifiedRequest = if (correlationId != null) {
                    ClientRequest.from(request)
                        .header(CORRELATION_ID_HEADER, correlationId)
                        .build()
                } else {
                    request
                }
                next.exchange(modifiedRequest)
            }
            .build()
}
