package com.cvix.authentication.infrastructure.filter

import com.cvix.authentication.infrastructure.cookie.AuthCookieBuilder
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtCookieOrHeaderFilter : WebFilter {

    private val logger = org.slf4j.LoggerFactory.getLogger(JwtCookieOrHeaderFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void?> {
        val request = exchange.request

        val hasAuthHeader = request.headers[HttpHeaders.AUTHORIZATION]
            ?.any { it.startsWith("Bearer ") } == true

        val cookieToken = request.cookies[AuthCookieBuilder.ACCESS_TOKEN]
            ?.firstOrNull()
            ?.value

        if (hasAuthHeader || cookieToken.isNullOrBlank()) {
            return chain.filter(exchange)
        }

        logger.debug(
            "Injecting Authorization header from ACCESS_TOKEN cookie for request to {}",
            request.uri,
        )

        val mutatedRequest = request.mutate()
            .header(HttpHeaders.AUTHORIZATION, "Bearer $cookieToken")
            .build()

        val mutatedExchange = exchange.mutate().request(mutatedRequest).build()

        return chain.filter(mutatedExchange)
    }
}
