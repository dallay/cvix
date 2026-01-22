package com.cvix.authentication.infrastructure.http

import com.cvix.UnitTest
import com.cvix.authentication.application.logout.UserLogoutCommand
import com.cvix.authentication.infrastructure.cookie.AuthCookieBuilder
import com.cvix.common.domain.bus.Mediator
import com.cvix.controllers.GlobalExceptionHandler
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import org.springframework.test.web.reactive.server.WebTestClient

private const val ENDPOINT = "/api/auth/logout"

@UnitTest
internal class UserLogoutControllerTest {

    private val mediator: Mediator = mockk()
    private val messageSource = mockk<MessageSource>(relaxed = true)
    private val userLogoutController = UserLogoutController(mediator)
    private val webTestClient = WebTestClient.bindToController(userLogoutController)
        .controllerAdvice(
            com.cvix.authentication.infrastructure.CookieAdvice(messageSource),
            GlobalExceptionHandler(messageSource),
        )
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
