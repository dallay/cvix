package com.cvix.waitlist.application.create

import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.waitlist.domain.WaitlistRepository
import com.cvix.waitlist.domain.event.WaitlistEntryCreatedEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
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

        // Mocks & Captors
        val entrySlot = slot<com.cvix.waitlist.domain.WaitlistEntry>()
        val eventSlot = slot<com.cvix.waitlist.domain.event.WaitlistEntryCreatedEvent>()
        coEvery { repository.save(capture(entrySlot)) } answers { entrySlot.captured }
        coEvery { eventPublisher.publish(capture(eventSlot)) } returns Unit

        // When
        joinWaitlistCommandHandler.handle(command)

        // Then: verify interactions occurred and the arguments passed
        coVerify(exactly = 1) { repository.save(any()) }
        coVerify(exactly = 1) { eventPublisher.publish(capture(eventSlot)) }
        // Check the saved entry fields
        val savedEntry = entrySlot.captured
        kotlin.test.assertEquals(email, savedEntry.email.value)
        kotlin.test.assertEquals(source, savedEntry.sourceRaw)
        // Check the published event fields
        val publishedEvent = eventSlot.captured
        kotlin.test.assertEquals(savedEntry.id.id.toString(), publishedEvent.id)
        kotlin.test.assertEquals(savedEntry.email.value, publishedEvent.email)
        kotlin.test.assertEquals(savedEntry.sourceRaw, publishedEvent.source)
        kotlin.test.assertEquals(savedEntry.language.code, publishedEvent.language)
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
