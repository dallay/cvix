package com.cvix.controllers

import com.cvix.UnitTest
import com.cvix.common.domain.error.BusinessRuleValidationException
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContext
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange

@UnitTest
internal class GlobalExceptionHandlerTest {

    private val messageSource = mockk<MessageSource>()
    private val handler = GlobalExceptionHandler(messageSource)
    private val exchange = mockk<ServerWebExchange>()

    @Test
    fun `should return bad request problem detail when illegal argument exception occurs`() {
        val exception = IllegalArgumentException("Invalid argument")
        val requestMock = mockk<ServerHttpRequest>()
        val localeContextMock = mockk<LocaleContext>()

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id"
        every { exchange.localeContext } returns localeContextMock
        every { localeContextMock.locale } returns Locale.ENGLISH
        // Generic mock: return the default message (3rd argument) for any getMessage call
        every { messageSource.getMessage(any(), any(), any(), any()) } answers { thirdArg() }

        val problemDetail = handler.handleIllegalArgumentException(exception, exchange)

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.status)
        assertNotNull(problemDetail.title)
        assertEquals("Invalid argument", problemDetail.detail)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
        assertEquals("test-trace-id", problemDetail.properties?.get("traceId"))
    }

    @Test
    fun `should return bad request problem detail when business rule validation exception occurs`() {
        val exception = mockk<BusinessRuleValidationException>()
        val requestMock = mockk<ServerHttpRequest>()
        val localeContextMock = mockk<LocaleContext>()

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id"
        every { exception.message } returns "Business rule violation"
        every { exchange.localeContext } returns localeContextMock
        every { localeContextMock.locale } returns Locale.ENGLISH
        // Generic mock: return the default message (3rd argument) for any getMessage call
        every { messageSource.getMessage(any(), any(), any(), any()) } answers { thirdArg() }

        val problemDetail = handler.handleIllegalArgumentException(exception, exchange)

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.status)
        assertNotNull(problemDetail.title)
        // handler prefers exception.message when present
        assertEquals("Business rule violation", problemDetail.detail)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
        assertEquals("test-trace-id", problemDetail.properties?.get("traceId"))
    }

    @Test
    fun `should return internal server error problem detail when exception occurs`() {
        // Use a real exception instead of a mock to avoid recursion issues with logging
        val exception = RuntimeException("Unexpected error")
        val requestMock = mockk<ServerHttpRequest>()
        val localeContextMock = mockk<LocaleContext>()

        every { exchange.request } returns requestMock
        every { requestMock.id } returns "test-trace-id"
        every { exchange.localeContext } returns localeContextMock
        every { localeContextMock.locale } returns Locale.ENGLISH
        // Mock for 3-argument getMessage(String, Array?, Locale) signature
        every { messageSource.getMessage(any<String>(), isNull(), any<Locale>()) } returns "Internal server error"

        val problemDetail = handler.handleGenericException(exception, exchange)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.status)
        assertEquals("An internal server error occurred", problemDetail.detail)
        assertNotNull(problemDetail.title)
        assertNotNull(problemDetail.properties?.get("localizedMessage"))
        assertNotNull(problemDetail.properties?.get("message"))
        assertNotNull(problemDetail.properties?.get("traceId"))
        assertEquals("test-trace-id", problemDetail.properties?.get("traceId"))
    }
}
