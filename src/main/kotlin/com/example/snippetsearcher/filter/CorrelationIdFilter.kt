package com.example.snippetsearcher.filter

import com.example.snippetsearcher.constants.CorrelationIdConstants.CORRELATION_ID_HEADER
import com.example.snippetsearcher.constants.CorrelationIdConstants.CORRELATION_ID_KEY
import com.newrelic.api.agent.NewRelic
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val correlationId = request.getHeader(CORRELATION_ID_HEADER)

        if (correlationId.isNullOrBlank()) {
            logger.warn("Missing $CORRELATION_ID_KEY for request: method=${request.method}, path=${request.requestURI}")
        }

        try {
            MDC.put(CORRELATION_ID_KEY, correlationId)
            NewRelic.addCustomParameter(CORRELATION_ID_KEY, correlationId)
            response.setHeader(CORRELATION_ID_HEADER, correlationId)

            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
