package com.cvix

import com.cvix.common.domain.bus.Mediator
import com.cvix.config.WorkspaceContextWebFilter
import com.cvix.controllers.GlobalExceptionHandler
import com.cvix.spring.boot.ApiController
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.context.MessageSource
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@UnitTest
@WithMockUser
abstract class ControllerTest {
    protected val mediator = mockk<Mediator>()
    protected val userId: UUID = UUID.randomUUID()
    protected val workspaceId: UUID = UUID.randomUUID()
    protected val messageSource = mockk<MessageSource>(relaxed = true)

    abstract val webTestClient: WebTestClient

    @BeforeEach
    protected open fun setUp() {
        mockSecurity()
        mockMessageSource()
    }

    private fun mockMessageSource() {
        // Mock for 3-argument getMessage(String, Array?, Locale)
        // This is used by getLocalizedMessage() method
        every {
            messageSource.getMessage(
                any<String>(),
                isNull(),
                any<Locale>(),
            )
        } returns "Internal server error"

        // Mock for 4-argument getMessage(String, Array?, String, Locale)
        // This is used by most exception handlers with a default message
        every {
            messageSource.getMessage(
                any<String>(),
                any(),
                any<String>(),
                any<Locale>(),
            )
        } answers { thirdArg() }
    }

    /**
     * Builds a WebTestClient for controller testing with CSRF and mock JWT authentication.
     * Uses Spring Security 7's mockJwt() for simpler, more reliable testing.
     * Static security context is already set up in @BeforeEach setUp().
     */
    protected fun buildWebTestClient(controller: ApiController): WebTestClient {
        return WebTestClient.bindToController(controller)
            .webFilter<WebTestClient.ControllerSpec>(WorkspaceContextWebFilter())
            .controllerAdvice(GlobalExceptionHandler(messageSource))
            .configureClient()
            .build()
            .mutateWith(csrf())
            .mutateWith(
                mockJwt()
                    .jwt { jwt ->
                        jwt.subject(userId.toString())
                            .claim("preferred_username", "test-user")
                            .claim("roles", listOf(ROLE_USER))
                    }
                    .authorities(AuthorityUtils.createAuthorityList(ROLE_USER)),
            )
    }

    private fun mockSecurity(jwtToken: JwtAuthenticationToken = jwtAuthenticationToken()) {
        mockkStatic(ReactiveSecurityContextHolder::class)

        // The securityContext mock should return the actual jwtToken instance
        // for the getAuthentication() call.
        val securityContext: SecurityContext = mockk {
            every { authentication } returns jwtToken
        }

        every { ReactiveSecurityContextHolder.getContext() } returns Mono.just(securityContext)
    }

    protected fun jwtAuthenticationToken(): JwtAuthenticationToken {
        val jwt = Jwt.withTokenValue("mockToken")
            .header("alg", "none")
            .claim("sub", userId.toString())
            .build()
        val authorities = AuthorityUtils.createAuthorityList(ROLE_USER)
        return JwtAuthenticationToken(jwt, authorities)
    }

    @AfterEach
    protected fun tearDown() {
        unmockkStatic(ReactiveSecurityContextHolder::class)
    }
}
