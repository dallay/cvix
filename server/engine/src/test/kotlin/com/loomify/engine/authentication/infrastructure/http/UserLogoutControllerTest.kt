package com.loomify.engine.authentication.infrastructure.http

import com.loomify.UnitTest
import com.loomify.common.domain.bus.Mediator
import com.loomify.engine.authentication.application.logout.UserLogoutCommand
import com.loomify.engine.authentication.infrastructure.cookie.AuthCookieBuilder
import com.loomify.engine.controllers.GlobalExceptionHandler
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

private const val ENDPOINT = "/api/auth/logout"

@UnitTest
internal class UserLogoutControllerTest {

    private val mediator: Mediator = mockk()
    private val userLogoutController = UserLogoutController(mediator)
    private val webTestClient = WebTestClient.bindToController(userLogoutController)
        .controllerAdvice(GlobalExceptionHandler())
        .build()

    @BeforeEach
    fun setUp() {
        coEvery { mediator.send(any(UserLogoutCommand::class)) } returns Unit
    }

    @Test
    fun `logout user successfully`() {

        webTestClient.post()
            .uri(ENDPOINT)
            .cookie(AuthCookieBuilder.REFRESH_TOKEN, "validRefreshToken")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `logout user with missing cookies`() {

        webTestClient.post()
            .uri(ENDPOINT)
            .exchange()
            .expectStatus().isBadRequest
    }
}
