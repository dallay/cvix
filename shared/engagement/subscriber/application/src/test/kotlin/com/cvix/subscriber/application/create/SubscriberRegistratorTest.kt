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
import io.mockk.slot
import java.util.UUID
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
internal class SubscriberRegistratorTest {
    private val faker = Faker()
    private val subscriberRepository: SubscriberRepository = mockk()
    private val eventPublisher: EventPublisher<SubscriberCreatedEvent> = mockk(relaxed = true)
    private val hasher: Hasher = mockk()
    private lateinit var registrator: SubscriberRegistrator

    @BeforeEach
    fun setUp() {
        every { hasher.hash(any()) } returns "a".repeat(64)
        registrator = SubscriberRegistrator(subscriberRepository, eventPublisher, hasher)
    }

    @Test
    fun `should register subscriber and publish event`() = runTest {
        // Given
        val id = UUID.randomUUID()
        val email = faker.internet().emailAddress()
        val source = "test-src"
        val language = com.cvix.common.domain.model.Language.fromString("en")
        val ip = faker.internet().ipV4Address()
        val workspaceId = UUID.randomUUID()

        val subscriberSlot = slot<Subscriber>()
        coEvery { subscriberRepository.create(capture(subscriberSlot)) } returns Unit
        coEvery { eventPublisher.publish(any<SubscriberCreatedEvent>()) } returns Unit

        // When
        registrator.register(id, email, source, language, ip, null, workspaceId)

        // Then
        coVerify(exactly = 1) { subscriberRepository.create(any()) }
        coVerify(exactly = 1) { eventPublisher.publish(any<SubscriberCreatedEvent>()) }

        val saved = subscriberSlot.captured
        assertEquals(id, saved.id.value)
        assertEquals(email, saved.email.value)
        assertEquals(source, saved.source.source)
        assertEquals(language, saved.language)
        assertEquals(workspaceId, saved.workspaceId.value)
    }

    @Test
    fun `should propagate exception from repository and not publish events`() = runTest {
        // Given
        val id = UUID.randomUUID()
        val email = faker.internet().emailAddress()
        val source = "test-src"
        val language = com.cvix.common.domain.model.Language.fromString("en")
        val workspaceId = UUID.randomUUID()

        coEvery { subscriberRepository.create(any()) } throws RuntimeException("boom")

        // When / Then
        val thrown = runCatching {
            registrator.register(id, email, source, language, null, null, workspaceId)
        }.exceptionOrNull()
        org.junit.jupiter.api.Assertions.assertNotNull(thrown, "Expected exception to be thrown")
        assertEquals("boom", thrown?.message)

        coVerify(exactly = 1) { subscriberRepository.create(any()) }
        // No events should be published when repository fails
        coVerify(exactly = 0) { eventPublisher.publish(any<SubscriberCreatedEvent>()) }
    }
}
