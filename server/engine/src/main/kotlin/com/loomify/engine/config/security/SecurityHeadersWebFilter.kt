package com.loomify.engine.config.security

import com.loomify.engine.authentication.infrastructure.ApplicationSecurityProperties
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class SecurityHeadersWebFilter(
    private val applicationSecurityProperties: ApplicationSecurityProperties = ApplicationSecurityProperties(),
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        // Ensure headers are present before the response is committed.
        exchange.response.beforeCommit {
            val headers: HttpHeaders = exchange.response.headers

            if (!headers.containsKey("Content-Security-Policy") &&
                applicationSecurityProperties.contentSecurityPolicy.isNotBlank()
            ) {
                headers.add(
                    "Content-Security-Policy",
                    applicationSecurityProperties.contentSecurityPolicy
                )
            }
            if (!headers.containsKey("X-Content-Type-Options")) {
                headers.add("X-Content-Type-Options", "nosniff")
            }
            if (!headers.containsKey("X-Frame-Options")) {
                headers.add("X-Frame-Options", "DENY")
            }
            if (!headers.containsKey("Referrer-Policy")) {
                headers.add("Referrer-Policy", "strict-origin-when-cross-origin")
            }
            // Optionally add Content-Language if configured on the response path (controller may set it explicitly)
            Mono.empty()
        }

        return chain.filter(exchange)
    }
}
