package com.cvix.subscriber.application.create

import com.cvix.UnitTest
import com.cvix.common.domain.bus.event.EventPublisher
import com.cvix.common.domain.security.Hasher
import com.cvix.subscriber.domain.Subscriber
import com.cvix.subscriber.domain.SubscriberRepository
import com.cvix.subscriber.domain.event.SubscriberCreatedEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.slot
import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class CreateSubscriberCommandHandlerTest {
    private val eventPublisher: EventPublisher<SubscriberCreatedEvent> = mockk()
    private val subscriberRepository: SubscriberRepository = mockkClass(SubscriberRepository::class)
    private val hasher: Hasher = mockk()
    private val subscriberRegistrator: SubscriberRegistrator =
        SubscriberRegistrator(subscriberRepository, eventPublisher, hasher)
    private val createSubscriberCommandHandler: CreateSubscriberCommandHandler =
        CreateSubscriberCommandHandler(subscriberRegistrator)
    private val faker = Faker()
    private lateinit var subscriberCommand: CreateSubscriberCommand

    @BeforeEach
    fun setUp() {
        every { hasher.hash(any()) } returns "a".repeat(64)
        coEvery { subscriberRepository.create(any(Subscriber::class)) } returns Unit
        coEvery { eventPublisher.publish(any(SubscriberCreatedEvent::class)) } returns Unit
        subscriberCommand = CreateSubscriberCommand(
            id = UUID.randomUUID(),
            email = faker.internet().emailAddress(),
            source = "test-source",
            language = "en",
            ipAddress = faker.internet().ipV4Address(),
            workspaceId = UUID.randomUUID(),
        )
    }

    @Test
    fun `should register a subscriber`() = runTest {
        createSubscriberCommandHandler.handle(subscriberCommand)

        coVerify(exactly = 1) {
            subscriberRepository.create(
                match {
                    it.id.value == subscriberCommand.id &&
                        it.email.value == subscriberCommand.email &&
                        it.source.source == subscriberCommand.source &&
                        it.language.code == subscriberCommand.language &&
                        it.workspaceId.value == subscriberCommand.workspaceId
                },
            )
        }
        coVerify(exactly = 1) {
            eventPublisher.publish(ofType<SubscriberCreatedEvent>())
        }
    }

    @Test
    fun `should create subscriber and publish event when handle is called`() = runTest {
        // Given
        val id = UUID.randomUUID()
        val email = faker.internet().emailAddress()
        val source = "landing-hero"
        val ipV4Address = faker.internet().ipV4Address()
        val workspaceId = UUID.randomUUID()
        val command = CreateSubscriberCommand(
            id = id,
            email = email,
            source = source,
            language = "en",
            ipAddress = ipV4Address,
            workspaceId = workspaceId,
        )

        // Mocks & Captors
        val subscriberSlot = slot<Subscriber>()
        val eventSlot = slot<SubscriberCreatedEvent>()
        coEvery { subscriberRepository.create(capture(subscriberSlot)) } returns Unit
        coEvery { eventPublisher.publish(capture(eventSlot)) } returns Unit

        // When
        createSubscriberCommandHandler.handle(command)

        // Then: verify interactions occurred and the arguments passed
        coVerify(exactly = 1) { subscriberRepository.create(any()) }
        coVerify(exactly = 1) { eventPublisher.publish(any<SubscriberCreatedEvent>()) }

        // Check the saved subscriber fields
        val savedSubscriber = subscriberSlot.captured
        assertEquals(email, savedSubscriber.email.value)
        assertEquals(source, savedSubscriber.source.source)
        assertEquals(command.language, savedSubscriber.language.code)
        assertEquals(workspaceId, savedSubscriber.workspaceId.value)

        // Check the published event fields
        val publishedEvent = eventSlot.captured
        assertEquals(savedSubscriber.id.value.toString(), publishedEvent.aggregateId)
        assertEquals(savedSubscriber.email.value, publishedEvent.email)
        // If the event includes normalized source/language fields, assert basics
        assertEquals(savedSubscriber.language, publishedEvent.language)
    }
}
