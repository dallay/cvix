package com.loomify.engine.config.security

import com.loomify.UnitTest
import com.loomify.engine.authentication.infrastructure.ApplicationSecurityProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain

@UnitTest
internal class SecurityHeadersWebFilterTest {

    @Test
    fun `filter should add security headers when properties set`() {
        val props = ApplicationSecurityProperties(contentSecurityPolicy = "default-src 'self'")
        val filter = SecurityHeadersWebFilter(props)

        val request = MockServerHttpRequest.get("/").build()
        val exchange = MockServerWebExchange.from(request)

        val chain = WebFilterChain { ex ->
            ex.response.setComplete()
        }

        // run the filter
        filter.filter(exchange, chain).block()

        val headers = exchange.response.headers
        assertEquals("nosniff", headers.getFirst("X-Content-Type-Options"))
        assertEquals("DENY", headers.getFirst("X-Frame-Options"))
        assertEquals("strict-origin-when-cross-origin", headers.getFirst("Referrer-Policy"))
        assertEquals("default-src 'self'", headers.getFirst("Content-Security-Policy"))
    }
}
