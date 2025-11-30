package com.cvix.spring.boot

import com.cvix.common.domain.bus.HandlerNotFoundException
import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.bus.command.CommandWithResult
import com.cvix.common.domain.bus.command.CommandWithResultHandler
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

private var springTestCounter = 0
private var springAsyncTestCounter = 0

@SpringBootTest(classes = [AppAutoConfiguration::class, MyAsyncCommandRHandler::class])
class CommandWithResultHandlerTest {

    init {
        springTestCounter = 0
        springAsyncTestCounter = 0
    }

    @Autowired
    lateinit var mediator: Mediator

    @Test
    fun `async commandHandler should be fired`() = runTest {
        mediator.send(MyCommandR())

        assertTrue {
            springAsyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runTest {
                mediator.send(NonExistCommandR())
            }
        }

        assertNotNull(exception)
        assertEquals(
            exception.message,
            "handler could not be found for com.cvix.spring.boot.NonExistCommandR",
        )
    }
}

class Result

class NonExistCommandR : CommandWithResult<Result>
class MyCommandR : CommandWithResult<Result>

class MyAsyncCommandRHandler : CommandWithResultHandler<MyCommandR, Result> {
    override suspend fun handle(command: MyCommandR): Result {
        delay(500)
        springAsyncTestCounter++

        return Result()
    }
}
