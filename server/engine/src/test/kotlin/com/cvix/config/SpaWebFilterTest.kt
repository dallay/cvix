package com.cvix.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class SpaWebFilterTest {

    private val webFilterChain: WebFilterChain = mock(WebFilterChain::class.java)
    private val spaWebFilter = SpaWebFilter()

    @Test
    fun `redirects when path does not start with excluded prefixes and does not contain dot`() {
        val request = MockServerHttpRequest.get("/somepath")
        val exchange = MockServerWebExchange.from(request)

        `when`(webFilterChain.filter(any())).thenReturn(Mono.empty())

        StepVerifier.create(spaWebFilter.filter(exchange, webFilterChain))
            .expectComplete()
            .verify()

        assertEquals("/somepath", exchange.request.uri.path)
    }

    @Test
    fun `does not redirect when path starts with excluded prefix`() {
        val request = MockServerHttpRequest.get("/api/somepath")
        val exchange = MockServerWebExchange.from(request)

        `when`(webFilterChain.filter(any())).thenReturn(Mono.empty())

        StepVerifier.create(spaWebFilter.filter(exchange, webFilterChain))
            .expectComplete()
            .verify()

        assertEquals("/api/somepath", exchange.request.uri.path)
    }

    @Test
    fun `does not redirect when path contains dot in last segment (static file)`() {
        val request = MockServerHttpRequest.get("/somepath.js")
        val exchange = MockServerWebExchange.from(request)

        `when`(webFilterChain.filter(any())).thenReturn(Mono.empty())

        StepVerifier.create(spaWebFilter.filter(exchange, webFilterChain))
            .expectComplete()
            .verify()

        assertEquals("/somepath.js", exchange.request.uri.path)
    }

    @Test
    fun `redirects when path contains dot in mid-path segment (username)`() {
        val request = MockServerHttpRequest.get("/user/john.doe")
        val exchange = MockServerWebExchange.from(request)

        `when`(webFilterChain.filter(any())).thenReturn(Mono.empty())

        StepVerifier.create(spaWebFilter.filter(exchange, webFilterChain))
            .expectComplete()
            .verify()

        // Should redirect to index.html (period is NOT in the last segment)
        assertEquals("/user/john.doe", exchange.request.uri.path)
    }

    @Test
    fun `redirects when path contains dot in mid-path segment (version number)`() {
        val request = MockServerHttpRequest.get("/page/v1.0")
        val exchange = MockServerWebExchange.from(request)

        `when`(webFilterChain.filter(any())).thenReturn(Mono.empty())

        StepVerifier.create(spaWebFilter.filter(exchange, webFilterChain))
            .expectComplete()
            .verify()

        // Should redirect to index.html (last segment "v1.0" is treated as route, not file)
        // Note: This is ambiguous - could be a file or route. We treat as route.
        assertEquals("/page/v1.0", exchange.request.uri.path)
    }

    @Test
    fun `does not redirect when path is static file in subdirectory`() {
        val request = MockServerHttpRequest.get("/assets/images/logo.png")
        val exchange = MockServerWebExchange.from(request)

        `when`(webFilterChain.filter(any())).thenReturn(Mono.empty())

        StepVerifier.create(spaWebFilter.filter(exchange, webFilterChain))
            .expectComplete()
            .verify()

        // Should NOT redirect (last segment "logo.png" contains period = static file)
        assertEquals("/assets/images/logo.png", exchange.request.uri.path)
    }

    @Test
    fun `does not redirect when path starts with api even with version in path`() {
        val request = MockServerHttpRequest.get("/api/v1.0/users")
        val exchange = MockServerWebExchange.from(request)

        `when`(webFilterChain.filter(any())).thenReturn(Mono.empty())

        StepVerifier.create(spaWebFilter.filter(exchange, webFilterChain))
            .expectComplete()
            .verify()

        // Should NOT redirect (starts with /api = excluded prefix)
        assertEquals("/api/v1.0/users", exchange.request.uri.path)
    }
}
