package com.cvix.spring.boot.presentation.pagination

import com.cvix.common.domain.presentation.pagination.OffsetPageResponse
import com.cvix.spring.boot.entity.Person
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpResponse
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Suppress("ReactiveStreamsUnusedPublisher")
internal class PageResponsePresenterTest {
    private val jsonMapper: JsonMapper = jsonMapper {
        addModule(kotlinModule())
    }
    private val offsetPagePresenter = OffsetPagePresenter(jsonMapper)

    @Test
    fun present() = runTest {
        val exchange = mockk<ServerWebExchange>()
        val result = mockk<HandlerResult>()

        val serverResponse = MockServerHttpResponse()
        val person = Person(name = "test", age = 0)
        val page = OffsetPageResponse(
            data = listOf(person),
            total = 1,
            perPage = 10,
            page = 0,
        )

        every { exchange.response } returns serverResponse
        every { result.returnValue } returns Mono.just(page)

        offsetPagePresenter.present(exchange, result)

        val headers = serverResponse.headers
        assertEquals(listOf(page.total.toString()), headers["Total-Count"])
        assertEquals(listOf(page.page.toString()), headers["Page"])
        assertEquals(listOf(page.perPage.toString()), headers["Per-Page"])

        val body = serverResponse.bodyAsString.awaitSingle()
            .let { jsonMapper.readValue(it, object : TypeReference<List<Person>>() {}) }
        assertEquals(1, body.size)
        assertEquals(person, body[0])
    }
}
