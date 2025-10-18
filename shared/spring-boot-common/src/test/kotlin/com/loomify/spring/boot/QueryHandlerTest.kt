package com.loomify.spring.boot

import com.loomify.common.domain.bus.HandlerNotFoundException
import com.loomify.common.domain.bus.Mediator
import com.loomify.common.domain.bus.query.Query
import com.loomify.common.domain.bus.query.QueryHandler
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [AppAutoConfiguration::class, TestQueryHandler::class])
class QueryHandlerTest {

    @Autowired
    lateinit var mediator: Mediator

    @Test
    fun `async queryHandler should retrieve result`() = runTest {
        val result = mediator.send(TestQuery(1))

        assertTrue {
            result == "hello 1"
        }
    }

    @Test
    fun `should throw exception if given async query does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runTest {
                mediator.send(NonExistQuery())
            }
        }

        assertNotNull(exception)
        assertEquals(
            "handler could not be found for com.loomify.spring.boot.NonExistQuery",
            exception.message,
        )
    }
}

class NonExistQuery : Query<String>
class TestQuery(val id: Int) : Query<String>

class TestQueryHandler : QueryHandler<TestQuery, String> {
    override suspend fun handle(query: TestQuery): String = "hello " + query.id
}
