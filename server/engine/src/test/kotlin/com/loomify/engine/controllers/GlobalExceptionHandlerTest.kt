package com.loomify.engine.controllers

import com.loomify.UnitTest
import com.loomify.common.domain.error.BusinessRuleValidationException
import io.mockk.every
import io.mockk.mockk
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange

@UnitTest
internal class GlobalExceptionHandlerTest {

    private val messageSource = mockk<MessageSource>()
    private val handler = GlobalExceptionHandler(messageSource)
    private val exchange = mockk<ServerWebExchange>()

    @Test
    fun `should return bad request problem detail when illegal argument exception occurs`() {
        val exception = mockk<IllegalArgumentException>()
        val requestMock = mockk<org.springframework.http.server.reactive.ServerHttpRequest>()

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id"
        every { exception.message } returns "Invalid argument"
        every {
            messageSource.getMessage(
                "error.bad_request",
                null,
                any<Locale>(),
            )
        } returns "Bad request"

        val problemDetail = handler.handleIllegalArgumentException(exception, exchange)

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.status)
        assertEquals("Bad request", problemDetail.title)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
        assertEquals("test-trace-id", problemDetail.properties?.get("traceId"))
    }

    @Test
    fun `should return bad request problem detail when business rule validation exception occurs`() {
        val exception = mockk<BusinessRuleValidationException>()
        val requestMock = mockk<org.springframework.http.server.reactive.ServerHttpRequest>()

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id"
        every { exception.message } returns "Business rule violation"
        every {
            messageSource.getMessage(
                "error.bad_request",
                null,
                any<Locale>(),
            )
        } returns "Bad request"

        val problemDetail = handler.handleIllegalArgumentException(exception, exchange)

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.status)
        assertEquals("Bad request", problemDetail.title)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
    }

    @Test
    fun `should return internal server error problem detail when exception occurs`() {
        val exception = mockk<Exception>()
        val requestMock = mockk<org.springframework.http.server.reactive.ServerHttpRequest>()

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id"
        every { exception.message } returns "Unexpected error"
        every {
            messageSource.getMessage(
                "error.internal_server_error",
                null,
                any<Locale>(),
            )
        } returns "Internal server error"

        val problemDetail = handler.handleGenericException(exception, exchange)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.status)
        assertEquals("Internal server error", problemDetail.title)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
        assertEquals("test-trace-id", problemDetail.properties?.get("traceId"))
    }
}
