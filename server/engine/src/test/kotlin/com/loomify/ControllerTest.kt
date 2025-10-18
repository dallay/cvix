package com.loomify

import com.loomify.common.domain.bus.Mediator
import com.loomify.engine.controllers.GlobalExceptionHandler
import com.loomify.spring.boot.ApiController
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.JwtMutator
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@UnitTest
@WithMockUser
abstract class ControllerTest {
    protected val mediator = mockk<Mediator>()
    protected val userId: UUID = UUID.randomUUID()

    abstract val webTestClient: WebTestClient

    @BeforeEach
    protected open fun setUp() {
        mockSecurity()
    }

    protected fun buildWebTestClient(controller: ApiController): WebTestClient {
        val jwtAuthenticationToken: JwtAuthenticationToken = jwtAuthenticationToken()
        mockSecurity(jwtAuthenticationToken)

        return WebTestClient.bindToController(controller)
            .controllerAdvice(GlobalExceptionHandler()) // Attach the global exception handler
            .apply {
                csrf()
            }.apply {
                mockAuthentication<JwtMutator>(jwtAuthenticationToken)
            }
            .build()
            .mutateWith(csrf())
            .mutateWith(mockAuthentication(jwtAuthenticationToken))
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
        val authorities = AuthorityUtils.createAuthorityList("ROLE_USER")
        return JwtAuthenticationToken(jwt, authorities)
    }

    @AfterEach
    protected fun tearDown() {
        unmockkStatic(ReactiveSecurityContextHolder::class)
    }
}
