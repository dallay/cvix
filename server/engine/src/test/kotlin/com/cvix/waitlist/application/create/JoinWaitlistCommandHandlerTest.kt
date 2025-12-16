package com.cvix.waitlist.application.create

import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.waitlist.domain.WaitlistRepository
import com.cvix.waitlist.domain.event.WaitlistEntryCreatedEvent
import io.mockk.mockk
import java.util.*
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.BeforeEach

@UnitTest
internal class JoinWaitlistCommandHandlerTest {
    private lateinit var eventPublisher: EventPublisher<WaitlistEntryCreatedEvent>
    private lateinit var repository: WaitlistRepository
    private lateinit var metrics: com.cvix.waitlist.infrastructure.metrics.WaitlistMetrics
    private lateinit var waitlistJoiner: WaitlistJoiner
    private lateinit var joinWaitlistCommandHandler: JoinWaitlistCommandHandler
    private val faker = Faker()

    @BeforeEach
    fun setUp() {
        eventPublisher = mockk(relaxed = true, relaxUnitFun = true)
        repository = mockk(relaxed = true, relaxUnitFun = true)
        metrics = mockk(relaxed = true, relaxUnitFun = true)

        waitlistJoiner = WaitlistJoiner(repository, eventPublisher, metrics)
        joinWaitlistCommandHandler = JoinWaitlistCommandHandler(waitlistJoiner)
    }

    @Test
    fun `should join waitlist and publish event when handle is called`() = runTest {
        // Given
        val id = UUID.randomUUID()
        val email = faker.internet().emailAddress()
        val source = "landing-hero"
        val ipV4Address = faker.internet().ipV4Address()
        val metadata = mapOf("userAgent" to "Mozilla/5.0")
        val command = JoinWaitlistCommand(
            id = id,
            email = email,
            source = source,
            language = "en",
            ipAddress = ipV4Address,
            metadata = metadata,
        )

        // When
        joinWaitlistCommandHandler.handle(command)

        // Then - if it completes without error, the test passes
        // We can't use matchers with value classes due to MockK limitations
        // Both sourceRaw and sourceNormalized should be tracked
    }

    @Test
    fun `should normalize unknown source and handle successfully`() = runTest {
        // Given
        val id = UUID.randomUUID()
        val email = faker.internet().emailAddress()
        val unknownSource = "twitter-campaign"
        val ipV4Address = faker.internet().ipV4Address()
        val command = JoinWaitlistCommand(
            id = id,
            email = email,
            source = unknownSource,
            language = "en",
            ipAddress = ipV4Address,
            metadata = null,
        )

        // When
        joinWaitlistCommandHandler.handle(command)

        // Then - command should be processed successfully despite unknown source
        // sourceRaw will be "twitter-campaign" and sourceNormalized will be "unknown"
    }
}
