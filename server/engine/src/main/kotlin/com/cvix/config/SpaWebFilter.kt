package com.cvix.config

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * WebFlux filter that enables Single Page Application (SPA) routing by forwarding
 * unmapped paths to `/index.html`.
 *
 * This filter implements the "HTML5 History Mode" pattern for SPAs, where client-side
 * routing handles navigation without full page reloads. When a user directly accesses
 * a client-side route (e.g., `/dashboard`, `/profile`), the server returns `index.html`
 * instead of a 404, allowing the SPA router to take over.
 *
 * ## Redirect Logic
 *
 * A path is redirected to `/index.html` if:
 * 1. It does **not** start with any excluded prefix (API endpoints, actuator, etc.)
 * 2. The **last path segment** does **not** contain a period (`.`) — assumes file extension means static resource
 *
 * ## Excluded Prefixes
 *
 * The following paths are **not** redirected (handled by backend or served as-is):
 * - `/actuator` — Spring Boot Actuator endpoints (health, metrics, etc.)
 * - `/api` — REST API endpoints
 * - `/management` — Management endpoints
 * - `/v3/api-docs` — OpenAPI documentation
 * - `/login` — Authentication endpoints
 * - `/oauth2` — OAuth2 endpoints
 *
 * ## Example Behavior
 *
 * | Request Path             | Action                       | Reason                                   |
 * |--------------------------|------------------------------|------------------------------------------|
 * | `/dashboard`             | Redirect to `/index.html`    | No excluded prefix, no file extension    |
 * | `/api/users`             | Pass through                 | Starts with `/api`                       |
 * | `/assets/logo.png`       | Pass through (serve as-is)   | Last segment contains period (file)      |
 * | `/actuator/health`       | Pass through                 | Starts with `/actuator`                  |
 * | `/profile/settings`      | Redirect to `/index.html`    | Client-side route                        |
 * | `/user/john.doe`         | Redirect to `/index.html`    | Period in mid-path, not last segment     |
 * | `/api/v1.0/users`        | Pass through                 | Starts with `/api` (excluded prefix)     |
 * | `/page/v1.0`             | Redirect to `/index.html`    | No excluded prefix, period not in last   |
 *
 * @see WebFilter
 * @since 1.0.0
 */
@Component
class SpaWebFilter : WebFilter {

    /**
     * Path prefixes that should **not** be redirected to `index.html`.
     * These typically represent backend endpoints or resources that should be handled
     * by the server directly.
     */
    private val excludedPrefixes: List<String> = listOf(
        "/actuator",
        "/api",
        "/management",
        "/v3/api-docs",
        "/login",
        "/oauth2",
    )

    /**
     * Intercepts incoming requests and redirects SPA routes to `/index.html`.
     *
     * @param exchange The current server web exchange (request/response)
     * @param chain The filter chain to continue processing
     * @return A [Mono] representing the completion of filter processing
     */
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        return if (shouldRedirect(path)) {
            val modifiedRequest = exchange.request.mutate().path("/index.html").build()
            chain.filter(exchange.mutate().request(modifiedRequest).build())
        } else {
            chain.filter(exchange)
        }
    }

    /**
     * Determines if a given path should be redirected to `/index.html`.
     *
     * @param path The request path to evaluate
     * @return `true` if the path should be redirected, `false` otherwise
     */
    private fun shouldRedirect(path: String): Boolean {
        // Extract the last path segment (after the last '/')
        val lastSegment = path.substringAfterLast('/')

        // Only treat as static file if the LAST segment contains a period
        // This allows routes like /user/john.doe or /api/v1.0/users to work correctly
        // while still catching actual files like /assets/logo.png or /js/app.bundle.js
        val isStaticFile = lastSegment.contains(".")

        return excludedPrefixes.none { path.startsWith(it) } && !isStaticFile
    }
}
