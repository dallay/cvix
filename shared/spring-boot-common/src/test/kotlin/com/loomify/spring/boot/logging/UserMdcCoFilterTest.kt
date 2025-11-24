package com.loomify.spring.boot.logging

import io.mockk.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Tests for UserMdcCoFilter.
 *
 * Note: These tests verify that the filter executes without errors and calls the downstream chain.
 * They also verify that the Reactor context is properly populated.
 * Direct assertion of MDC context values in coroutine-based code is challenging in unit tests
 * due to the coroutine context propagation model. The actual MDC context propagation is verified
 * through integration tests and manual verification of log output.
 */
class UserMdcCoFilterTest {

    private lateinit var filter: UserMdcCoFilter

    @BeforeEach
    fun setup() {
        filter = UserMdcCoFilter()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        ReactiveSecurityContextHolder.clearContext()
    }

    @Test
    fun `should execute filter chain without errors when JWT present`() {
        // Arrange
        val userId = "user123"
        val jwt = mockJwt(userId)
        val securityContext = createSecurityContext(jwt)
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"))
        var chainCalled = false
        val chain = WebFilterChain {
            chainCalled = true
            Mono.empty()
        }

        // Act & Assert
        StepVerifier.create(
            filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))),
        )
            .verifyComplete()

        assertEquals(true, chainCalled, "Filter chain should be called")
    }

    @Test
    fun `should execute filter chain without errors when String principal present`() {
        // Arrange
        val userId = "stringUser"
        val auth = mockk<Authentication>()
        every { auth.principal } returns userId
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns auth

        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"))
        var chainCalled = false
        val chain = WebFilterChain {
            chainCalled = true
            Mono.empty()
        }

        // Act & Assert
        StepVerifier.create(
            filter.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))),
        )
            .verifyComplete()

        assertEquals(true, chainCalled)
    }

    @Test
    fun `should propagate context to Reactor chain`() {
        // Arrange
        val userId = "user123"
        val jwt = mockJwt(userId)
        val securityContext = createSecurityContext(jwt)
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"))
        var reactorContextUserId: String? = null

        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                reactorContextUserId = ctx.getOrDefault(UserMdcCoFilter.REACTOR_USER_ID_KEY, null)
                Mono.empty()
            }
        }

        // Act
        filter.filter(exchange, chain)
            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
            .block()

        // Assert
        assertNotNull(reactorContextUserId, "Reactor context should contain userId")
        assertEquals(LogMasker.mask(userId), reactorContextUserId)
    }

    @Test
    fun `should extract workspaceId from X-Workspace-Id header`() {
        // Arrange
        val workspaceId = "workspace456"
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header("X-Workspace-Id", workspaceId),
        )
        var reactorContextWorkspaceId: String? = null

        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                reactorContextWorkspaceId = ctx.getOrDefault(UserMdcCoFilter.REACTOR_WORKSPACE_ID_KEY, null)
                Mono.empty()
            }
        }

        // Act
        filter.filter(exchange, chain).block()

        // Assert
        assertNotNull(reactorContextWorkspaceId)
        assertEquals(LogMasker.mask(workspaceId), reactorContextWorkspaceId)
    }

    @Test
    fun `should extract workspaceId from query parameter`() {
        // Arrange
        val workspaceId = "workspace789"
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test?workspaceId=$workspaceId"),
        )
        var reactorContextWorkspaceId: String? = null

        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                reactorContextWorkspaceId = ctx.getOrDefault(UserMdcCoFilter.REACTOR_WORKSPACE_ID_KEY, null)
                Mono.empty()
            }
        }

        // Act
        filter.filter(exchange, chain).block()

        // Assert
        assertNotNull(reactorContextWorkspaceId)
        assertEquals(LogMasker.mask(workspaceId), reactorContextWorkspaceId)
    }

    @Test
    fun `should extract workspaceId from JSON request body`() {
        // Arrange
        val workspaceId = "workspace999"
        val jsonBody = """{"workspaceId": "$workspaceId", "data": "test"}"""
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/test")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody),
        )
        var reactorContextWorkspaceId: String? = null

        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                reactorContextWorkspaceId = ctx.getOrDefault(UserMdcCoFilter.REACTOR_WORKSPACE_ID_KEY, null)
                Mono.empty()
            }
        }

        // Act
        filter.filter(exchange, chain).block()

        // Assert
        assertNotNull(reactorContextWorkspaceId)
        assertEquals(LogMasker.mask(workspaceId), reactorContextWorkspaceId)
    }

    @Test
    fun `should handle missing userId gracefully`() {
        // Arrange - no security context
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"))
        var chainCalled = false
        val chain = WebFilterChain {
            chainCalled = true
            Mono.empty()
        }

        // Act & Assert - should not throw
        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete()

        assertEquals(true, chainCalled)
    }

    @Test
    fun `should handle missing workspaceId gracefully`() {
        // Arrange - no workspace ID in header, query, or body
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"))
        var chainCalled = false
        val chain = WebFilterChain {
            chainCalled = true
            Mono.empty()
        }

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete()

        assertEquals(true, chainCalled)
    }

    @Test
    fun `should prioritize header over query param for workspaceId`() {
        // Arrange
        val headerWorkspaceId = "workspace-header"
        val queryWorkspaceId = "workspace-query"
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test?workspaceId=$queryWorkspaceId")
                .header("X-Workspace-Id", headerWorkspaceId),
        )
        var reactorContextWorkspaceId: String? = null

        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                reactorContextWorkspaceId = ctx.getOrDefault(UserMdcCoFilter.REACTOR_WORKSPACE_ID_KEY, null)
                Mono.empty()
            }
        }

        // Act
        filter.filter(exchange, chain).block()

        // Assert
        assertEquals(LogMasker.mask(headerWorkspaceId), reactorContextWorkspaceId)
    }

    @Test
    fun `should skip body extraction for GET requests`() {
        // Arrange
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"))
        var chainCalled = false
        val chain = WebFilterChain {
            chainCalled = true
            Mono.empty()
        }

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete()

        assertEquals(true, chainCalled)
    }

    @Test
    fun `should handle malformed JSON body gracefully`() {
        // Arrange
        val malformedJson = """{"workspaceId": "missing-quote}"""
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/test")
                .contentType(MediaType.APPLICATION_JSON)
                .body(malformedJson),
        )
        var chainCalled = false
        val chain = WebFilterChain {
            chainCalled = true
            Mono.empty()
        }

        // Act & Assert - should not throw
        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete()

        assertEquals(true, chainCalled)
    }

    @Test
    fun `should propagate both userId and workspaceId when both present`() {
        // Arrange
        val userId = "user123"
        val workspaceId = "workspace456"
        val jwt = mockJwt(userId)
        val securityContext = createSecurityContext(jwt)
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header("X-Workspace-Id", workspaceId),
        )
        var reactorContextUserId: String? = null
        var reactorContextWorkspaceId: String? = null

        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                reactorContextUserId = ctx.getOrDefault(UserMdcCoFilter.REACTOR_USER_ID_KEY, null)
                reactorContextWorkspaceId = ctx.getOrDefault(UserMdcCoFilter.REACTOR_WORKSPACE_ID_KEY, null)
                Mono.empty()
            }
        }

        // Act
        filter.filter(exchange, chain)
            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
            .block()

        // Assert
        assertNotNull(reactorContextUserId)
        assertNotNull(reactorContextWorkspaceId)
        assertEquals(LogMasker.mask(userId), reactorContextUserId)
        assertEquals(LogMasker.mask(workspaceId), reactorContextWorkspaceId)
    }

    @Test
    fun `should handle PUT method for body extraction`() {
        // Arrange
        val workspaceId = "workspace-put"
        val jsonBody = """{"workspaceId": "$workspaceId"}"""
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.put("/test")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody),
        )
        var reactorContextWorkspaceId: String? = null

        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                reactorContextWorkspaceId = ctx.getOrDefault(UserMdcCoFilter.REACTOR_WORKSPACE_ID_KEY, null)
                Mono.empty()
            }
        }

        // Act
        filter.filter(exchange, chain).block()

        // Assert
        assertNotNull(reactorContextWorkspaceId)
        assertEquals(LogMasker.mask(workspaceId), reactorContextWorkspaceId)
    }

    @Test
    fun `should handle PATCH method for body extraction`() {
        // Arrange
        val workspaceId = "workspace-patch"
        val jsonBody = """{"workspaceId": "$workspaceId"}"""
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.patch("/test")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody),
        )
        var reactorContextWorkspaceId: String? = null

        val chain = WebFilterChain { _ ->
            Mono.deferContextual { ctx ->
                reactorContextWorkspaceId = ctx.getOrDefault(UserMdcCoFilter.REACTOR_WORKSPACE_ID_KEY, null)
                Mono.empty()
            }
        }

        // Act
        filter.filter(exchange, chain).block()

        // Assert
        assertNotNull(reactorContextWorkspaceId)
        assertEquals(LogMasker.mask(workspaceId), reactorContextWorkspaceId)
    }

    // Helper functions
    private fun mockJwt(subject: String): Jwt {
        val jwt = mockk<Jwt>()
        every { jwt.subject } returns subject
        return jwt
    }

    private fun createSecurityContext(jwt: Jwt): SecurityContext {
        val auth = mockk<Authentication>()
        every { auth.principal } returns jwt

        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns auth

        return securityContext
    }
}
