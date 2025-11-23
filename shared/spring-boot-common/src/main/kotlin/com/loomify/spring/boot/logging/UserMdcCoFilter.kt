package com.loomify.spring.boot.logging

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.util.context.Context

/**
 * Coroutine-based WebFilter that propagates user context to MDC for logging.
 *
 * This filter:
 * 1. Extracts the userId from the JWT token (subject claim)
 * 2. Masks the userId using SHA-256 hashing for security
 * 3. Propagates the masked ID through both:
 *    - MDCContext: For coroutine-based code (services, handlers)
 *    - ReactorContext: For reactive library calls (WebClient, repositories)
 *
 * The masked ID will automatically appear in logs via the Logback pattern
 * configuration using %X{maskedUserId}.
 */
@Component
@Profile("!test")
@Order(Ordered.LOWEST_PRECEDENCE - 10) // Run after most filters, especially security
class UserMdcCoFilter : CoWebFilter() {

    private val logger = LoggerFactory.getLogger(UserMdcCoFilter::class.java)

    override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
        // Extract user ID from security context (JWT subject)
        val userId = extractUserId()

        // Extract workspace ID from header, query param, or body
        var workspaceId: String? = null
        var mutatedExchange = exchange
        val method = exchange.request.method.toString()
        val isJson = exchange.request.headers.contentType?.toString()?.lowercase()?.contains("json") == true
        val isWriteMethod = method == "POST" || method == "PUT" || method == "PATCH"
        if (isWriteMethod && isJson) {
            val dataBuffer = DataBufferUtils.join(exchange.request.body).awaitSingleOrNull()
            if (dataBuffer != null) {
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                DataBufferUtils.release(dataBuffer)
                val body = String(bytes, Charsets.UTF_8)
                val workspaceIdFromBody = extractWorkspaceIdFromString(body)
                if (workspaceIdFromBody != null) {
                    workspaceId = workspaceIdFromBody
                    val cached = exchange.response.bufferFactory().wrap(bytes)
                    val decoratedRequest = object : ServerHttpRequestDecorator(exchange.request) {
                        override fun getBody(): Flux<DataBuffer> = Flux.just(cached)
                    }
                    mutatedExchange = exchange.mutate().request(decoratedRequest).build()
                }
            }
        }
        if (workspaceId == null) {
            workspaceId = extractWorkspaceId(mutatedExchange)
        }

        // Create MDC context map
        val mdcMap = buildMdcMap(userId, workspaceId)

        // Create Reactor context for reactive libraries, preserving upstream context
        val reactorContext = buildReactorContext(userId, workspaceId)

        // Log context establishment (only in debug mode)
        if (logger.isDebugEnabled && userId != null) {
            logger.debug("Established logging context for user: " + LogMasker.mask(userId))
        }

        // Propagate context through both MDC (coroutines) and Reactor (reactive libraries)
        withContext(MDCContext(mdcMap) + ReactorContext(reactorContext)) {
            chain.filter(mutatedExchange)
        }
    }

    /**
     * Extracts userId from JWT token in the security context.
     */
    private suspend fun extractUserId(): String? {
        return try {
            val authentication = withTimeoutOrNull(TIMEOUT_MILLIS) {
                ReactiveSecurityContextHolder.getContext()
                    .map { it.authentication }
                    .awaitSingleOrNull()
            }
            when (val principal = authentication?.principal) {
                is Jwt -> principal.subject
                is String -> principal
                else -> null
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.trace("Could not extract user ID from security context", e)
            null
        }
    }

    /**
     * Extracts workspaceId from header, query param, or request body (JSON).
     * Return count reduced to 3.
     * This method now only checks header and query param, not body.
     */
    @Suppress("ReturnCount")
    private suspend fun extractWorkspaceId(exchange: ServerWebExchange): String? {
        val headerId = exchange.request.headers.getFirst("X-Workspace-Id")
        if (headerId != null) return headerId

        val queryId = exchange.request.queryParams["workspaceId"]?.firstOrNull()
        if (queryId != null) return queryId

        return null
    }

    /**
     * Pure helper to extract workspaceId from a JSON string body.
     */
    private fun extractWorkspaceIdFromString(body: String): String? {
        if (body.isNotBlank()) {
            val regex = """"workspaceId"\s*:\s*"([^"]+)""".toRegex()
            val match = regex.find(body)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    /**
     * Builds the MDC context map with masked identifiers.
     */
    private fun buildMdcMap(userId: String?, workspaceId: String?): Map<String, String> {
        val map = mutableMapOf<String, String>()

        if (userId != null) {
            map[MDC_USER_ID_KEY] = LogMasker.mask(userId)
        }

        if (workspaceId != null) {
            map[MDC_WORKSPACE_ID_KEY] = LogMasker.mask(workspaceId)
        }

        return map
    }

    /**
     * Builds the Reactor context with masked identifiers, preserving upstream context.
     * This ensures reactive library calls (WebClient, R2DBC) can access the context.
     */
    private suspend fun buildReactorContext(userId: String?, workspaceId: String?): Context {
        var context = currentCoroutineContext()[ReactorContext]?.context ?: Context.empty()
        if (userId != null) {
            context = context.put(REACTOR_USER_ID_KEY, LogMasker.mask(userId))
        }
        if (workspaceId != null) {
            context = context.put(REACTOR_WORKSPACE_ID_KEY, LogMasker.mask(workspaceId))
        }
        return context
    }

    companion object {
        const val MDC_USER_ID_KEY = "maskedUserId"
        const val MDC_WORKSPACE_ID_KEY = "maskedWorkspaceId"
        const val REACTOR_USER_ID_KEY = "maskedUserId"
        const val REACTOR_WORKSPACE_ID_KEY = "maskedWorkspaceId"
        private const val TIMEOUT_MILLIS = 100L
    }
}
